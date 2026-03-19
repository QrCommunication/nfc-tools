package com.nfcemulator.dump.analyzer

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DictionaryManager(private val context: Context) {

    private var allKeys: List<ByteArray> = emptyList()
    private var keyFrequency: MutableMap<String, Int> = mutableMapOf()
    private var isLoaded = false

    private val dictionaryFiles = listOf(
        "dictionaries/default_keys.txt",
        "dictionaries/mct_extended.txt",
        "dictionaries/flipper_mf_classic.txt",
        "dictionaries/proxmark_default.txt",
        "dictionaries/mfoc_keys.txt",
        "dictionaries/libnfc_keys.txt",
        "dictionaries/icopy_x.txt",
        "dictionaries/rfidresearchgroup.txt",
        "dictionaries/magic_chinese.txt",
        "dictionaries/vigik_public.txt",
        "dictionaries/urmet.txt",
        "dictionaries/came.txt",
        "dictionaries/saflok.txt",
        "dictionaries/salto.txt",
        "dictionaries/dormakaba.txt",
        "dictionaries/intratone.txt"
    )

    suspend fun loadDictionaries() = withContext(Dispatchers.IO) {
        if (isLoaded) return@withContext

        val keysSet = linkedSetOf<String>()

        for (file in dictionaryFiles) {
            try {
                context.assets.open(file).bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val trimmed = line.trim().uppercase()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.length == 12) {
                            if (trimmed.all { it in "0123456789ABCDEF" }) {
                                keysSet.add(trimmed)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Skip missing dictionaries
            }
        }

        allKeys = keysSet.map { hexStringToByteArray(it) }
        isLoaded = true
    }

    fun getDefaultKeys(): List<ByteArray> {
        return listOf(
            hexStringToByteArray("FFFFFFFFFFFF"),
            hexStringToByteArray("A0A1A2A3A4A5"),
            hexStringToByteArray("B0B1B2B3B4B5"),
            hexStringToByteArray("D3F7D3F7D3F7"),
            hexStringToByteArray("000000000000"),
            hexStringToByteArray("4D3A99C351DD"),
            hexStringToByteArray("1A982C7E459A"),
            hexStringToByteArray("AABBCCDDEEFF")
        )
    }

    fun getAllKeys(): List<ByteArray> {
        return if (isLoaded) {
            val sortedKeys = allKeys.sortedByDescending { key ->
                keyFrequency[key.joinToString("") { "%02X".format(it) }] ?: 0
            }
            sortedKeys
        } else {
            getDefaultKeys()
        }
    }

    fun getTotalKeyCount(): Int = allKeys.size

    fun recordKeySuccess(key: ByteArray) {
        val hex = key.joinToString("") { "%02X".format(it) }
        keyFrequency[hex] = (keyFrequency[hex] ?: 0) + 1
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
