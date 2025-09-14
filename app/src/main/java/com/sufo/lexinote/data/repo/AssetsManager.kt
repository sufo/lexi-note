package com.sufo.lexinote.data.repo

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetsManager @Inject constructor(val app: Application) {

    private val dictsDir by lazy { File(app.filesDir, "dicts") }

    suspend fun copyAssetsDictToInternalIfNeeded(dirName: String, assetBaseName: String) = withContext(Dispatchers.IO) {
        val destDir = File(dictsDir, dirName)
        if (destDir.exists() && (destDir.listFiles()?.size ?: 0) >= 3) {
            // Assume files are already copied if the directory exists and has at least 3 files.
            return@withContext
        }

        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        try {
            val assetDictPath = "dicts/$dirName"
            val assetFiles = app.assets.list(assetDictPath)

            if (assetFiles.isNullOrEmpty()) {
                throw IllegalStateException("No asset files found in dicts/$dirName")
            }

            assetFiles.forEach { fileName ->
                val assetFileStream = app.assets.open("$assetDictPath/$fileName")
                // We need to rename the files to match the directory name for the library to work
                val destFileName = fileName.replace(assetBaseName, dirName)
                val destFile = File(destDir, destFileName)
                FileOutputStream(destFile).use {
                    assetFileStream.copyTo(it)
                }
                assetFileStream.close()
            }
        } catch (e: Exception) {
            // In a real app, you'd want more robust error handling.
            // For now, we just rethrow to be caught by the repository.
            throw e
        }
    }
}
