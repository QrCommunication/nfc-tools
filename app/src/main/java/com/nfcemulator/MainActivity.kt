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
import com.nfcemulator.dump.analyzer.KeyCracker
import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.model.TagType
import com.nfcemulator.dump.model.DumpFormat
import com.nfcemulator.dump.parser.DumpParserFactory
import com.nfcemulator.nfc.hal.NfcEmulatorHal
import com.nfcemulator.nfc.reader.ReadProgress
import com.nfcemulator.nfc.reader.TagReader
import com.nfcemulator.storage.EncryptedFileManager
import com.nfcemulator.storage.local.TagDao
import com.nfcemulator.ui.NfcNavigation
import com.nfcemulator.ui.home.TagUiModel
import com.nfcemulator.ui.theme.NfcEmulatorTheme
import com.nfcemulator.util.TagMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private val tagReader: TagReader by inject()
    private val dictionaryManager: DictionaryManager by inject()
    private val keyCracker: KeyCracker by inject()
    private val dumpParserFactory: DumpParserFactory by inject()
    private val encryptedFileManager: EncryptedFileManager by inject()
    private val tagDao: TagDao by inject()
    private val nfcHal: NfcEmulatorHal by inject()

    private var lastReadTag: Tag? = null
    private val _isEmulating = MutableStateFlow(false)
    private val _emulationMode = MutableStateFlow("HCE Standard (Limited)")

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
            val capabilities = nfcHal.getCapabilities()
            _emulationMode.value = capabilities.emulationMode.displayName
        }

        setContent {
            NfcEmulatorTheme {
                val readProgress by tagReader.progress.collectAsState()
                val isEmulating by nfcHal.isEmulating.collectAsState()
                val emulationMode by _emulationMode.collectAsState()

                NfcNavigation(
                    readProgress = readProgress,
                    isEmulating = isEmulating,
                    emulationMode = emulationMode,
                    onImportClick = {
                        importFileLauncher.launch(arrayOf("*/*"))
                    },
                    onSaveTag = { dump -> saveTag(dump) },
                    onResetReader = { tagReader.reset() },
                    onCrackKeys = { dump -> crackRemainingKeys(dump) },
                    onStartEmulation = { tagUiModel -> startEmulation(tagUiModel) },
                    onStopEmulation = { stopEmulation() }
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

    private fun startEmulation(tagUiModel: TagUiModel) {
        lifecycleScope.launch {
            val rawData = encryptedFileManager.loadDump(tagUiModel.id)
            if (rawData != null) {
                val dump = TagDump(
                    id = tagUiModel.id,
                    name = tagUiModel.name,
                    type = TagType.entries.find { it.displayName == tagUiModel.type } ?: TagType.UNKNOWN,
                    uid = tagUiModel.uid.split(":").map { it.toInt(16).toByte() }.toByteArray(),
                    rawData = rawData,
                    sourceFormat = DumpFormat.JSON
                )
                nfcHal.startEmulation(dump)
            }
        }
    }

    private fun stopEmulation() {
        lifecycleScope.launch {
            nfcHal.stopEmulation()
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
