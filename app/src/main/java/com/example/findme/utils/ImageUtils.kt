package com.example.findme.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import com.example.findme.R
import java.io.File

object ImageUtils {

    /**
     * Loads an item image into an ImageView using native Android APIs.
     * Supports local internal storage file paths, URIs, and bundled sample drawable resources.
     * Automatically applies memory-efficient downsampling and EXIF orientation correction.
     */
    fun loadItemImage(
        imageView: ImageView,
        imageUri: String?,
        imageResId: Int?,
        targetWidth: Int = 600,
        targetHeight: Int = 450
    ) {
        val context = imageView.context

        // 1. Try loading from imageUri (internal storage file path or URI)
        if (!imageUri.isNullOrBlank()) {
            val uri = try {
                if (imageUri.startsWith("/")) {
                    Uri.fromFile(File(imageUri))
                } else {
                    Uri.parse(imageUri)
                }
            } catch (_: Exception) {
                null
            }

            if (uri != null) {
                val bitmap = decodeSampledBitmap(context, uri, targetWidth, targetHeight)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    return
                }
            }
        }

        // 2. Try loading bundled drawable resource (for sample items)
        if (imageResId != null && imageResId != 0) {
            imageView.setImageResource(imageResId)
            return
        }

        // 3. Fallback placeholder
        imageView.setImageResource(R.drawable.ic_photo_placeholder)
    }

    /**
     * Decodes a downsampled Bitmap from a given URI to fit within the specified bounds,
     * ensuring optimal memory usage and avoiding OutOfMemoryErrors.
     */
    fun decodeSampledBitmap(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return null
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // More memory-efficient

            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            // Correct orientation from EXIF metadata
            val rotationDegrees = getExifRotation(context, uri)
            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(
                    decodedBitmap,
                    0,
                    0,
                    decodedBitmap.width,
                    decodedBitmap.height,
                    matrix,
                    true
                )
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
            val exif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ExifInterface(inputStream)
            } else {
                null
            }
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL
            inputStream.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {
            0
        }
    }
}

