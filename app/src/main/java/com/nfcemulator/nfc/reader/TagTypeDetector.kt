package com.nfcemulator.nfc.reader

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import com.nfcemulator.dump.model.TagType

object TagTypeDetector {

    fun detect(tag: Tag): TagType {
        val techList = tag.techList

        return when {
            techList.contains(MifareClassic::class.java.name) -> detectMifareClassic(tag)
            techList.contains(MifareUltralight::class.java.name) -> detectMifareUltralight(tag)
            techList.contains(IsoDep::class.java.name) -> detectIsoDep(tag)
            techList.contains(NfcA::class.java.name) -> detectFromSak(tag)
            else -> TagType.UNKNOWN
        }
    }

    private fun detectMifareClassic(tag: Tag): TagType {
        val mifareClassic = MifareClassic.get(tag) ?: return TagType.UNKNOWN
        return try {
            mifareClassic.connect()
            val type = when (mifareClassic.size) {
                MifareClassic.SIZE_1K -> TagType.MIFARE_CLASSIC_1K
                MifareClassic.SIZE_4K -> TagType.MIFARE_CLASSIC_4K
                else -> TagType.MIFARE_CLASSIC_1K
            }
            type
        } catch (_: Exception) {
            TagType.MIFARE_CLASSIC_1K
        } finally {
            try { mifareClassic.close() } catch (_: Exception) {}
        }
    }

    private fun detectMifareUltralight(tag: Tag): TagType {
        val ultralight = MifareUltralight.get(tag) ?: return TagType.MIFARE_ULTRALIGHT
        return try {
            ultralight.connect()
            when (ultralight.type) {
                MifareUltralight.TYPE_ULTRALIGHT -> TagType.MIFARE_ULTRALIGHT
                MifareUltralight.TYPE_ULTRALIGHT_C -> TagType.MIFARE_ULTRALIGHT_C
                else -> TagType.MIFARE_ULTRALIGHT
            }
        } catch (_: Exception) {
            TagType.MIFARE_ULTRALIGHT
        } finally {
            try { ultralight.close() } catch (_: Exception) {}
        }
    }

    private fun detectIsoDep(tag: Tag): TagType {
        val nfcA = NfcA.get(tag)
        if (nfcA != null) {
            val sak = nfcA.sak.toInt()
            if (sak and 0x20 != 0) {
                return TagType.MIFARE_DESFIRE
            }
        }
        return TagType.ISO_14443_4
    }

    private fun detectFromSak(tag: Tag): TagType {
        val nfcA = NfcA.get(tag) ?: return TagType.UNKNOWN
        val sak = nfcA.sak.toInt()
        return when {
            sak == 0x08 -> TagType.MIFARE_CLASSIC_1K
            sak == 0x18 -> TagType.MIFARE_CLASSIC_4K
            sak == 0x00 -> TagType.MIFARE_ULTRALIGHT
            sak and 0x20 != 0 -> TagType.ISO_14443_4
            else -> TagType.UNKNOWN
        }
    }
}
