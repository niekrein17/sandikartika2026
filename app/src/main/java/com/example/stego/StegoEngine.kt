package com.example.stego

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.crypto.CryptoEngine
import com.example.data.model.StegoTransmissionMode
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

object StegoEngine {

    const val BITS_PER_PIXEL = 3 // 1 bit in Red, 1 bit in Green, 1 bit in Blue

    data class ExtractedStegoPacket(
        val payloadBytes: ByteArray,
        val detectedMode: StegoTransmissionMode,
        val fecCorrectedBytes: Int = 0,
        val fecParityPercent: Int = 0
    )

    /**
     * Embeds an encrypted text string into a Bitmap via LsbTextSteganographyUtil.
     */
    fun embedText(sourceBitmap: Bitmap, encryptedText: String): Bitmap {
        return LsbTextSteganographyUtil.embedEncryptedText(sourceBitmap, encryptedText)
    }

    /**
     * Extracts an encrypted text string from a Bitmap via LsbTextSteganographyUtil.
     */
    fun extractText(stegoBitmap: Bitmap): String? {
        return LsbTextSteganographyUtil.extractEncryptedText(stegoBitmap)
    }

    /**
     * Calculates the maximum payload capacity in bytes for a given image resolution and transmission mode.
     */
    fun calculateMaxCapacityBytes(
        width: Int,
        height: Int,
        mode: StegoTransmissionMode = StegoTransmissionMode.HIGH_CAPACITY_LSB
    ): Int {
        return when (mode) {
            StegoTransmissionMode.HIGH_CAPACITY_LSB -> {
                val totalBits = (width.toLong() * height.toLong() * BITS_PER_PIXEL)
                val totalBytes = totalBits / 8
                if (totalBytes > 4) (totalBytes - 4).toInt() else 0
            }
            StegoTransmissionMode.TACTICAL_ROBUST_DCT -> {
                DctStegoEngine.calculateMaxNetCapacityBytes(width, height)
            }
        }
    }

    /**
     * Embeds payload bytes into a bitmap using the selected adaptive operational mode:
     * - HIGH_CAPACITY_LSB: 3-bit spatial RGB LSB for large lossless PNG payloads.
     * - TACTICAL_ROBUST_DCT: 8x8 2D-DCT Mid-Frequency QIM + Reed-Solomon GF(2^8) FEC for lossy-resistant payloads.
     */
    fun embedPayload(
        sourceBitmap: Bitmap,
        payload: ByteArray,
        mode: StegoTransmissionMode = StegoTransmissionMode.HIGH_CAPACITY_LSB
    ): Bitmap {
        return when (mode) {
            StegoTransmissionMode.HIGH_CAPACITY_LSB -> embedLsbPayload(sourceBitmap, payload)
            StegoTransmissionMode.TACTICAL_ROBUST_DCT -> DctStegoEngine.embedRobustPayload(sourceBitmap, payload)
        }
    }

    /**
     * Embeds payload bytes into a bitmap using 3-bit LSB (Least Significant Bit) steganography.
     * Returns a new mutable ARGB_8888 bitmap with the hidden payload.
     */
    fun embedLsbPayload(sourceBitmap: Bitmap, payload: ByteArray): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val maxCapacity = calculateMaxCapacityBytes(width, height, StegoTransmissionMode.HIGH_CAPACITY_LSB)

        if (payload.size > maxCapacity) {
            throw IllegalArgumentException(
                "Ukuran muatan (${payload.size} B) melebihi kapasitas maksimum citra (${maxCapacity} B)."
            )
        }

        // Output bitmap must be ARGB_8888 for lossless pixel manipulation
        val outputBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(width * height)
        outputBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Prepend 4-byte length prefix (big endian)
        val payloadLen = payload.size
        val fullData = ByteArray(4 + payloadLen)
        fullData[0] = (payloadLen shr 24).toByte()
        fullData[1] = (payloadLen shr 16).toByte()
        fullData[2] = (payloadLen shr 8).toByte()
        fullData[3] = payloadLen.toByte()
        System.arraycopy(payload, 0, fullData, 4, payloadLen)

        val totalBits = fullData.size * 8
        var bitIndex = 0

