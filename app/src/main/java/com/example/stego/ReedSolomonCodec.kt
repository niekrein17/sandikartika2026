package com.example.stego

import java.nio.ByteBuffer

/**
 * Military-grade Reed-Solomon Forward Error Correction (FEC) Codec over Galois Field GF(2^8).
 *
 * Uses primitive polynomial x^8 + x^4 + x^3 + x^2 + 1 (0x11D).
 * Provides systematic encoding, Berlekamp-Massey error locator polynomial synthesis,
 * Chien search root finding, Forney error magnitude evaluation, and byte-level block interleaving
 * to protect AES-256-GCM ciphertexts against burst and random bit/coefficient errors caused by
 * lossy JPEG/WebP compression in tactical radio/public messaging channels.
 */
object ReedSolomonCodec {

    private const val PRIMITIVE_POLY = 0x11D
    private val EXP_TABLE = IntArray(512)
    private val LOG_TABLE = IntArray(256)

    // Magic header "RSKT" (Reed-Solomon Kartika Tactical)
    val FEC_MAGIC = byteArrayOf(0x52, 0x53, 0x4B, 0x54)

    // Header is 8 bytes: [RSKT: 4B] + [DataLen: 2B] + [BlockDataSize: 1B] + [ParitySize: 1B]
    // Protected with 8 parity bytes -> 16 bytes total header codeword (corrects up to 4 header byte errors)
    private const val HEADER_DATA_LEN = 8
    private const val HEADER_PARITY_LEN = 8
    const val HEADER_TOTAL_LEN = HEADER_DATA_LEN + HEADER_PARITY_LEN

    // Default block parameters: 140 data bytes + 60 parity bytes = 200 bytes per codeword
    // Parity ratio = 60 / 200 = 30.0% of total codeword (42.8% parity overhead on data),
    // correcting up to 30 corrupted bytes (240 bits) per 200-byte interleaved block.
    const val DEFAULT_DATA_SHARD = 140
    const val DEFAULT_PARITY_SHARD = 60

    init {
        var x = 1
        for (i in 0 until 255) {
            EXP_TABLE[i] = x
            LOG_TABLE[x] = i
            x = x shl 1
            if ((x and 0x100) != 0) {
                x = x xor PRIMITIVE_POLY
            }
        }
        for (i in 255 until 512) {
            EXP_TABLE[i] = EXP_TABLE[i - 255]
        }
    }

