package com.example.stego

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri

/**
 * OPSEC Utility: Strips all EXIF metadata, GPS geotags, camera identifiers, and hardware signatures
 * from carrier photos before steganography embedding to prevent SIGINT location leakage.
 */
object TacticalExifSanitizer {

    data class SanitizeResult(
        val sanitizedBitmap: Bitmap,
        val originalExifFound: Boolean,
        val strippedTags: List<String>,
        val width: Int,
        val height: Int
    )

    fun sanitizeCarrierImage(context: Context, uri: Uri): SanitizeResult? {
        return try {
            val strippedTags = mutableListOf<String>()

            // Inspect EXIF tags before stripping using native android.media.ExifInterface
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val latLong = FloatArray(2)
                    if (exif.getLatLong(latLong)) {
                        strippedTags.add("GPS Koordinat Geografis (${latLong[0]}, ${latLong[1]})")
                    }
                    exif.getAttribute(ExifInterface.TAG_MODEL)?.let {
                        strippedTags.add("Identitas Perangkat/Kamera ($it)")
                    }
                    exif.getAttribute(ExifInterface.TAG_DATETIME)?.let {
                        strippedTags.add("Waktu Pengambilan Asli ($it)")
                    }
                    exif.getAttribute(ExifInterface.TAG_MAKE)?.let {
                        strippedTags.add("Manufaktur ($it)")
                    }
                }
            } catch (_: Exception) {}

            // Decode image bytes into a clean raster bitmap with no metadata retention
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inMutable = true
                }
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            // Inspect rotation from EXIF and rotate if needed so the raster is upright and pristine
            var orientation = ExifInterface.ORIENTATION_NORMAL
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                }
            } catch (_: Exception) {}

            val uprightBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }

            // Ensure pure 32-bit ARGB_8888 software format
            val cleanBitmap = if (uprightBitmap.config != Bitmap.Config.ARGB_8888 || !uprightBitmap.isMutable) {
                uprightBitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                uprightBitmap
            }

            if (strippedTags.isEmpty()) {
                strippedTags.add("Pembersihan Metadata (Zero-Metadata Normalization)")
            }

            SanitizeResult(
                sanitizedBitmap = cleanBitmap,
                originalExifFound = strippedTags.size > 1,
                strippedTags = strippedTags,
                width = cleanBitmap.width,
                height = cleanBitmap.height
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        if (rotated != source && !source.isRecycled) {
            source.recycle()
        }
        return rotated
    }
}
