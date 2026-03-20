package com.nfcemulator

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.nfcemulator.dump.analyzer.DictionaryManager
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.parser.DumpParserFactory
import com.nfcemulator.nfc.reader.TagReader
import com.nfcemulator.nfc.writer.TagWriter
import com.nfcemulator.nfc.writer.WriteProgress
import com.nfcemulator.storage.EncryptedFileManager
import com.nfcemulator.storage.local.TagDao
import com.nfcemulator.ui.NfcNavigation
import com.nfcemulator.ui.theme.NfcEmulatorTheme
import com.nfcemulator.util.TagMapper
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private val tagReader: TagReader by inject()
    private val tagWriter: TagWriter by inject()
    private val dictionaryManager: DictionaryManager by inject()
    private val dumpParserFactory: DumpParserFactory by inject()
    private val encryptedFileManager: EncryptedFileManager by inject()
    private val tagDao: TagDao by inject()

    private var lastReadTag: Tag? = null

    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importDumpFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        lifecycleScope.launch {
            dictionaryManager.loadDictionaries()
        }

        setContent {
            NfcEmulatorTheme {
                val readProgress by tagReader.progress.collectAsState()

                NfcNavigation(
                    readProgress = readProgress,
                    onImportClick = {
                        importFileLauncher.launch(arrayOf("*/*"))
                    },
                    onSaveTag = { dump -> saveTag(dump) },
                    onResetReader = { tagReader.reset() },
                    onCrackKeys = { dump -> crackRemainingKeys(dump) }
                )
            }
        }

        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent == null) return
        @Suppress("DEPRECATION")
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG) as? Tag ?: return

        // If in write mode, write to the tag instead of reading
        if (tagWriter.progress.value is WriteProgress.WaitingForTag) {
            lifecycleScope.launch {
                val emulatorVm = org.koin.java.KoinJavaComponent.getKoin().get<com.nfcemulator.ui.emulator.EmulatorViewModel>()
                val selectedTag = emulatorVm.uiState.value.selectedTag ?: return@launch
                val rawData = encryptedFileManager.loadDump(selectedTag.id) ?: return@launch

                val dump = com.nfcemulator.dump.model.TagDump(
                    id = selectedTag.id,
                    name = selectedTag.name,
                    type = com.nfcemulator.dump.model.TagType.entries.find { it.displayName == selectedTag.type } ?: com.nfcemulator.dump.model.TagType.UNKNOWN,
                    uid = selectedTag.uid.split(":").map { it.toInt(16).toByte() }.toByteArray(),
                    rawData = rawData,
                    sourceFormat = com.nfcemulator.dump.model.DumpFormat.JSON
                )
                tagWriter.writeTag(tag, dump)
            }
            return
        }

        lastReadTag = tag
        lifecycleScope.launch {
            val keys = dictionaryManager.getAllKeys()
            tagReader.readTag(tag, keys)
        }
    }

    private fun saveTag(dump: TagDump) {
        lifecycleScope.launch {
            val rawBytes = dump.sectors.flatMap { sector ->
                sector.blocks.flatMap { block -> block.data.toList() }
            }.toByteArray()
            val filePath = encryptedFileManager.saveDump(dump.id, rawBytes)
            val entity = TagMapper.toEntity(dump, filePath)
            tagDao.insertTag(entity)
        }
    }

    private fun crackRemainingKeys(dump: TagDump) {
        val tag = lastReadTag ?: return
        lifecycleScope.launch {
            tagReader.reset()
            val allKeys = dictionaryManager.getAllKeys()
            tagReader.readTag(tag, allKeys)
        }
    }

    private fun importDumpFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                val fileName = getFileName(uri) ?: "unknown.bin"
                val inputStream = contentResolver.openInputStream(uri) ?: return@launch
                val dump = dumpParserFactory.parse(inputStream, fileName)
                inputStream.close()

                val rawData = contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                val filePath = encryptedFileManager.saveDump(dump.id, rawData)

                val entity = TagMapper.toEntity(dump, filePath)
                tagDao.insertTag(entity)
            } catch (_: Exception) {
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) return it.getString(nameIndex)
            }
        }
        return uri.lastPathSegment
    }

    private fun enableNfcForegroundDispatch() {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        val intentFilters = arrayOf(
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )
        val techLists = arrayOf(
            arrayOf("android.nfc.tech.MifareClassic"),
            arrayOf("android.nfc.tech.MifareUltralight"),
            arrayOf("android.nfc.tech.IsoDep"),
            arrayOf("android.nfc.tech.NfcA")
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists)
    }
}