    data class FecDecodeResult(
        val data: ByteArray,
        val correctedBytesCount: Int,
        val totalCodewords: Int,
        val parityRatioPercent: Int
    )

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return EXP_TABLE[LOG_TABLE[a and 0xFF] + LOG_TABLE[b and 0xFF]]
    }

    private fun gfDiv(a: Int, b: Int): Int {
        if (b == 0) throw ArithmeticException("GF(2^8) division by zero")
        if (a == 0) return 0
        return EXP_TABLE[(LOG_TABLE[a and 0xFF] - LOG_TABLE[b and 0xFF]) + 255]
    }

    private fun gfPow(a: Int, power: Int): Int {
        if (power == 0) return 1
        if (a == 0) return 0
        val logA = LOG_TABLE[a and 0xFF]
        val exp = (logA * power) % 255
        return EXP_TABLE[if (exp < 0) exp + 255 else exp]
    }

    private fun gfInv(a: Int): Int {
        if (a == 0) throw ArithmeticException("GF(2^8) inverse of zero")
        return EXP_TABLE[255 - LOG_TABLE[a and 0xFF]]
    }

    /**
     * Polynomial multiplication in GF(2^8) with highest degree first (index 0 = x^deg).
     */
    private fun polyMul(p: IntArray, q: IntArray): IntArray {
        val res = IntArray(p.size + q.size - 1)
        for (j in q.indices) {
            for (i in p.indices) {
                res[i + j] = res[i + j] xor gfMul(p[i], q[j])
            }
        }
        return res
    }

    /**
     * Evaluates polynomial at x in GF(2^8) using Horner's method (index 0 = highest degree).
     */
    private fun polyEval(poly: IntArray, x: Int): Int {
        var y = poly[0]
        for (i in 1 until poly.size) {
            y = gfMul(y, x) xor poly[i]
        }
        return y
    }

    /**
     * Builds Reed-Solomon generator polynomial g(x) = (x - alpha^0)(x - alpha^1)...(x - alpha^(nsym-1)).
     */
    private fun buildGeneratorPoly(nsym: Int): IntArray {
        var g = intArrayOf(1)
        for (i in 0 until nsym) {
            g = polyMul(g, intArrayOf(1, gfPow(2, i)))
        }
        return g
    }

    /**
     * Encodes a single data block of length k <= 255 - nsym by appending nsym parity bytes.
     */
    fun encodeBlock(data: ByteArray, nsym: Int): ByteArray {
        require(data.size + nsym <= 255) { "RS block size (${data.size + nsym}) exceeds GF(2^8) limit 255" }
        val gen = buildGeneratorPoly(nsym)
        val msgOut = IntArray(data.size + nsym)
        for (i in data.indices) {
            msgOut[i] = data[i].toInt() and 0xFF
        }

        for (i in data.indices) {
            val coef = msgOut[i]
            if (coef != 0) {
                for (j in 1 until gen.size) {
                    msgOut[i + j] = msgOut[i + j] xor gfMul(gen[j], coef)
                }
            }
        }

        val out = ByteArray(data.size + nsym)
        for (i in data.indices) {
            out[i] = data[i]
        }
        for (i in 0 until nsym) {
            out[data.size + i] = (msgOut[data.size + i] and 0xFF).toByte()
        }
        return out
    }

    /**
     * Decodes a single Reed-Solomon codeword of length n = k + nsym.
     * Returns Pair(reconstructedDataBytes, correctedSymbolCount) or null if uncorrectable.
     */
    fun decodeBlock(codeword: ByteArray, nsym: Int): Pair<ByteArray, Int>? {
        val n = codeword.size
        val k = n - nsym
        if (k <= 0 || n > 255) return null

        val msg = IntArray(n) { codeword[it].toInt() and 0xFF }

        // 1. Compute Syndromes S_i = R(alpha^i) for i = 0 .. nsym-1
        val synd = IntArray(nsym)
        var hasError = false
        for (i in 0 until nsym) {
            val s = polyEval(msg, gfPow(2, i))
            synd[i] = s
            if (s != 0) hasError = true
        }

        if (!hasError) {
            return Pair(codeword.copyOfRange(0, k), 0)
        }

        // 2. Berlekamp-Massey algorithm to find error locator polynomial C(x) = 1 + C_1*x + ... + C_L*x^L
        var cPoly = IntArray(nsym + 1).apply { this[0] = 1 }
        var bPoly = IntArray(nsym + 1).apply { this[0] = 1 }
        var errCountL = 0
        var shiftM = 1
        var prevDiscB = 1

        for (stepN in 0 until nsym) {
            var disc = synd[stepN]
            for (j in 1..errCountL) {
                disc = disc xor gfMul(cPoly[j], synd[stepN - j])
            }
            if (disc == 0) {
                shiftM++
            } else {
                val tempPoly = cPoly.copyOf()
                val factor = gfDiv(disc, prevDiscB)
                for (j in 0..(nsym - shiftM)) {
                    if (bPoly[j] != 0) {
                        cPoly[j + shiftM] = cPoly[j + shiftM] xor gfMul(factor, bPoly[j])
                    }
                }
                if (2 * errCountL <= stepN) {
                    errCountL = stepN + 1 - errCountL
                    bPoly = tempPoly
                    prevDiscB = disc
                    shiftM = 1
                } else {
                    shiftM++
                }
            }
        }

        val deg = errCountL
        if (deg <= 0 || deg * 2 > nsym) {
            return null
        }
        val errLoc = cPoly.copyOfRange(0, deg + 1)

        // 3. Chien Search: find roots X_k^-1 of C(x) = sum_{j=0..deg} errLoc[j] * x^j
        val errPositions = ArrayList<Int>()
        for (i in 0 until n) {
            val xInv = gfInv(gfPow(2, i)) // X_k^-1 = (alpha^i)^-1 where i = n - 1 - pos
            var evalSum = 0
            var xInvPow = 1
            for (j in 0..deg) {
                evalSum = evalSum xor gfMul(errLoc[j], xInvPow)
                xInvPow = gfMul(xInvPow, xInv)
            }
            if (evalSum == 0) {
                val pos = n - 1 - i
                if (pos in 0 until n) {
                    errPositions.add(pos)
                }
            }
        }

        if (errPositions.size != deg) {
            // Error locator roots did not match degree -> uncorrectable
            return null
        }

        // 4. Forney Algorithm to compute error magnitudes
        // Compute Omega(x) = (Synd(x) * Sigma(x)) mod x^nsym in constant-first order
        val omega = IntArray(nsym)
        for (i in 0 until nsym) {
            var term = 0
            for (j in 0..minOf(i, deg)) {
                term = term xor gfMul(synd[i - j], errLoc[j])
            }
            omega[i] = term
        }

        // Formal derivative sigma'(x) in GF(2^8): odd power terms of errLoc
        // sigma'(x) = errLoc[1] + errLoc[3]*x^2 + errLoc[5]*x^4 + ...
        for (pos in errPositions) {
            val power = n - 1 - pos
            val xVal = gfPow(2, power)      // X_k = alpha^(n - 1 - pos)
            val xInv = gfInv(xVal)          // X_k^-1

            // Evaluate Omega(X_k^-1)
            var omegaVal = 0
            var xInvPow = 1
            for (i in 0 until nsym) {
                omegaVal = omegaVal xor gfMul(omega[i], xInvPow)
                xInvPow = gfMul(xInvPow, xInv)
            }

            // Evaluate Sigma'(X_k^-1)
            var sigmaDerivVal = 0
            var xInvSqPow = 1
            val xInvSq = gfMul(xInv, xInv)
            var j = 1
            while (j <= deg) {
                sigmaDerivVal = sigmaDerivVal xor gfMul(errLoc[j], xInvSqPow)
                xInvSqPow = gfMul(xInvSqPow, xInvSq)
                j += 2
            }

            if (sigmaDerivVal == 0) return null

            // Since generator roots start at alpha^0 (FCR = 0), magnitude e_k = X_k * Omega(X_k^-1) / Sigma'(X_k^-1)
            val errMag = gfMul(xVal, gfDiv(omegaVal, sigmaDerivVal))
            msg[pos] = msg[pos] xor errMag
        }

        // 5. Verify syndromes of corrected message are all zero
        for (i in 0 until nsym) {
            if (polyEval(msg, gfPow(2, i)) != 0) {
                return null
            }
        }

        val reconstructed = ByteArray(k) { (msg[it] and 0xFF).toByte() }
        return Pair(reconstructed, errPositions.size)
    }

    /**
     * Encodes an arbitrary-length payload with Reed-Solomon FEC and byte-level interleaving.
     *
     * Frame Format:
     * - [16 Bytes]: RS(16,8) Protected Header containing:
     *     [RSKT: 4B] [DataLen: 2B UInt16] [DataShard: 1B] [ParityShard: 1B] + [8B Header Parity]
     * - [Interleaved Codewords]: B blocks of length (k_i + parityShard), interleaved column-wise
     *   so burst errors in any 8x8 DCT block are spread across all RS codewords.
     */
    fun encodeWithFec(
        payload: ByteArray,
        dataShardSize: Int = DEFAULT_DATA_SHARD,
        parityShardSize: Int = DEFAULT_PARITY_SHARD
    ): ByteArray {
        require(payload.isNotEmpty() && payload.size <= 32767) {
            "Ukuran muatan untuk mode DCT+RS harus antara 1 B hingga 32 KB (saat ini: ${payload.size} B)."
        }

        // Build 8-byte header
        val headerData = ByteArray(HEADER_DATA_LEN)
        System.arraycopy(FEC_MAGIC, 0, headerData, 0, 4)
        headerData[4] = ((payload.size shr 8) and 0xFF).toByte()
        headerData[5] = (payload.size and 0xFF).toByte()
        headerData[6] = (dataShardSize and 0xFF).toByte()
        headerData[7] = (parityShardSize and 0xFF).toByte()

        val encodedHeader = encodeBlock(headerData, HEADER_PARITY_LEN) // 16 bytes

        // Split payload into blocks of dataShardSize (last block padded to dataShardSize for uniform interleaving)
        val numBlocks = (payload.size + dataShardSize - 1) / dataShardSize
        val blockCodewordLen = dataShardSize + parityShardSize
        val codewords = Array(numBlocks) { bIdx ->
            val start = bIdx * dataShardSize
            val end = minOf(start + dataShardSize, payload.size)
            val blockData = ByteArray(dataShardSize)
            System.arraycopy(payload, start, blockData, 0, end - start)
            encodeBlock(blockData, parityShardSize)
        }

        // Interleave bytes across blocks: column-by-column
        val totalBodyBytes = numBlocks * blockCodewordLen
        val output = ByteArray(HEADER_TOTAL_LEN + totalBodyBytes)
        System.arraycopy(encodedHeader, 0, output, 0, HEADER_TOTAL_LEN)

        var outIdx = HEADER_TOTAL_LEN
        for (col in 0 until blockCodewordLen) {
            for (row in 0 until numBlocks) {
                output[outIdx++] = codewords[row][col]
            }
        }

        return output
    }

    /**
     * Decodes an interleaved Reed-Solomon FEC frame back into the original payload.
     */
    fun decodeWithFec(fecStream: ByteArray): FecDecodeResult? {
        if (fecStream.size < HEADER_TOTAL_LEN) return null

        val headerCodeword = fecStream.copyOfRange(0, HEADER_TOTAL_LEN)
        val (headerData, headerCorrected) = decodeBlock(headerCodeword, HEADER_PARITY_LEN) ?: return null

        // Verify magic "RSKT"
        for (i in 0 until 4) {
            if (headerData[i] != FEC_MAGIC[i]) return null
        }

        val dataLen = ((headerData[4].toInt() and 0xFF) shl 8) or (headerData[5].toInt() and 0xFF)
        val dataShardSize = headerData[6].toInt() and 0xFF
        val parityShardSize = headerData[7].toInt() and 0xFF

        if (dataLen <= 0 || dataShardSize <= 0 || parityShardSize <= 0 || dataShardSize + parityShardSize > 255) {
            return null
        }

        val numBlocks = (dataLen + dataShardSize - 1) / dataShardSize
        val blockCodewordLen = dataShardSize + parityShardSize
        val expectedTotalSize = HEADER_TOTAL_LEN + (numBlocks * blockCodewordLen)

        if (fecStream.size < expectedTotalSize) return null

        // De-interleave into individual codewords
        val codewords = Array(numBlocks) { ByteArray(blockCodewordLen) }
        var inIdx = HEADER_TOTAL_LEN
        for (col in 0 until blockCodewordLen) {
            for (row in 0 until numBlocks) {
                codewords[row][col] = fecStream[inIdx++]
            }
        }

        // Decode each RS codeword
        val reconstructedFull = ByteBuffer.allocate(numBlocks * dataShardSize)
        var totalCorrected = headerCorrected

        for (row in 0 until numBlocks) {
            val decodedPair = decodeBlock(codewords[row], parityShardSize) ?: return null
            reconstructedFull.put(decodedPair.first)
            totalCorrected += decodedPair.second
        }

        val exactPayload = ByteArray(dataLen)
        val fullArr = reconstructedFull.array()
        System.arraycopy(fullArr, 0, exactPayload, 0, dataLen)

        val parityRatioPcnt = (parityShardSize * 100) / blockCodewordLen
        return FecDecodeResult(
            data = exactPayload,
            correctedBytesCount = totalCorrected,
            totalCodewords = numBlocks,
            parityRatioPercent = parityRatioPcnt
        )
    }

    /**
     * Calculates the total byte size after FEC framing for a given raw payload byte length.
     */
    fun calculateFecFrameSize(
        rawPayloadSize: Int,
        dataShardSize: Int = DEFAULT_DATA_SHARD,
        parityShardSize: Int = DEFAULT_PARITY_SHARD
    ): Int {
        if (rawPayloadSize <= 0) return HEADER_TOTAL_LEN
        val numBlocks = (rawPayloadSize + dataShardSize - 1) / dataShardSize
        return HEADER_TOTAL_LEN + numBlocks * (dataShardSize + parityShardSize)
    }
}
