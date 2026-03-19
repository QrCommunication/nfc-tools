package com.nfcemulator.storage.backup

import android.content.Context
import android.net.Uri
import com.nfcemulator.storage.CryptoManager
import com.nfcemulator.storage.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(
    private val context: Context,
    private val cryptoManager: CryptoManager,
    private val database: AppDatabase
) {

    suspend fun exportBackup(destinationUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val dumpsDir = File(context.filesDir, "dumps")
            val dumpFiles = dumpsDir.listFiles()?.filter { it.extension == "enc" } ?: emptyList()

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zip ->
                    // Add encrypted dump files
                    for (file in dumpFiles) {
                        zip.putNextEntry(ZipEntry("dumps/${file.name}"))
                        FileInputStream(file).use { input ->
                            input.copyTo(zip)
                        }
                        zip.closeEntry()
                    }

                    // Add database export
                    val tags = database.tagDao().getAllTagsSync()
                    val dbJson = tags.joinToString("\n") { tag ->
                        "${tag.id}|${tag.name}|${tag.category}|${tag.type}|${tag.uid}|${tag.sak}|${tag.atqa}|${tag.sectorCount}|${tag.keysFound}|${tag.keysTotal}|${tag.sourceFormat}|${tag.filePath}|${tag.notes}|${tag.createdAt}|${tag.lastEmulatedAt ?: ""}"
                    }
                    zip.putNextEntry(ZipEntry("metadata.txt"))
                    zip.write(dbJson.toByteArray())
                    zip.closeEntry()
                }
            }

            Result.success(dumpFiles.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(sourceUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val dumpsDir = File(context.filesDir, "dumps").also { it.mkdirs() }
            var importedCount = 0

            context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        if (entry.name.startsWith("dumps/") && entry.name.endsWith(".enc")) {
                            val fileName = entry.name.substringAfter("dumps/")
                            val outFile = File(dumpsDir, fileName)
                            FileOutputStream(outFile).use { output ->
                                zip.copyTo(output)
                            }
                            importedCount++
                        }
                        entry = zip.nextEntry
                    }
                }
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
