package com.example.stego

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Frequency-Domain Steganography Engine using 8x8 2D Discrete Cosine Transform (DCT)
 * Mid-Frequency Quantization Index Modulation (QIM) coupled with Reed-Solomon GF(2^8) FEC.
 *
 * Designed for PUSSIBERAD tactical communications to survive lossy JPEG/WebP compression
 * over VHF/HF tactical data links, MMS, and instant messaging channels.
 */
object DctStegoEngine {

    private const val BLOCK_SIZE = 8
    const val QUALITY_FACTOR_BASIS = 75

    /**
     * Standard ITU-T T.81 / JPEG Annex K Luminance Quantization Matrix (Q = 50).
     */
    private val JPEG_LUMINANCE_QUANT_Q50 = arrayOf(
        intArrayOf(16, 11, 10, 16, 24, 40, 51, 61),
        intArrayOf(12, 12, 14, 19, 26, 58, 60, 55),
        intArrayOf(14, 13, 16, 24, 40, 57, 69, 56),
        intArrayOf(14, 17, 22, 29, 51, 87, 80, 62),
        intArrayOf(18, 22, 37, 56, 68, 109, 103, 77),
        intArrayOf(24, 35, 55, 64, 81, 104, 113, 92),
        intArrayOf(49, 64, 78, 87, 103, 121, 120, 101),
        intArrayOf(72, 92, 95, 98, 112, 100, 103, 99)
    )

    /**
     * Normalized JPEG Quantization Matrix for Q = 75 (Scale factor = 200 - 2*75 = 50%).
     */
    val JPEG_QUANT_Q75 = Array(BLOCK_SIZE) { u ->
        DoubleArray(BLOCK_SIZE) { v ->
            val q50 = JPEG_LUMINANCE_QUANT_Q50[u][v]
            val scale = 200 - 2 * QUALITY_FACTOR_BASIS
            max(1, (q50 * scale + 50) / 100).toDouble()
        }
    }

    /**
     * 10 Strictly Mid-Frequency AC coefficient coordinates (u, v) in each 8x8 DCT block (2 <= u + v <= 4).
     * Avoids DC (0,0) and ultra-low frequencies (0,1)/(1,0) to prevent visible block artifacts,
     * and avoids high frequencies (u + v >= 5, Q50 >= 37) which are aggressively truncated by JPEG quantization.
     */
    val MID_FREQ_COORDS = arrayOf(
        intArrayOf(0, 2),
        intArrayOf(1, 1),
        intArrayOf(2, 0),
        intArrayOf(0, 3),
        intArrayOf(1, 2),
        intArrayOf(2, 1),
        intArrayOf(3, 0),
        intArrayOf(1, 3),
        intArrayOf(2, 2),
        intArrayOf(3, 1)
    )

    val BITS_PER_BLOCK = MID_FREQ_COORDS.size // 10 bits per 8x8 block

    /**
     * Converts RGB [0..255] to ITU-R BT.601 YCbCr (Y in [0..255], Cb/Cr centered at 128.0).
     */
    fun rgbToYCbCr(r: Int, g: Int, b: Int): DoubleArray {
        val y = 0.299 * r + 0.587 * g + 0.114 * b
        val cb = 128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b
        val cr = 128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b
        return doubleArrayOf(y, cb, cr)
    }

    /**
     * Converts ITU-R BT.601 YCbCr back to clamped RGB [0..255].
     */
    fun yCbCrToRgb(y: Double, cb: Double, cr: Double): IntArray {
        val cbShift = cb - 128.0
        val crShift = cr - 128.0
        val r = (y + 1.402 * crShift).roundToInt().coerceIn(0, 255)
        val g = (y - 0.344136 * cbShift - 0.714136 * crShift).roundToInt().coerceIn(0, 255)
        val b = (y + 1.772 * cbShift).roundToInt().coerceIn(0, 255)
        return intArrayOf(r, g, b)
    }