        var pixelIdx = 0
        while (bitIndex < totalBits && pixelIdx < pixels.size) {
            val p = pixels[pixelIdx]
            val a = (p shr 24) and 0xFF
            var r = (p shr 16) and 0xFF
            var g = (p shr 8) and 0xFF
            var b = p and 0xFF

            // Red channel LSB
            if (bitIndex < totalBits) {
                val bytePos = bitIndex / 8
                val bitPos = 7 - (bitIndex % 8)
                val bitVal = (fullData[bytePos].toInt() shr bitPos) and 1
                r = (r and 0xFE) or bitVal
                bitIndex++
            }

            // Green channel LSB
            if (bitIndex < totalBits) {
                val bytePos = bitIndex / 8
                val bitPos = 7 - (bitIndex % 8)
                val bitVal = (fullData[bytePos].toInt() shr bitPos) and 1
                g = (g and 0xFE) or bitVal
                bitIndex++
            }

            // Blue channel LSB
            if (bitIndex < totalBits) {
                val bytePos = bitIndex / 8
                val bitPos = 7 - (bitIndex % 8)
                val bitVal = (fullData[bytePos].toInt() shr bitPos) and 1
                b = (b and 0xFE) or bitVal
                bitIndex++
            }

            pixels[pixelIdx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            pixelIdx++
        }

        outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return outputBitmap
    }

    /**
     * Automatically probes and extracts hidden payload from either:
     * 1. HIGH_CAPACITY_LSB (3-channel spatial LSB)
     * 2. TACTICAL_ROBUST_DCT (8x8 DCT Mid-Frequency QIM + Reed-Solomon FEC)
     */
    fun extractPayloadWithMeta(stegoBitmap: Bitmap): ExtractedStegoPacket? {
        // 1. Probe spatial LSB first (very fast O(1) header check)
        val lsbBytes = extractLsbPayload(stegoBitmap)
        if (lsbBytes != null && lsbBytes.size >= 4 &&
            lsbBytes[0] == CryptoEngine.MAGIC_HEADER[0] &&
            lsbBytes[1] == CryptoEngine.MAGIC_HEADER[1] &&
            lsbBytes[2] == CryptoEngine.MAGIC_HEADER[2] &&
            lsbBytes[3] == CryptoEngine.MAGIC_HEADER[3]
        ) {
            return ExtractedStegoPacket(
                payloadBytes = lsbBytes,
                detectedMode = StegoTransmissionMode.HIGH_CAPACITY_LSB,
                fecCorrectedBytes = 0,
                fecParityPercent = 0
            )
        }

        // 2. Probe Frequency-Domain DCT 8x8 + Reed-Solomon FEC
        val dctFecResult = DctStegoEngine.extractRobustPayload(stegoBitmap)
        if (dctFecResult != null && dctFecResult.data.size >= 4 &&
            dctFecResult.data[0] == CryptoEngine.MAGIC_HEADER[0] &&
            dctFecResult.data[1] == CryptoEngine.MAGIC_HEADER[1] &&
            dctFecResult.data[2] == CryptoEngine.MAGIC_HEADER[2] &&
            dctFecResult.data[3] == CryptoEngine.MAGIC_HEADER[3]
        ) {
            return ExtractedStegoPacket(
                payloadBytes = dctFecResult.data,
                detectedMode = StegoTransmissionMode.TACTICAL_ROBUST_DCT,
                fecCorrectedBytes = dctFecResult.correctedBytesCount,
                fecParityPercent = dctFecResult.parityRatioPercent
            )
        }

        // Fallback if custom non-SDKR payload was embedded in LSB
        if (lsbBytes != null) {
            return ExtractedStegoPacket(
                payloadBytes = lsbBytes,
                detectedMode = StegoTransmissionMode.HIGH_CAPACITY_LSB
            )
        }

        return null
    }

    /**
     * Extracts payload bytes from a stego bitmap (auto-detecting LSB or DCT+RS).
     */
    fun extractPayload(stegoBitmap: Bitmap): ByteArray? {
        return extractPayloadWithMeta(stegoBitmap)?.payloadBytes
    }

