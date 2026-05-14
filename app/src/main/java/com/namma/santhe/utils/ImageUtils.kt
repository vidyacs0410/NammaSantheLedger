package com.namma.santhe.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
    /**
     * Copies the content from a URI to the app's internal files directory and returns the absolute path.
     * This is crucial for persisting images picked from the gallery across app restarts.
     */
    fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return null

            // Create a unique file in the app's files directory
            val fileName = "profile_img_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)

            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
