package `in`.anits.mesh.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * High-efficiency offline image compression utility for Campus Mesh.
 * Resizes and compresses photos to ~15KB - 30KB JPEG payloads for ultra-fast,
 * peer-to-peer mesh packet transmission over BLE and Local UDP.
 */
object ImageCompressor {
    private const val TAG = "ImageCompressor"
    private const val DEFAULT_MAX_DIMENSION = 480
    private const val DEFAULT_JPEG_QUALITY = 65

    /**
     * Reads an image Uri from the system gallery/camera, downsamples it to prevent OOM,
     * resizes to a max bounding box, and compresses to a Base64 string.
     */
    fun compressUriToBase64(
        context: Context,
        uri: Uri,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        quality: Int = DEFAULT_JPEG_QUALITY
    ): String? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                Log.e(TAG, "Invalid image bounds from URI: $uri")
                return null
            }

            val longestDim = max(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (longestDim / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val srcWidth = sampledBitmap.width
            val srcHeight = sampledBitmap.height
            val scale = maxDimension.toFloat() / max(srcWidth, srcHeight).coerceAtLeast(1)

            val scaledBitmap = if (scale < 1.0f) {
                val targetW = (srcWidth * scale).toInt().coerceAtLeast(1)
                val targetH = (srcHeight * scale).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(sampledBitmap, targetW, targetH, true).also {
                    if (it != sampledBitmap) sampledBitmap.recycle()
                }
            } else {
                sampledBitmap
            }

            val byteStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteStream)
            val jpegBytes = byteStream.toByteArray()
            scaledBitmap.recycle()

            Log.i(TAG, "Compressed photo to ${jpegBytes.size / 1024} KB for mesh transmission")
            Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}", e)
            null
        }
    }

    /**
     * Decodes Base64 JPEG string back into an Android Bitmap.
     */
    fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Base64 image: ${e.message}")
            null
        }
    }

    /**
     * Converts a Base64 string directly into a Compose ImageBitmap for UI rendering.
     */
    fun base64ToComposeImageBitmap(base64Str: String): ImageBitmap? {
        val bitmap = base64ToBitmap(base64Str) ?: return null
        return bitmap.asImageBitmap()
    }
}