    /**
     * Precomputed Orthonormal 1D DCT-II Transform Matrix T (8x8) and its Transpose T^T.
     * Forward 2D-DCT: D = T * B * T^T
     * Inverse 2D-IDCT: B = T^T * D * T
     */
    private val DCT_MATRIX = Array(BLOCK_SIZE) { u ->
        val alpha = if (u == 0) sqrt(1.0 / BLOCK_SIZE) else sqrt(2.0 / BLOCK_SIZE)
        DoubleArray(BLOCK_SIZE) { x ->
            alpha * cos(((2 * x + 1) * u * PI) / (2.0 * BLOCK_SIZE))
        }
    }

    private val DCT_MATRIX_T = Array(BLOCK_SIZE) { x ->
        DoubleArray(BLOCK_SIZE) { u ->
            DCT_MATRIX[u][x]
        }
    }

    /**
     * Computes the QIM quantization step Delta(u, v) normalized by the JPEG Q75 table.
     * Ensures sufficient decision margin (> 3.6x Q75) to survive lossy JPEG/WebP re-quantization.
     */
    private fun getQimStep(u: Int, v: Int): Double {
        return max(32.0, JPEG_QUANT_Q75[u][v] * 3.6)
    }

    /**
     * Calculates the maximum raw and net (FEC-adjusted) byte capacity for DCT + Reed-Solomon mode.
     */
    fun calculateMaxNetCapacityBytes(width: Int, height: Int): Int {
        val blocksX = width / BLOCK_SIZE
        val blocksY = height / BLOCK_SIZE
        val totalBlocks = blocksX * blocksY
        val totalRawBits = totalBlocks * BITS_PER_BLOCK
        val totalRawBytes = totalRawBits / 8

        if (totalRawBytes <= ReedSolomonCodec.HEADER_TOTAL_LEN) return 0

        val bodyBytes = totalRawBytes - ReedSolomonCodec.HEADER_TOTAL_LEN
        val codewordLen = ReedSolomonCodec.DEFAULT_DATA_SHARD + ReedSolomonCodec.DEFAULT_PARITY_SHARD
        val fullCodewords = bodyBytes / codewordLen
        val netCapacity = fullCodewords * ReedSolomonCodec.DEFAULT_DATA_SHARD
        return max(0, netCapacity)
    }

    /**
     * Calculates the raw bit/byte capacity of the 8x8 DCT mid-frequency band before FEC parity.
     */
    fun calculateRawDctCapacityBytes(width: Int, height: Int): Int {
        val blocksX = width / BLOCK_SIZE
        val blocksY = height / BLOCK_SIZE
        return (blocksX * blocksY * BITS_PER_BLOCK) / 8
    }

