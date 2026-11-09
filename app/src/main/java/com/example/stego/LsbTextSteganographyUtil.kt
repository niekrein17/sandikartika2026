package com.example.stego

import android.graphics.Bitmap
import java.nio.charset.StandardCharsets

/**
 * Utility class for hiding and retrieving encrypted text strings within the
 * Least Significant Bits (LSB) of Android Bitmap images.
 *
 * Employs 3-bit per pixel steganography (1 bit per RGB channel: Red LSB, Green LSB, Blue LSB)
 * ensuring zero visual degradation while maximizing text storage capacity.
 */
object LsbTextSteganographyUtil {

    private const val BITS_PER_PIXEL = 3 // 1 bit R, 1 bit G, 1 bit B
    private const val HEADER_SIZE_BYTES = 4 // 32-bit integer length prefix
    private const val MAX_ALLOWED_PAYLOAD_BYTES = 20_000_000 // 20 MB safety ceiling

    /**
     * Represents the outcome of an embedding or extraction operation.
     */
    sealed class StegoResult<out T> {
        data class Success<out T>(val data: T) : StegoResult<T>()
        data class Error(val message: String, val cause: Throwable? = null) : StegoResult<Nothing>()
    }

    /**
     * Calculates the maximum number of text bytes that can be embedded into the given Bitmap.
     *
     * @param bitmap The source carrier image
     * @return Maximum payload capacity in bytes (excluding length header)
     */
    fun calculateMaxCapacityBytes(bitmap: Bitmap): Int {
        val totalBits = bitmap.width.toLong() * bitmap.height.toLong() * BITS_PER_PIXEL
        val totalBytes = totalBits / 8
        return if (totalBytes > HEADER_SIZE_BYTES) (totalBytes - HEADER_SIZE_BYTES).toInt() else 0
    }

    /**
     * Estimates the maximum number of ASCII/Base64 characters that can fit into the Bitmap.
     *
     * @param bitmap The source carrier image
     * @return Character capacity count
     */
    fun getMaxTextCapacityChars(bitmap: Bitmap): Int {
        return calculateMaxCapacityBytes(bitmap)
    }

    /**
     * Checks whether an encrypted text string can fit inside the provided Bitmap.
     *
     * @param bitmap The candidate carrier image
     * @param encryptedText The text string to embed
     * @return True if the text fits within the image's LSB capacity, false otherwise
     */
    fun canFitText(bitmap: Bitmap, encryptedText: String): Boolean {
        val textBytes = encryptedText.toByteArray(StandardCharsets.UTF_8)
        return textBytes.size <= calculateMaxCapacityBytes(bitmap)
    }

    /**
     * Calculates the percentage ratio of capacity utilized by the text string (0.0 to 1.0).
     */
    fun calculateCapacityRatio(bitmap: Bitmap, encryptedText: String): Float {
        val maxCap = calculateMaxCapacityBytes(bitmap)
        if (maxCap <= 0) return 1.0f
        val textBytes = encryptedText.toByteArray(StandardCharsets.UTF_8)
        return (textBytes.size.toFloat() / maxCap.toFloat()).coerceIn(0.0f, 1.0f)
    }