    /**
     * Extracts payload bytes strictly from spatial 3-channel LSB.
     */
    fun extractLsbPayload(stegoBitmap: Bitmap): ByteArray? {
        val width = stegoBitmap.width
        val height = stegoBitmap.height
        val pixels = IntArray(width * height)
        stegoBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val totalPixels = pixels.size
        val maxPossibleBytes = (totalPixels.toLong() * BITS_PER_PIXEL / 8).toInt()

        // Read 32 bits (4 bytes) for length
        var payloadLength = 0
        var bitIndex = 0
        var pixelIdx = 0

        while (bitIndex < 32 && pixelIdx < totalPixels) {
            val p = pixels[pixelIdx]
            val rBit = ((p shr 16) and 1)
            val gBit = ((p shr 8) and 1)
            val bBit = (p and 1)

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
            pixelIdx++
        }

        // Validate payload length bounds
        if (payloadLength <= 0 || payloadLength > (maxPossibleBytes - 4) || payloadLength > 15_000_000) {
            return null
        }

        val payload = ByteArray(payloadLength)
        var payloadBitIdx = 0
        val totalPayloadBits = payloadLength * 8

        val lastP = pixels[10] // the 11th pixel (0-indexed 10)
        val leftoverBit = (lastP and 1)
        val bytePos0 = payloadBitIdx / 8
        val bitPos0 = 7 - (payloadBitIdx % 8)
        payload[bytePos0] = (payload[bytePos0].toInt() or (leftoverBit shl bitPos0)).toByte()
        payloadBitIdx++

        while (payloadBitIdx < totalPayloadBits && pixelIdx < totalPixels) {
            val p = pixels[pixelIdx]
            val rBit = ((p shr 16) and 1)
            val gBit = ((p shr 8) and 1)
            val bBit = (p and 1)

            if (payloadBitIdx < totalPayloadBits) {
                val bP = payloadBitIdx / 8
                val bOffset = 7 - (payloadBitIdx % 8)
                payload[bP] = (payload[bP].toInt() or (rBit shl bOffset)).toByte()
                payloadBitIdx++
            }
            if (payloadBitIdx < totalPayloadBits) {
                val bP = payloadBitIdx / 8
                val bOffset = 7 - (payloadBitIdx % 8)
                payload[bP] = (payload[bP].toInt() or (gBit shl bOffset)).toByte()
                payloadBitIdx++
            }
            if (payloadBitIdx < totalPayloadBits) {
                val bP = payloadBitIdx / 8
                val bOffset = 7 - (payloadBitIdx % 8)
                payload[bP] = (payload[bP].toInt() or (bBit shl bOffset)).toByte()
                payloadBitIdx++
            }
            pixelIdx++
        }

        return payload
    }

    /**
     * Saves the stego bitmap to a lossless PNG file in cache and returns its File and FileProvider Uri.
     */
    fun saveLosslessStegoImage(context: Context, bitmap: Bitmap, fileNamePrefix: String = "SANDI_STEGO"): Pair<File, Uri> {
        val stegoDir = File(context.cacheDir, "stego_images").apply { mkdirs() }
        val timestamp = System.currentTimeMillis()
        val file = File(stegoDir, "${fileNamePrefix}_$timestamp.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }

        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        return Pair(file, uri)
    }

    // ==========================================
    // Tactical Carrier Presets (High Polish Procedural, 640x640 = 80x80 8x8 DCT blocks)
    // ==========================================
    fun generatePresetBitmap(presetIndex: Int, width: Int = 640, height: Int = 640): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (presetIndex) {
            0 -> drawForestRecon(canvas, width, height, paint)
            1 -> drawBorderOutpost(canvas, width, height, paint)
            2 -> drawLogisticsDepot(canvas, width, height, paint)
            else -> drawTopographicGrid(canvas, width, height, paint)
        }

        return bitmap
    }

