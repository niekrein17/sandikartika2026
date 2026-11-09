package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.CryptoEngine
import com.example.data.model.ExtractionStatus
import com.example.data.model.PayloadClassification
import com.example.data.model.SecretPayload
import com.example.data.model.StegoTransmissionMode
import com.example.stego.DctStegoEngine
import com.example.stego.ReedSolomonCodec
import com.example.stego.StegoEngine
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SANDI-KARTIKA", appName)
    }

    @Test
    fun `reed solomon codec recovers up to 30 corrupted bytes per 200-byte codeword`() {
        val original = ByteArray(350) { i -> ((i * 73 + 19) and 0xFF).toByte() }
        val encoded = ReedSolomonCodec.encodeWithFec(
            payload = original,
            dataShardSize = 140,
            parityShardSize = 60
        )

        // Corrupt 45 bytes across the interleaved payload body (after 16-byte RS header)
        val corrupted = encoded.copyOf()
        for (i in 0 until 45) {
            val pos = 16 + (i * 7) % (corrupted.size - 16)
            corrupted[pos] = (corrupted[pos].toInt() xor 0xA5).toByte()
        }

        val decodeResult = ReedSolomonCodec.decodeWithFec(corrupted)
        assertNotNull("FEC decode result must not be null", decodeResult)
        assertArrayEquals("Payload must be 100% reconstructed by Reed-Solomon FEC", original, decodeResult!!.data)
        assertTrue("Corrected bytes should be > 0", decodeResult.correctedBytesCount > 0)
        assertEquals(30, decodeResult.parityRatioPercent)
    }

    @Test
    fun `tactical robust dct stego survives lossy jpeg q80 compression for master and decoy`() {
        val carrier = StegoEngine.generatePresetBitmap(presetIndex = 3)
        val masterPayload = SecretPayload(
            type = "REAL",
            title = "OP-HARIMAU // SEKTOR UTARA",
            message = "SERANGAN FAJAR PUKUL 0430 WIB DI SEKTOR UTARA. JAGA RADIO SILENCE.",
            coordinates = "CO 48M 70450 28910",
            classification = PayloadClassification.TOP_SECRET
        )
        val decoyPayload = SecretPayload(
            type = "DECOY",
            title = "LAPORAN LOGISTIK HARIAN",
            message = "Distribusi ransum T-2 sejumlah 40 dus telah tiba di pos komando taktis.",
            coordinates = "POSKO LOGISTIK",
            classification = PayloadClassification.DECOY_ROUTINE
        )

        val encryptedPackage = CryptoEngine.buildEncryptedPackage(
            masterPayload = masterPayload,
            masterPassword = "SANDI-KOMANDO-99",
            decoyPayload = decoyPayload,
            decoyPassword = "SANDI-DARURAT-00"
        )

        // Embed using TACTICAL_ROBUST_DCT (8x8 DCT Mid-Frequency QIM + Reed-Solomon FEC)
        val stegoBmp = StegoEngine.embedPayload(
            sourceBitmap = carrier,
            payload = encryptedPackage,
            mode = StegoTransmissionMode.TACTICAL_ROBUST_DCT
        )

        // Pass through lossy JPEG Q=80 re-compression pipeline
        val lossyJpegBmp = DctStegoEngine.simulateLossyJpegCompression(stegoBmp, quality = 80)

        // Extract and verify auto-detection + Master & Decoy decryption
        val extracted = StegoEngine.extractPayloadWithMeta(lossyJpegBmp)
        assertNotNull("Extracted stego packet after JPEG Q=80 compression must not be null", extracted)
        assertEquals(StegoTransmissionMode.TACTICAL_ROBUST_DCT, extracted!!.detectedMode)

        val masterResult = CryptoEngine.decryptPackage(
            packageBytes = extracted.payloadBytes,
            password = "SANDI-KOMANDO-99"
        )
        assertEquals(ExtractionStatus.SUCCESS_MASTER, masterResult.status)
        assertFalse(masterResult.isDecoy)
        assertEquals(masterPayload.message, masterResult.payload?.message)
        assertEquals(masterPayload.coordinates, masterResult.payload?.coordinates)

        val decoyResult = CryptoEngine.decryptPackage(
            packageBytes = extracted.payloadBytes,
            password = "SANDI-DARURAT-00"
        )
        assertEquals(ExtractionStatus.SUCCESS_DECOY, decoyResult.status)
        assertTrue(decoyResult.isDecoy)
        assertEquals(decoyPayload.message, decoyResult.payload?.message)
    }

    @Test
    fun `high capacity lossless lsb roundtrip succeeds`() {
        val carrier = StegoEngine.generatePresetBitmap(presetIndex = 0)
        val masterPayload = SecretPayload(
            type = "REAL",
            title = "DOKUMEN OPERASI",
            message = "DOKUMEN OPERASI LENGKAP TRANSMISI LOSSLESS PNG.",
            coordinates = "CO 48M 71200 29400",
            classification = PayloadClassification.TOP_SECRET
        )
        val decoyPayload = SecretPayload(
            type = "DECOY",
            title = "CUACA HARIAN",
            message = "Kondisi cuaca cerah berawan, jarak pandang 5 km.",
            classification = PayloadClassification.DECOY_ROUTINE
        )

        val encryptedPackage = CryptoEngine.buildEncryptedPackage(
            masterPayload = masterPayload,
            masterPassword = "MASTER-LSB-123",
            decoyPayload = decoyPayload,
            decoyPassword = "DECOY-LSB-456"
        )

        val stegoBmp = StegoEngine.embedPayload(
            sourceBitmap = carrier,
            payload = encryptedPackage,
            mode = StegoTransmissionMode.HIGH_CAPACITY_LSB
        )

        val extracted = StegoEngine.extractPayloadWithMeta(stegoBmp)
        assertNotNull("Extracted LSB packet must not be null", extracted)
        assertEquals(StegoTransmissionMode.HIGH_CAPACITY_LSB, extracted!!.detectedMode)

        val masterResult = CryptoEngine.decryptPackage(
            packageBytes = extracted.payloadBytes,
            password = "MASTER-LSB-123"
        )
        assertEquals(ExtractionStatus.SUCCESS_MASTER, masterResult.status)
        assertEquals(masterPayload.message, masterResult.payload?.message)
    }
}
