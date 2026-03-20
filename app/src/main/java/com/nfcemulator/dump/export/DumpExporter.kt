package com.nfcemulator.dump.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.nfcemulator.dump.model.TagDump
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DumpExporter(private val context: Context) {

    suspend fun exportAsMct(dump: TagDump): Uri? = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        for (sector in dump.sectors) {
            sb.appendLine("+Sector: ${sector.index}")
            for (block in sector.blocks) {
                sb.appendLine(block.data.joinToString("") { "%02X".format(it) })
            }
        }
        saveToFile("${dump.name}.mct", sb.toString().toByteArray())
    }

    suspend fun exportAsBin(dump: TagDump): Uri? = withContext(Dispatchers.IO) {
        val data = dump.sectors.flatMap { sector ->
            sector.blocks.flatMap { block -> block.data.toList() }
        }.toByteArray()
        saveToFile("${dump.name}.bin", data)
    }

    suspend fun exportAsJson(dump: TagDump): Uri? = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.appendLine("{")
        sb.appendLine("  \"name\": \"${dump.name}\",")
        sb.appendLine("  \"type\": \"${dump.type.displayName}\",")
        sb.appendLine("  \"uid\": \"${dump.uidHex}\",")
        sb.appendLine("  \"sak\": \"${dump.sakHex}\",")
        sb.appendLine("  \"atqa\": \"${dump.atqaHex}\",")
        sb.appendLine("  \"sectors\": [")
        for ((sIdx, sector) in dump.sectors.withIndex()) {
            sb.appendLine("    {")
            sb.appendLine("      \"index\": ${sector.index},")
            sb.appendLine("      \"keyA\": \"${sector.keyA?.joinToString("") { "%02X".format(it) } ?: ""}\",")
            sb.appendLine("      \"keyB\": \"${sector.keyB?.joinToString("") { "%02X".format(it) } ?: ""}\",")
            sb.appendLine("      \"blocks\": [")
            for ((bIdx, block) in sector.blocks.withIndex()) {
                val comma = if (bIdx < sector.blocks.size - 1) "," else ""
                sb.appendLine("        \"${block.data.joinToString("") { "%02X".format(it) }}\"$comma")
            }
            sb.appendLine("      ]")
            val sComma = if (sIdx < dump.sectors.size - 1) "," else ""
            sb.appendLine("    }$sComma")
        }
        sb.appendLine("  ]")
        sb.appendLine("}")
        saveToFile("${dump.name}.json", sb.toString().toByteArray())
    }

    private fun saveToFile(fileName: String, data: ByteArray): Uri? {
        val exportDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(exportDir, fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_"))
        file.writeBytes(data)
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            Uri.fromFile(file)
        }
    }

    fun shareFile(uri: Uri, mimeType: String = "*/*") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Export dump").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
