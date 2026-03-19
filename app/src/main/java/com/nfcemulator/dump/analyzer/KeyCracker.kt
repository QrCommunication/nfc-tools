package com.nfcemulator.dump.analyzer

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class CrackProgress(
    val currentSector: Int,
    val totalSectors: Int,
    val keysFound: Int,
    val keysTotal: Int,
    val currentPhase: CrackPhase,
    val currentKeyIndex: Int,
    val totalKeysToTest: Int
)

enum class CrackPhase(val displayName: String) {
    IDLE("Idle"),
    DEFAULT_KEYS("Testing default keys"),
    EXTENDED_KEYS("Testing extended dictionary"),
    ALL_KEYS("Testing all dictionaries"),
    COMPLETE("Complete")
}

data class CrackResult(
    val sectorKeys: Map<Int, SectorKeys>,
    val totalSectors: Int,
    val crackedSectors: Int,
    val duration: Long
)

data class SectorKeys(
    val keyA: ByteArray? = null,
    val keyB: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SectorKeys) return false
        return keyA.contentEquals(other.keyA) && keyB.contentEquals(other.keyB)
    }

    override fun hashCode(): Int {
        var result = keyA?.contentHashCode() ?: 0
        result = 31 * result + (keyB?.contentHashCode() ?: 0)
        return result
    }
}

class KeyCracker(private val dictionaryManager: DictionaryManager) {

    private val _progress = MutableStateFlow(
        CrackProgress(0, 0, 0, 0, CrackPhase.IDLE, 0, 0)
    )
    val progress: StateFlow<CrackProgress> = _progress.asStateFlow()

    suspend fun crack(tag: Tag): CrackResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val mfc = MifareClassic.get(tag) ?: return@withContext CrackResult(emptyMap(), 0, 0, 0)

        val sectorKeys = mutableMapOf<Int, SectorKeys>()

        try {
            mfc.connect()
            val sectorCount = mfc.sectorCount
            val totalKeys = sectorCount * 2
            var keysFound = 0

            // Phase 1: Default keys
            val defaultKeys = dictionaryManager.getDefaultKeys()
            _progress.value = CrackProgress(0, sectorCount, 0, totalKeys, CrackPhase.DEFAULT_KEYS, 0, defaultKeys.size)

            for (sector in 0 until sectorCount) {
                val keys = tryKeys(mfc, sector, defaultKeys)
                if (keys.keyA != null || keys.keyB != null) {
                    sectorKeys[sector] = keys
                    keysFound += (if (keys.keyA != null) 1 else 0) + (if (keys.keyB != null) 1 else 0)
                    keys.keyA?.let { dictionaryManager.recordKeySuccess(it) }
                    keys.keyB?.let { dictionaryManager.recordKeySuccess(it) }
                }
                _progress.value = CrackProgress(sector + 1, sectorCount, keysFound, totalKeys, CrackPhase.DEFAULT_KEYS, 0, defaultKeys.size)
            }

            // Phase 2: All dictionary keys for remaining sectors
            val uncrackedSectors = (0 until sectorCount).filter { it !in sectorKeys }
            if (uncrackedSectors.isNotEmpty()) {
                dictionaryManager.loadDictionaries()
                val allKeys = dictionaryManager.getAllKeys()
                _progress.value = _progress.value.copy(currentPhase = CrackPhase.ALL_KEYS, totalKeysToTest = allKeys.size)

                for (sector in uncrackedSectors) {
                    val keys = tryKeys(mfc, sector, allKeys) { keyIndex ->
                        _progress.value = _progress.value.copy(
                            currentSector = sector,
                            currentKeyIndex = keyIndex,
                            keysFound = keysFound
                        )
                    }
                    if (keys.keyA != null || keys.keyB != null) {
                        sectorKeys[sector] = keys
                        keysFound += (if (keys.keyA != null) 1 else 0) + (if (keys.keyB != null) 1 else 0)
                        keys.keyA?.let { dictionaryManager.recordKeySuccess(it) }
                        keys.keyB?.let { dictionaryManager.recordKeySuccess(it) }
                    }
                }
            }

            _progress.value = CrackProgress(sectorCount, sectorCount, keysFound, totalKeys, CrackPhase.COMPLETE, 0, 0)
        } catch (_: Exception) {
            // Return what we have
        } finally {
            try { mfc.close() } catch (_: Exception) {}
        }

        val duration = System.currentTimeMillis() - startTime
        CrackResult(
            sectorKeys = sectorKeys,
            totalSectors = mfc.sectorCount,
            crackedSectors = sectorKeys.size,
            duration = duration
        )
    }

    private fun tryKeys(
        mfc: MifareClassic,
        sector: Int,
        keys: List<ByteArray>,
        onProgress: ((Int) -> Unit)? = null
    ): SectorKeys {
        var keyA: ByteArray? = null
        var keyB: ByteArray? = null

        for ((index, key) in keys.withIndex()) {
            onProgress?.invoke(index)

            if (keyA == null) {
                try {
                    if (mfc.authenticateSectorWithKeyA(sector, key)) {
                        keyA = key.copyOf()
                    }
                } catch (_: Exception) {}
            }

            if (keyB == null) {
                try {
                    if (mfc.authenticateSectorWithKeyB(sector, key)) {
                        keyB = key.copyOf()
                    }
                } catch (_: Exception) {}
            }

            if (keyA != null && keyB != null) break
        }

        return SectorKeys(keyA, keyB)
    }

    fun reset() {
        _progress.value = CrackProgress(0, 0, 0, 0, CrackPhase.IDLE, 0, 0)
    }
}