    private fun drawForestRecon(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        paint.shader = LinearGradient(0f, 0f, 0f, h * 0.5f, Color.rgb(24, 44, 54), Color.rgb(74, 98, 70), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        val mountainPath = Path().apply {
            moveTo(0f, h * 0.45f)
            lineTo(w * 0.25f, h * 0.32f)
            lineTo(w * 0.55f, h * 0.38f)
            lineTo(w * 0.8f, h * 0.28f)
            lineTo(w.toFloat(), h * 0.42f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        paint.color = Color.rgb(36, 58, 48)
        canvas.drawPath(mountainPath, paint)

        val ridgePath = Path().apply {
            moveTo(0f, h * 0.52f)
            lineTo(w * 0.4f, h * 0.46f)
            lineTo(w * 0.75f, h * 0.5f)
            lineTo(w.toFloat(), h * 0.47f)
            lineTo(w.toFloat(), h.toFloat())
            lineTo(0f, h.toFloat())
            close()
        }
        paint.color = Color.rgb(28, 48, 38)
        canvas.drawPath(ridgePath, paint)

        paint.color = Color.rgb(22, 38, 28)
        for (i in 0 until 18) {
            val tx = (w * 0.05f) + (i * w * 0.055f)
            val ty = h * (0.6f + (i % 5) * 0.06f)
            val treeW = w * 0.08f
            val treeH = h * 0.3f
            val treePath = Path().apply {
                moveTo(tx, ty - treeH)
                lineTo(tx + treeW / 2, ty)
                lineTo(tx - treeW / 2, ty)
                close()
            }
            canvas.drawPath(treePath, paint)
        }

        paint.color = Color.rgb(42, 80, 96)
        paint.strokeWidth = 24f
        paint.style = Paint.Style.STROKE
        val riverPath = Path().apply {
            moveTo(w * 0.2f, h.toFloat())
            cubicTo(w * 0.35f, h * 0.85f, w * 0.5f, h * 0.75f, w * 0.85f, h * 0.65f)
        }
        canvas.drawPath(riverPath, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawBorderOutpost(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        paint.shader = LinearGradient(0f, 0f, 0f, h * 0.6f, Color.rgb(30, 36, 52), Color.rgb(165, 85, 45), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        paint.color = Color.rgb(225, 175, 70)
        canvas.drawCircle(w * 0.75f, h * 0.38f, w * 0.12f, paint)

        paint.color = Color.rgb(28, 32, 36)
        canvas.drawRect(0f, h * 0.65f, w.toFloat(), h.toFloat(), paint)

        paint.color = Color.rgb(22, 24, 28)
        val twX = w * 0.3f
        val twY = h * 0.65f
        paint.strokeWidth = 6f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(twX, twY, twX + 30f, twY - 180f, paint)
        canvas.drawLine(twX + 80f, twY, twX + 50f, twY - 180f, paint)
        canvas.drawLine(twX + 10f, twY - 90f, twX + 70f, twY - 90f, paint)
        paint.style = Paint.Style.FILL

        canvas.drawRect(twX + 15f, twY - 230f, twX + 65f, twY - 180f, paint)
        val roof = Path().apply {
            moveTo(twX + 5f, twY - 230f)
            lineTo(twX + 40f, twY - 255f)
            lineTo(twX + 75f, twY - 230f)
            close()
        }
        canvas.drawPath(roof, paint)

        canvas.drawLine(twX + 40f, twY - 255f, twX + 40f, twY - 295f, paint)
        paint.color = Color.rgb(220, 80, 80)
        canvas.drawCircle(twX + 40f, twY - 295f, 4f, paint)
    }

    private fun drawLogisticsDepot(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        paint.color = Color.rgb(35, 40, 44)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        paint.color = Color.rgb(26, 30, 34)
        paint.strokeWidth = 14f
        paint.style = Paint.Style.STROKE
        for (i in 0..6) {
            canvas.drawLine(0f, i * (h * 0.08f), w.toFloat(), i * (h * 0.08f) + 40f, paint)
        }
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(48, 52, 56)
        canvas.drawRect(0f, h * 0.55f, w.toFloat(), h.toFloat(), paint)

        val crateColors = intArrayOf(Color.rgb(85, 75, 50), Color.rgb(70, 85, 60), Color.rgb(60, 68, 55))
        for (row in 0..2) {
            for (col in 0..3) {
                paint.color = crateColors[(row + col) % crateColors.size]
                val cw = w * 0.18f
                val ch = h * 0.14f
                val cx = w * 0.12f + col * (cw + 12f)
                val cy = h * 0.58f + row * (ch + 10f)
                canvas.drawRect(cx, cy, cx + cw, cy + ch, paint)

                paint.color = Color.rgb(180, 170, 130)
                paint.textSize = 14f
                canvas.drawText("PBN-24", cx + 8f, cy + ch * 0.6f, paint)
            }
        }
    }

    private fun drawTopographicGrid(canvas: Canvas, w: Int, h: Int, paint: Paint) {
        paint.color = Color.rgb(22, 32, 38)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        for (r in 1..10) {
            paint.color = if (r % 3 == 0) Color.rgb(75, 140, 160) else Color.rgb(35, 70, 85)
            val path = Path()
            val radius = r * (w * 0.045f)
            val cx = w * 0.45f
            val cy = h * 0.45f
            for (angle in 0..360 step 10) {
                val rad = Math.toRadians(angle.toDouble())
                val noise = sin(angle * 0.08) * 16.0
                val px = cx + (radius + noise) * Math.cos(rad)
                val py = cy + (radius + noise) * Math.sin(rad)
                if (angle == 0) path.moveTo(px.toFloat(), py.toFloat()) else path.lineTo(px.toFloat(), py.toFloat())
            }
            path.close()
            canvas.drawPath(path, paint)
        }

        paint.color = Color.rgb(32, 52, 62)
        paint.strokeWidth = 1f
        for (x in 0..w step (w / 8)) {
            canvas.drawLine(x.toFloat(), 0f, x.toFloat(), h.toFloat(), paint)
        }
        for (y in 0..h step (h / 8)) {
            canvas.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), paint)
        }

        paint.color = Color.rgb(46, 229, 157)
        paint.strokeWidth = 2.5f
        val tx = w * 0.45f
        val ty = h * 0.45f
        canvas.drawLine(tx - 35f, ty, tx + 35f, ty, paint)
        canvas.drawLine(tx, ty - 35f, tx, ty + 35f, paint)
        canvas.drawCircle(tx, ty, 24f, paint)

        paint.style = Paint.Style.FILL
    }
}
