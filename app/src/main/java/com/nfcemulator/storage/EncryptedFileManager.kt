package com.nfcemulator.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class EncryptedFileManager(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {

    private val dumpsDir: File
        get() = File(context.filesDir, "dumps").also { it.mkdirs() }

    suspend fun saveDump(id: String, data: ByteArray): String = withContext(Dispatchers.IO) {
        val encrypted = cryptoManager.encrypt(data)
        val file = File(dumpsDir, "$id.enc")
        file.writeBytes(encrypted)
        file.absolutePath
    }

    suspend fun loadDump(id: String): ByteArray? = withContext(Dispatchers.IO) {
        val file = File(dumpsDir, "$id.enc")
        if (!file.exists()) return@withContext null
        val encrypted = file.readBytes()
        cryptoManager.decrypt(encrypted)
    }

    suspend fun deleteDump(id: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(dumpsDir, "$id.enc")
        file.delete()
    }

    suspend fun dumpExists(id: String): Boolean = withContext(Dispatchers.IO) {
        File(dumpsDir, "$id.enc").exists()
    }

    suspend fun getAllDumpIds(): List<String> = withContext(Dispatchers.IO) {
        dumpsDir.listFiles()
            ?.filter { it.extension == "enc" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }

    suspend fun getDumpsSize(): Long = withContext(Dispatchers.IO) {
        dumpsDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
}
