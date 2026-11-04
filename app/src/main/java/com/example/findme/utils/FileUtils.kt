package com.example.findme.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {

    private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
    private const val CAMERA_CACHE_DIR = "camera"
    private const val ITEMS_STORAGE_DIR = "items"

    /**
     * Creates a temporary file in the app's cache directory and returns a content URI
     * generated via FileProvider for camera capture.
     */
    fun createCameraTempUri(context: Context): Pair<File, Uri> {
        val cacheDir = File(context.cacheDir, CAMERA_CACHE_DIR).apply {
            if (!exists()) mkdirs()
        }

        // Clean up older temp files if any (older than 1 hour)
        cleanupOldTempFiles(cacheDir, maxAgeMillis = 60 * 60 * 1000L)

        val tempFile = File.createTempFile(
            "camera_${System.currentTimeMillis()}_",
            ".jpg",
            cacheDir
        )

        val authority = "${context.packageName}$FILE_PROVIDER_AUTHORITY_SUFFIX"
        val contentUri = FileProvider.getUriForFile(context, authority, tempFile)

        return Pair(tempFile, contentUri)
    }

    /**
     * Copies an image from any source URI (e.g. Gallery content URI or temporary camera URI)
     * directly into the app's internal private storage directory (`filesDir/items/item_<itemId>.jpg`).
     *
     * Returns the absolute path of the persisted image file, or null if saving failed.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri, itemId: String): String? {
        return try {
            val itemsDir = File(context.filesDir, ITEMS_STORAGE_DIR).apply {
                if (!exists()) mkdirs()
            }

            val destFile = File(itemsDir, "item_${itemId}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream: InputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a file if it exists at the given path.
     */
    fun deleteFile(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return try {
            val file = File(filePath)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    private fun cleanupOldTempFiles(dir: File, maxAgeMillis: Long) {
        try {
            val now = System.currentTimeMillis()
            dir.listFiles()?.forEach { file ->
                if (now - file.lastModified() > maxAgeMillis) {
                    file.delete()
                }
            }
        } catch (_: Exception) {
            // Ignore cleanup errors
        }
    }
}