    /**
     * Fast separable 2D-DCT on an 8x8 block: D = T * B * T^T
     */
    private fun forwardDct8x8(spatial: Array<DoubleArray>): Array<DoubleArray> {
        val temp = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }
        val freq = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }

        // temp = T * spatial
        for (i in 0 until BLOCK_SIZE) {
            for (j in 0 until BLOCK_SIZE) {
                var sum = 0.0
                for (k in 0 until BLOCK_SIZE) {
                    sum += DCT_MATRIX[i][k] * spatial[k][j]
                }
                temp[i][j] = sum
            }
        }

        // freq = temp * T^T
        for (i in 0 until BLOCK_SIZE) {
            for (j in 0 until BLOCK_SIZE) {
                var sum = 0.0
                for (k in 0 until BLOCK_SIZE) {
                    sum += temp[i][k] * DCT_MATRIX_T[k][j]
                }
                freq[i][j] = sum
            }
        }
        return freq
    }

    /**
     * Fast separable 2D-IDCT on an 8x8 block: B = T^T * D * T
     */
    private fun inverseDct8x8(freq: Array<DoubleArray>): Array<DoubleArray> {
        val temp = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }
        val spatial = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }

        // temp = T^T * freq
        for (i in 0 until BLOCK_SIZE) {
            for (j in 0 until BLOCK_SIZE) {
                var sum = 0.0
                for (k in 0 until BLOCK_SIZE) {
                    sum += DCT_MATRIX_T[i][k] * freq[k][j]
                }
                temp[i][j] = sum
            }
        }

        // spatial = temp * T
        for (i in 0 until BLOCK_SIZE) {
            for (j in 0 until BLOCK_SIZE) {
                var sum = 0.0
                for (k in 0 until BLOCK_SIZE) {
                    sum += temp[i][k] * DCT_MATRIX[k][j]
                }
                spatial[i][j] = sum
            }
        }
        return spatial
    }

    /**
     * Quantization Index Modulation (QIM) of a single DCT coefficient c to embed bit b in {0, 1}.
     * Even integer multiple of step -> bit 0
     * Odd integer multiple of step  -> bit 1
     * Uses signHint (+1 or -1) when coeff sits near a decision midpoint to prevent constructive interference in flat blocks.
     */
    private fun qimEmbedBit(coeff: Double, step: Double, bit: Int, signHint: Int = 1): Double {
        val k = (coeff / step).roundToInt()
        val parity = abs(k) and 1
        if (parity == bit) {
            return k * step
        }
        val diff = coeff - k * step
        val kAdjusted = when {
            diff > 0.5 -> k + 1
            diff < -0.5 -> k - 1
            signHint >= 0 -> k + 1
            else -> k - 1
        }
        return kAdjusted * step
    }

    /**
     * Extracts a single bit from a DCT coefficient c using QIM step.
     */
    private fun qimExtractBit(coeff: Double, step: Double): Int {
        val k = (coeff / step).roundToInt()
        return abs(k) and 1
    }

    /**
     * Embeds an encrypted payload into a Bitmap using:
     * 1. Reed-Solomon GF(2^8) Forward Error Correction (~30% parity bytes + byte interleaving)
     * 2. 8x8 Block 2D-DCT Mid-Frequency Quantization Index Modulation (QIM) on Luminance (Y)
     */
    fun embedRobustPayload(sourceBitmap: Bitmap, rawPayload: ByteArray): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val maxNetCap = calculateMaxNetCapacityBytes(width, height)

        if (rawPayload.size > maxNetCap) {
            throw IllegalArgumentException(
                "Ukuran muatan (${rawPayload.size} B) melebihi kapasitas mode TACTICAL ROBUST DCT+RS (${maxNetCap} B) pada resolusi ${width}x${height} px."
            )
        }

        // Step 1: Apply Reed-Solomon FEC + Interleaving
        val fecBytes = ReedSolomonCodec.encodeWithFec(rawPayload)
        val totalBits = fecBytes.size * 8

        val outputBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(width * height)
        outputBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val blocksX = width / BLOCK_SIZE
        val blocksY = height / BLOCK_SIZE

        var bitIdx = 0
        val yBlockOrig = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }
        val rOrig = Array(BLOCK_SIZE) { IntArray(BLOCK_SIZE) }
        val gOrig = Array(BLOCK_SIZE) { IntArray(BLOCK_SIZE) }
        val bOrig = Array(BLOCK_SIZE) { IntArray(BLOCK_SIZE) }
        val aOrig = Array(BLOCK_SIZE) { IntArray(BLOCK_SIZE) }

        for (by in 0 until blocksY) {
            if (bitIdx >= totalBits) break
            for (bx in 0 until blocksX) {
                if (bitIdx >= totalBits) break

                val blockStartBit = bitIdx
                // Extract 8x8 block and apply luma-chroma headroom [64..191] so IDCT & JPEG 4:2:0 never clip RGB at 0 or 255
                for (r in 0 until BLOCK_SIZE) {
                    val py = by * BLOCK_SIZE + r
                    for (c in 0 until BLOCK_SIZE) {
                        val px = bx * BLOCK_SIZE + c
                        val color = pixels[py * width + px]
                        val a = (color shr 24) and 0xFF
                        val rawR = (color shr 16) and 0xFF
                        val rawG = (color shr 8) and 0xFF
                        val rawB = color and 0xFF

                        val rawY = (0.299 * rawR + 0.587 * rawG + 0.114 * rawB).roundToInt()
                        val baseY = 64 + (rawY * 127 + 127) / 255
                        val safeR = (baseY + ((rawR - rawY) * 35) / 100).coerceIn(48, 207)
                        val safeG = (baseY + ((rawG - rawY) * 35) / 100).coerceIn(48, 207)
                        val safeB = (baseY + ((rawB - rawY) * 35) / 100).coerceIn(48, 207)

                        aOrig[r][c] = a
                        rOrig[r][c] = safeR
                        gOrig[r][c] = safeG
                        bOrig[r][c] = safeB
                    }
                }

                // Two-pass QIM projection so integer RGB rounding & clamping converge onto exact QIM lattice points
                var endBitIdx = blockStartBit
                for (pass in 0 until 2) {
                    for (r in 0 until BLOCK_SIZE) {
                        for (c in 0 until BLOCK_SIZE) {
                            val yVal = 0.299 * rOrig[r][c] + 0.587 * gOrig[r][c] + 0.114 * bOrig[r][c]
                            yBlockOrig[r][c] = yVal - 128.0
                        }
                    }

                    val dctCoeffs = forwardDct8x8(yBlockOrig)
                    var localBitIdx = blockStartBit

                    for (coordIdx in MID_FREQ_COORDS.indices) {
                        if (localBitIdx >= totalBits) break
                        val coord = MID_FREQ_COORDS[coordIdx]
                        val u = coord[0]
                        val v = coord[1]
                        val bytePos = localBitIdx / 8
                        val bitOffset = 7 - (localBitIdx % 8)
                        val bitVal = (fecBytes[bytePos].toInt() shr bitOffset) and 1

                        val step = getQimStep(u, v)
                        val signHint = if (((coordIdx + bx + by) and 1) == 0) 1 else -1
                        dctCoeffs[u][v] = qimEmbedBit(dctCoeffs[u][v], step, bitVal, signHint)
                        localBitIdx++
                    }
                    endBitIdx = localBitIdx

                    val yBlockMod = inverseDct8x8(dctCoeffs)

                    for (r in 0 until BLOCK_SIZE) {
                        for (c in 0 until BLOCK_SIZE) {
                            val oldY = yBlockOrig[r][c] + 128.0
                            val newY = (yBlockMod[r][c] + 128.0).coerceIn(8.0, 247.0)
                            val dY = (newY - oldY).roundToInt()

                            val newR = (rOrig[r][c] + dY).coerceIn(4, 251)
                            val newB = (bOrig[r][c] + dY).coerceIn(4, 251)
                            val exactG = ((newY - 0.299 * newR - 0.114 * newB) / 0.587).roundToInt()
                            val newG = exactG.coerceIn(4, 251)

                            rOrig[r][c] = newR
                            gOrig[r][c] = newG
                            bOrig[r][c] = newB
                        }
                    }
                }
                bitIdx = endBitIdx

                for (r in 0 until BLOCK_SIZE) {
                    val py = by * BLOCK_SIZE + r
                    for (c in 0 until BLOCK_SIZE) {
                        val px = bx * BLOCK_SIZE + c
                        pixels[py * width + px] =
                            (aOrig[r][c] shl 24) or (rOrig[r][c] shl 16) or (gOrig[r][c] shl 8) or bOrig[r][c]
                    }
                }
            }
        }

        outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return outputBitmap
    }

    /**
     * Extracts a Reed-Solomon FEC-protected payload from the 8x8 DCT mid-frequency band of a Bitmap.
     * Returns ReedSolomonCodec.FecDecodeResult containing the reconstructed payload and FEC error statistics,
     * or null if no valid DCT+RS payload is present.
     */
    fun extractRobustPayload(stegoBitmap: Bitmap): ReedSolomonCodec.FecDecodeResult? {
        val width = stegoBitmap.width
        val height = stegoBitmap.height
        val blocksX = width / BLOCK_SIZE
        val blocksY = height / BLOCK_SIZE
        val totalBlocks = blocksX * blocksY

        val maxRawBytes = (totalBlocks * BITS_PER_BLOCK) / 8
        if (maxRawBytes < ReedSolomonCodec.HEADER_TOTAL_LEN + 32) return null

        val pixels = IntArray(width * height)
        stegoBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val extractedBytes = ByteArray(maxRawBytes)
        var bitIdx = 0
        val maxBits = maxRawBytes * 8

        var requiredBytes = ReedSolomonCodec.HEADER_TOTAL_LEN
        var headerDecoded = false

        val yBlock = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }

        outer@ for (by in 0 until blocksY) {
            for (bx in 0 until blocksX) {
                for (r in 0 until BLOCK_SIZE) {
                    val py = by * BLOCK_SIZE + r
                    for (c in 0 until BLOCK_SIZE) {
                        val px = bx * BLOCK_SIZE + c
                        val color = pixels[py * width + px]
                        val rCh = (color shr 16) and 0xFF
                        val gCh = (color shr 8) and 0xFF
                        val bCh = color and 0xFF
                        val yVal = 0.299 * rCh + 0.587 * gCh + 0.114 * bCh
                        yBlock[r][c] = yVal - 128.0
                    }
                }

                val dctCoeffs = forwardDct8x8(yBlock)

                for (coord in MID_FREQ_COORDS) {
                    if (bitIdx >= maxBits) break@outer
                    val u = coord[0]
                    val v = coord[1]
                    val step = getQimStep(u, v)
                    val bit = qimExtractBit(dctCoeffs[u][v], step)

                    val bytePos = bitIdx / 8
                    val bitOffset = 7 - (bitIdx % 8)
                    if (bit == 1) {
                        extractedBytes[bytePos] = (extractedBytes[bytePos].toInt() or (1 shl bitOffset)).toByte()
                    }
                    bitIdx++

                    // Once we have the 16-byte RS header, decode it immediately to check if this image is DCT-encoded
                    if (!headerDecoded && bitIdx == ReedSolomonCodec.HEADER_TOTAL_LEN * 8) {
                        val headerCodeword = extractedBytes.copyOfRange(0, ReedSolomonCodec.HEADER_TOTAL_LEN)
                        val decodedHeader = ReedSolomonCodec.decodeBlock(headerCodeword, 8) ?: return null
                        val hData = decodedHeader.first
                        if (hData[0] != ReedSolomonCodec.FEC_MAGIC[0] ||
                            hData[1] != ReedSolomonCodec.FEC_MAGIC[1] ||
                            hData[2] != ReedSolomonCodec.FEC_MAGIC[2] ||
                            hData[3] != ReedSolomonCodec.FEC_MAGIC[3]
                        ) {
                            return null
                        }
                        val dataLen = ((hData[4].toInt() and 0xFF) shl 8) or (hData[5].toInt() and 0xFF)
                        val dataShard = hData[6].toInt() and 0xFF
                        val parityShard = hData[7].toInt() and 0xFF
                        if (dataLen <= 0 || dataShard <= 0 || parityShard <= 0 || dataShard + parityShard > 255) {
                            return null
                        }
                        val numBlocks = (dataLen + dataShard - 1) / dataShard
                        requiredBytes = ReedSolomonCodec.HEADER_TOTAL_LEN + numBlocks * (dataShard + parityShard)
                        if (requiredBytes > maxRawBytes) return null
                        headerDecoded = true
                    }

                    if (headerDecoded && bitIdx >= requiredBytes * 8) {
                        break@outer
                    }
                }
            }
        }

        if (!headerDecoded) return null

        val fecStream = extractedBytes.copyOfRange(0, requiredBytes)
        return ReedSolomonCodec.decodeWithFec(fecStream)
    }

    /**
     * Simulates real-world lossy JPEG compression at a given quality factor (e.g., Q=80)
     * in memory using the complete ITU-T T.81 Baseline JPEG pipeline (RGB -> YCbCr 4:2:0
     * chroma subsampling -> 8x8 2D-DCT -> JPEG Annex K Quantization Table at `quality` ->
     * 8x8 2D-IDCT -> clamped integer ARGB_8888 reconstruction).
     */
    fun simulateLossyJpegCompression(sourceBitmap: Bitmap, quality: Int = 80): Bitmap {
        val qClamped = quality.coerceIn(50, 100)
        return applyItuT81JpegCompression(sourceBitmap, qClamped)
    }

    /**
     * Full ITU-T T.81 Baseline JPEG Lossy Compression & Decompression Pipeline:
     * 1. RGB -> YCbCr conversion + 4:2:0 Chroma Subsampling
     * 2. 8x8 2D-DCT on Luminance Y and Chrominance Cb, Cr
     * 3. Quantization & De-quantization using scaled JPEG Annex K Quantization Tables for `quality`
     * 4. 8x8 2D-IDCT and YCbCr -> clamped integer ARGB_8888 reconstruction
     */
    private fun applyItuT81JpegCompression(sourceBitmap: Bitmap, quality: Int): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val outBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(width * height)
        outBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val scale = if (quality < 50) 5000 / quality else 200 - 2 * quality
        val qTableY = Array(BLOCK_SIZE) { u ->
            DoubleArray(BLOCK_SIZE) { v ->
                max(1, (JPEG_LUMINANCE_QUANT_Q50[u][v] * scale + 50) / 100).toDouble()
            }
        }

        val blocksX = width / BLOCK_SIZE
        val blocksY = height / BLOCK_SIZE
        val yBlock = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }
        val cbBlock = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }
        val crBlock = Array(BLOCK_SIZE) { DoubleArray(BLOCK_SIZE) }

        for (by in 0 until blocksY) {
            for (bx in 0 until blocksX) {
                for (r in 0 until BLOCK_SIZE) {
                    val py = by * BLOCK_SIZE + r
                    for (c in 0 until BLOCK_SIZE) {
                        val px = bx * BLOCK_SIZE + c
                        val color = pixels[py * width + px]
                        val rCh = (color shr 16) and 0xFF
                        val gCh = (color shr 8) and 0xFF
                        val bCh = color and 0xFF

                        val yVal = 0.299 * rCh + 0.587 * gCh + 0.114 * bCh
                        val cbVal = 128.0 - 0.168736 * rCh - 0.331264 * gCh + 0.5 * bCh
                        val crVal = 128.0 + 0.5 * rCh - 0.418688 * gCh - 0.081312 * bCh

                        yBlock[r][c] = yVal - 128.0
                        cbBlock[r][c] = cbVal
                        crBlock[r][c] = crVal
                    }
                }

                // 4:2:0 Chroma Subsampling (2x2 average on Cb and Cr)
                for (r in 0 until BLOCK_SIZE step 2) {
                    for (c in 0 until BLOCK_SIZE step 2) {
                        val avgCb = 0.25 * (cbBlock[r][c] + cbBlock[r][c + 1] + cbBlock[r + 1][c] + cbBlock[r + 1][c + 1])
                        val avgCr = 0.25 * (crBlock[r][c] + crBlock[r][c + 1] + crBlock[r + 1][c] + crBlock[r + 1][c + 1])
                        cbBlock[r][c] = avgCb; cbBlock[r][c + 1] = avgCb
                        cbBlock[r + 1][c] = avgCb; cbBlock[r + 1][c + 1] = avgCb
                        crBlock[r][c] = avgCr; crBlock[r][c + 1] = avgCr
                        crBlock[r + 1][c] = avgCr; crBlock[r + 1][c + 1] = avgCr
                    }
                }

                // Forward 8x8 DCT + Quantization at `quality` + De-quantization + Inverse 8x8 IDCT
                val dctY = forwardDct8x8(yBlock)
                for (u in 0 until BLOCK_SIZE) {
                    for (v in 0 until BLOCK_SIZE) {
                        val q = qTableY[u][v]
                        dctY[u][v] = (dctY[u][v] / q).roundToInt() * q
                    }
                }
                val idctY = inverseDct8x8(dctY)

                for (r in 0 until BLOCK_SIZE) {
                    val py = by * BLOCK_SIZE + r
                    for (c in 0 until BLOCK_SIZE) {
                        val px = bx * BLOCK_SIZE + c
                        val yRec = idctY[r][c] + 128.0
                        val cbRec = cbBlock[r][c] - 128.0
                        val crRec = crBlock[r][c] - 128.0

                        val rRec = (yRec + 1.402 * crRec).roundToInt().coerceIn(0, 255)
                        val gRec = (yRec - 0.344136 * cbRec - 0.714136 * crRec).roundToInt().coerceIn(0, 255)
                        val bRec = (yRec + 1.772 * cbRec).roundToInt().coerceIn(0, 255)

                        pixels[py * width + px] = (0xFF shl 24) or (rRec shl 16) or (gRec shl 8) or bRec
                    }
                }
            }
        }

        outBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return outBitmap
    }
}
