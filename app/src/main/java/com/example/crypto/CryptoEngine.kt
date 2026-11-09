package com.example.crypto

import com.example.data.model.DecryptedResult
import com.example.data.model.ExtractionStatus
import com.example.data.model.SecretPayload
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoEngine {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val IV_LENGTH_BYTES = 12
    private const val SALT_LENGTH_BYTES = 16
    private const val PBKDF2_ITERATIONS = 12000
    private const val KEY_LENGTH_BITS = 256

    // Magic bytes "SDKR" (Sandi-Kartika Header)
    val MAGIC_HEADER = byteArrayOf(0x53, 0x44, 0x4B, 0x52)
    private const val MODE_SINGLE: Byte = 0x01
    private const val MODE_DENIABLE: Byte = 0x02

    private val secureRandom = SecureRandom()

    /**
     * Derives a 256-bit AES key from a passphrase and salt using PBKDF2WithHmacSHA256
     */
    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plaintext bytes using AES-256-GCM.
     * Returns: [Salt (16B)] + [IV (12B)] + [Ciphertext + AuthTag]
     */
    private fun encryptAesGcm(plainBytes: ByteArray, passphrase: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES).apply { secureRandom.nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_BYTES).apply { secureRandom.nextBytes(this) }
        val key = deriveKey(passphrase.toCharArray(), salt)

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val cipherText = cipher.doFinal(plainBytes)

        val buffer = ByteBuffer.allocate(SALT_LENGTH_BYTES + IV_LENGTH_BYTES + cipherText.size)
        buffer.put(salt)
        buffer.put(iv)
        buffer.put(cipherText)
        return buffer.array()
    }

    /**
     * Attempts to decrypt an AES-GCM block: [Salt (16B)] + [IV (12B)] + [Ciphertext + AuthTag]
     * Returns decrypted plaintext bytes or null if decryption / GCM verification fails.
     */
    private fun tryDecryptAesGcm(encryptedBlock: ByteArray, passphrase: String): ByteArray? {
        if (encryptedBlock.size < SALT_LENGTH_BYTES + IV_LENGTH_BYTES + 16) {
            return null
        }
        return try {
            val buffer = ByteBuffer.wrap(encryptedBlock)
            val salt = ByteArray(SALT_LENGTH_BYTES)
            buffer.get(salt)
            val iv = ByteArray(IV_LENGTH_BYTES)
            buffer.get(iv)
            val cipherText = ByteArray(buffer.remaining())
            buffer.get(cipherText)

            val key = deriveKey(passphrase.toCharArray(), salt)
            val cipher = Cipher.getInstance(ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
            cipher.doFinal(cipherText)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Builds a single or deniable encrypted container byte array.
     */
    fun buildEncryptedPackage(
        masterPayload: SecretPayload,
        masterPassword: String,
        decoyPayload: SecretPayload? = null,
        decoyPassword: String? = null
    ): ByteArray {
        val isDeniable = decoyPayload != null && !decoyPassword.isNullOrBlank()

        val masterPlain = masterPayload.toJsonString().toByteArray(Charsets.UTF_8)
        val masterEncrypted = encryptAesGcm(masterPlain, masterPassword)

        val now = System.currentTimeMillis()

        if (!isDeniable) {
            // Mode Single
            val totalSize = MAGIC_HEADER.size + 1 + 8 + 4 + masterEncrypted.size
            val buffer = ByteBuffer.allocate(totalSize)
            buffer.put(MAGIC_HEADER)
            buffer.put(MODE_SINGLE)
            buffer.putLong(now)
            buffer.putInt(masterEncrypted.size)
            buffer.put(masterEncrypted)
            return buffer.array()
        } else {
            // Mode Deniable: contains Decoy block AND Master block
            val decoyPlain = decoyPayload.toJsonString().toByteArray(Charsets.UTF_8)
            val decoyEncrypted = encryptAesGcm(decoyPlain, decoyPassword!!)

            val totalSize = MAGIC_HEADER.size + 1 + 8 + 4 + decoyEncrypted.size + 4 + masterEncrypted.size
            val buffer = ByteBuffer.allocate(totalSize)
            buffer.put(MAGIC_HEADER)
            buffer.put(MODE_DENIABLE)
            buffer.putLong(now)

            buffer.putInt(decoyEncrypted.size)
            buffer.put(decoyEncrypted)

            buffer.putInt(masterEncrypted.size)
            buffer.put(masterEncrypted)
            return buffer.array()
        }
    }

    /**
     * Decrypts an encrypted container byte array using the provided password.
     * Automatically probes Master and Decoy blocks.
     */
    fun decryptPackage(
        packageBytes: ByteArray,
        password: String,
        officerOverridePin: String? = null
    ): DecryptedResult {
        val startTime = System.currentTimeMillis()

        if (packageBytes.size < MAGIC_HEADER.size + 1 + 8 + 4) {
            return DecryptedResult(
                status = ExtractionStatus.ERROR_CORRUPTED,
                errorMessage = "Ukuran paket enkripsi tidak memenuhi syarat minimum."
            )
        }

        val buffer = ByteBuffer.wrap(packageBytes)

        // Verify magic header
        val header = ByteArray(MAGIC_HEADER.size)
        buffer.get(header)
        if (!header.contentEquals(MAGIC_HEADER)) {
            return DecryptedResult(
                status = ExtractionStatus.ERROR_NO_PAYLOAD,
                errorMessage = "Header Sandi-Kartika tidak ditemukan pada media ini."
            )
        }

        val mode = buffer.get()
        val timestamp = buffer.getLong()

        if (mode == MODE_SINGLE) {
            val masterLen = buffer.getInt()
            if (masterLen <= 0 || buffer.remaining() < masterLen) {
                return DecryptedResult(
                    status = ExtractionStatus.ERROR_CORRUPTED,
                    errorMessage = "Struktur blok master korup atau tidak lengkap."
                )
            }
            val masterBytes = ByteArray(masterLen)
            buffer.get(masterBytes)

            val decrypted = tryDecryptAesGcm(masterBytes, password)
            if (decrypted == null) {
                return DecryptedResult(
                    status = ExtractionStatus.ERROR_WRONG_PASSWORD,
                    errorMessage = "Kata sandi tidak valid. GCM Authentication Tag gagal diverifikasi."
                )
            }

            return parsePayloadResult(decrypted, isDecoy = false, startTime = startTime, officerOverridePin = officerOverridePin)
        } else if (mode == MODE_DENIABLE) {
            val decoyLen = buffer.getInt()
            if (decoyLen <= 0 || buffer.remaining() < decoyLen) {
                return DecryptedResult(
                    status = ExtractionStatus.ERROR_CORRUPTED,
                    errorMessage = "Blok decoy korup."
                )
            }
            val decoyBytes = ByteArray(decoyLen)
            buffer.get(decoyBytes)

            if (buffer.remaining() < 4) {
                return DecryptedResult(
                    status = ExtractionStatus.ERROR_CORRUPTED,
                    errorMessage = "Header blok master hilang."
                )
            }
            val masterLen = buffer.getInt()
            if (masterLen <= 0 || buffer.remaining() < masterLen) {
                return DecryptedResult(
                    status = ExtractionStatus.ERROR_CORRUPTED,
                    errorMessage = "Blok master korup."
                )
            }
            val masterBytes = ByteArray(masterLen)
            buffer.get(masterBytes)

            // Step 1: Probe master key first
            val masterDecrypted = tryDecryptAesGcm(masterBytes, password)
            if (masterDecrypted != null) {
                return parsePayloadResult(masterDecrypted, isDecoy = false, startTime = startTime, officerOverridePin = officerOverridePin)
            }

            // Step 2: Probe decoy key (Deniable camouflage password)
            val decoyDecrypted = tryDecryptAesGcm(decoyBytes, password)
            if (decoyDecrypted != null) {
                // Successfully opened decoy message! Under duress, this looks like the only valid message!
                return parsePayloadResult(decoyDecrypted, isDecoy = true, startTime = startTime, officerOverridePin = officerOverridePin)
            }

            // Both failed
            return DecryptedResult(
                status = ExtractionStatus.ERROR_WRONG_PASSWORD,
                errorMessage = "Kata sandi tidak cocok dengan berkas terenkripsi."
            )
        } else {
            return DecryptedResult(
                status = ExtractionStatus.ERROR_CORRUPTED,
                errorMessage = "Mode enkripsi taktis tidak dikenali (0x${"%02X".format(mode)})."
            )
        }
    }

    private fun parsePayloadResult(
        plainBytes: ByteArray,
        isDecoy: Boolean,
        startTime: Long,
        officerOverridePin: String? = null
    ): DecryptedResult {
        return try {
            val jsonStr = String(plainBytes, Charsets.UTF_8)
            val payload = SecretPayload.fromJsonString(jsonStr)

            val rawResult = DecryptedResult(
                status = if (isDecoy) ExtractionStatus.SUCCESS_DECOY else ExtractionStatus.SUCCESS_MASTER,
                payload = payload,
                isDecoy = isDecoy,
                isTimeLocked = payload.timeLockEpochMs > 0,
                timeLockRemainingMs = 0L,
                executionDurationMs = System.currentTimeMillis() - startTime
            )

            // Enforce tactical time-lock policy via TacticalTimeLockService
            com.example.service.TacticalTimeLockService.instance.enforceTimeLockOnResult(
                rawResult,
                officerOverridePin
            )
        } catch (e: Exception) {
            DecryptedResult(
                status = ExtractionStatus.ERROR_CORRUPTED,
                errorMessage = "Gagal memproses format pesan terurai: ${e.message}"
            )
        }
    }

    fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
