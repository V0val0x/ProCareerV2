package com.example.procareerv2.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            // Create directory if it doesn't exist
            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_images")
            if (!directory.exists() && !directory.mkdirs()) {
                throw Exception("Failed to create directory")
            }

            // Create a unique file name
            val fileName = "profile_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)

            // Copy the content from Uri to our file
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Failed to open input stream")

            if (!file.exists()) {
                throw Exception("File was not created")
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteImage(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            } else {
                true // File doesn't exist, so we consider it deleted
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun createImageUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }
}