    /**
     * Embeds an encrypted text string into the Least Significant Bits (LSB) of a Bitmap.
     *
     * @param sourceBitmap The original carrier image
     * @param encryptedText The encrypted string (e.g. Base64, Hex, or UTF-8 ciphertext)
     * @return A new mutable ARGB_8888 [Bitmap] containing the hidden encrypted text
     * @throws IllegalArgumentException if the text size exceeds the image capacity
     */
    @Throws(IllegalArgumentException::class)
    fun embedEncryptedText(sourceBitmap: Bitmap, encryptedText: String): Bitmap {
        val payloadBytes = encryptedText.toByteArray(StandardCharsets.UTF_8)
        val maxCapacity = calculateMaxCapacityBytes(sourceBitmap)

        if (payloadBytes.size > maxCapacity) {
            throw IllegalArgumentException(
                "Ukuran teks terenkripsi (${payloadBytes.size} bytes) melebihi kapasitas maksimum gambar (${maxCapacity} bytes)."
            )
        }

        val width = sourceBitmap.width
        val height = sourceBitmap.height

        // Ensure ARGB_8888 bitmap for deterministic lossless pixel manipulations
        val outputBitmap = if (sourceBitmap.config == Bitmap.Config.ARGB_8888 && sourceBitmap.isMutable) {
            sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val pixels = IntArray(width * height)
        outputBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Prepend 4-byte (32-bit) Big-Endian length header
        val payloadLen = payloadBytes.size
        val packet = ByteArray(HEADER_SIZE_BYTES + payloadLen)
        packet[0] = (payloadLen shr 24).toByte()
        packet[1] = (payloadLen shr 16).toByte()
        packet[2] = (payloadLen shr 8).toByte()
        packet[3] = payloadLen.toByte()
        System.arraycopy(payloadBytes, 0, packet, HEADER_SIZE_BYTES, payloadLen)

        val totalBits = packet.size * 8
        var bitIndex = 0
        var pixelIndex = 0

        while (bitIndex < totalBits && pixelIndex < pixels.size) {
            val pixel = pixels[pixelIndex]
            val a = (pixel shr 24) and 0xFF
            var r = (pixel shr 16) and 0xFF
            var g = (pixel shr 8) and 0xFF
            var b = pixel and 0xFF

            // Red Channel LSB
            if (bitIndex < totalBits) {
                val bytePos = bitIndex / 8
                val bitOffset = 7 - (bitIndex % 8)
                val bitVal = (packet[bytePos].toInt() shr bitOffset) and 1
                r = (r and 0xFE) or bitVal
                bitIndex++
            }

            // Green Channel LSB
            if (bitIndex < totalBits) {
                val bytePos = bitIndex / 8
                val bitOffset = 7 - (bitIndex % 8)
                val bitVal = (packet[bytePos].toInt() shr bitOffset) and 1
                g = (g and 0xFE) or bitVal
                bitIndex++
            }

            // Blue Channel LSB
            if (bitIndex < totalBits) {
                val bytePos = bitIndex / 8
                val bitOffset = 7 - (bitIndex % 8)
                val bitVal = (packet[bytePos].toInt() shr bitOffset) and 1
                b = (b and 0xFE) or bitVal
                bitIndex++
            }

            pixels[pixelIndex] = (a shl 24) or (r shl 16) or (g shl 8) or b
            pixelIndex++
        }

        outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return outputBitmap
    }

    /**
     * Extracts an encrypted text string hidden within the LSB bits of a stego Bitmap.
     *
     * @param stegoBitmap The image containing the hidden text
     * @return The extracted encrypted text string, or null if no valid payload is detected or length header is invalid
     */
    fun extractEncryptedText(stegoBitmap: Bitmap): String? {
        val width = stegoBitmap.width
        val height = stegoBitmap.height
        val totalPixels = width * height
        val pixels = IntArray(totalPixels)
        stegoBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val maxAllowedCapacity = ((totalPixels.toLong() * BITS_PER_PIXEL) / 8).toInt()

        // Read 32 bits (4 bytes) length prefix
        var payloadLength = 0
        var bitIndex = 0
        var pixelIndex = 0

        while (bitIndex < 32 && pixelIndex < totalPixels) {
            val pixel = pixels[pixelIndex]
            val rBit = (pixel shr 16) and 1
            val gBit = (pixel shr 8) and 1
            val bBit = pixel and 1

            if (bitIndex < 32) {
                payloadLength = (payloadLength shl 1) or rBit
                bitIndex++
            }
            if (bitIndex < 32) {
                payloadLength = (payloadLength shl 1) or gBit
                bitIndex++
            }
            if (bitIndex < 32) {
                payloadLength = (payloadLength shl 1) or bBit
                bitIndex++
            }
            pixelIndex++
        }

        // Validate payload length bounds
        if (payloadLength <= 0 || payloadLength > (maxAllowedCapacity - HEADER_SIZE_BYTES) || payloadLength > MAX_ALLOWED_PAYLOAD_BYTES) {
            return null
        }

        val payload = ByteArray(payloadLength)
        var payloadBitIndex = 0
        val totalPayloadBits = payloadLength * 8

        // Handle remaining bit from the 11th pixel (since 32 bits % 3 bits = 2 bits used in pixel 10, leaving 1 bit in Blue channel)
        val pixel10 = pixels[10]
        val leftoverBlueBit = pixel10 and 1
        val byte0 = payloadBitIndex / 8
        val bitOffset0 = 7 - (payloadBitIndex % 8)
        payload[byte0] = (payload[byte0].toInt() or (leftoverBlueBit shl bitOffset0)).toByte()
        payloadBitIndex++

        // Read remaining payload bits
        while (payloadBitIndex < totalPayloadBits && pixelIndex < totalPixels) {
            val pixel = pixels[pixelIndex]
            val rBit = (pixel shr 16) and 1
            val gBit = (pixel shr 8) and 1
            val bBit = pixel and 1

            if (payloadBitIndex < totalPayloadBits) {
                val bPos = payloadBitIndex / 8
                val bOffset = 7 - (payloadBitIndex % 8)
                payload[bPos] = (payload[bPos].toInt() or (rBit shl bOffset)).toByte()
                payloadBitIndex++
            }
            if (payloadBitIndex < totalPayloadBits) {
                val bPos = payloadBitIndex / 8
                val bOffset = 7 - (payloadBitIndex % 8)
                payload[bPos] = (payload[bPos].toInt() or (gBit shl bOffset)).toByte()
                payloadBitIndex++
            }
            if (payloadBitIndex < totalPayloadBits) {
                val bPos = payloadBitIndex / 8
                val bOffset = 7 - (payloadBitIndex % 8)
                payload[bPos] = (payload[bPos].toInt() or (bBit shl bOffset)).toByte()
                payloadBitIndex++
            }
            pixelIndex++
        }

        return try {
            String(payload, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Safe wrapper for embedding text that catches exceptions and returns [StegoResult].
     */
    fun embedEncryptedTextSafe(sourceBitmap: Bitmap, encryptedText: String): StegoResult<Bitmap> {
        return try {
            val resultBitmap = embedEncryptedText(sourceBitmap, encryptedText)
            StegoResult.Success(resultBitmap)
        } catch (e: Exception) {
            StegoResult.Error(e.message ?: "Gagal menyisipkan teks ke citra", e)
        }
    }

    /**
     * Safe wrapper for extracting text that returns [StegoResult].
     */
    fun extractEncryptedTextSafe(stegoBitmap: Bitmap): StegoResult<String> {
        val extracted = extractEncryptedText(stegoBitmap)
        return if (extracted != null) {
            StegoResult.Success(extracted)
        } else {
            StegoResult.Error("Tidak ditemukan teks terenkripsi valid dalam citra ini.")
        }
    }
}
