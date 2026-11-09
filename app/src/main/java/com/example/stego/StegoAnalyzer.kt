package com.example.stego

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

data class StegoAnalysisReport(
    val width: Int,
    val height: Int,
    val totalPixels: Int,
    val maxCapacityBytes: Int,
    val detectedPayloadBytes: Int?,
    val usagePercentage: Float,
    val stealthScore: String,
    val recommendation: String
)

object StegoAnalyzer {

    /**
     * Extracts a single bit-plane (0 to 7) from the red or luminance channel of a bitmap
     * to visualize noise vs structural data.
     */
    fun extractBitPlaneBitmap(source: Bitmap, bitPlaneIndex: Int): Bitmap {
        val w = source.width
        val h = source.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        val outPixels = IntArray(w * h)
        val mask = 1 shl bitPlaneIndex.coerceIn(0, 7)

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val bit = if ((r and mask) != 0) 255 else 0
            outPixels[i] = (0xFF shl 24) or (bit shl 16) or (bit shl 8) or bit
        }

        output.setPixels(outPixels, 0, w, 0, 0, w, h)
        return output
    }

    /**
     * Generates an amplified difference map between original and stego bitmap.
     * Highlights pixels that were altered by LSB insertion in bright green/cyan.
     */
    fun generateAmplifiedDiffMap(original: Bitmap, stego: Bitmap): Bitmap? {
        if (original.width != stego.width || original.height != stego.height) return null

        val w = original.width
        val h = original.height
        val diffBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        val origPixels = IntArray(w * h)
        val stegoPixels = IntArray(w * h)
        original.getPixels(origPixels, 0, w, 0, 0, w, h)
        stego.getPixels(stegoPixels, 0, w, 0, 0, w, h)

        val diffPixels = IntArray(w * h)
        for (i in origPixels.indices) {
            val op = origPixels[i]
            val sp = stegoPixels[i]

            val dr = abs(((op shr 16) and 0xFF) - ((sp shr 16) and 0xFF))
            val dg = abs(((op shr 8) and 0xFF) - ((sp shr 8) and 0xFF))
            val db = abs((op and 0xFF) - (sp and 0xFF))

            if (dr > 0 || dg > 0 || db > 0) {
                // Modified pixel highlighted with cyber emerald
                diffPixels[i] = Color.argb(255, 46, 229, 157)
            } else {
                // Dim dark slate for untouched
                diffPixels[i] = Color.argb(255, 12, 18, 15)
            }
        }

        diffBitmap.setPixels(diffPixels, 0, w, 0, 0, w, h)
        return diffBitmap
    }

    /**
     * Generates an operational analysis report on a given carrier image.
     */
    fun analyzeCarrier(bitmap: Bitmap, payloadSizeBytes: Int? = null): StegoAnalysisReport {
        val w = bitmap.width
        val h = bitmap.height
        val totalPixels = w * h
        val maxCap = StegoEngine.calculateMaxCapacityBytes(w, h)

        val used = payloadSizeBytes ?: 0
        val ratio = if (maxCap > 0) (used.toFloat() / maxCap.toFloat()) * 100f else 0f

        val (score, rec) = when {
            ratio == 0f -> Pair("SIAP PAKAI (100% STERIL)", "Carrier dalam kondisi bersih. Siap disisipi instruksi rahasia.")
            ratio < 2.0f -> Pair("KAMUFLASE PRIMA (99.8%)", "Densitas bit sangat rendah. Secara visual dan statistik tidak dapat dibedakan dari noise wajar.")
            ratio < 10.0f -> Pair("KAMUFLASE BAIK (98.5%)", "Sangat aman untuk transmisi saluran publik.")
            ratio < 40.0f -> Pair("KAMUFLASE SEDANG (92.0%)", "Aman untuk media beresolusi tinggi dengan tekstur alam/banyak dedaunan.")
            else -> Pair("DENSITAS TINGGI (84.0%)", "Disarankan menggunakan citra carrier dengan resolusi lebih tinggi.")
        }

        return StegoAnalysisReport(
            width = w,
            height = h,
            totalPixels = totalPixels,
            maxCapacityBytes = maxCap,
            detectedPayloadBytes = payloadSizeBytes,
            usagePercentage = ratio,
            stealthScore = score,
            recommendation = rec
        )
    }
}
