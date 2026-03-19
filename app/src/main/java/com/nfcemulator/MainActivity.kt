package com.nfcemulator

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.nfcemulator.nfc.reader.ReadProgress
import com.nfcemulator.nfc.reader.TagReader
import com.nfcemulator.dump.analyzer.DictionaryManager
import com.nfcemulator.ui.NfcNavigation
import com.nfcemulator.ui.theme.NfcEmulatorTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private val tagReader: TagReader by inject()
    private val dictionaryManager: DictionaryManager by inject()

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
                    onImportClick = { }
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
        val tag: Tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java) ?: return

        lifecycleScope.launch {
            val keys = dictionaryManager.getAllKeys()
            tagReader.readTag(tag, keys)
        }
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
