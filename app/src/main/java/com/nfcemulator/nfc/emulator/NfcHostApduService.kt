package com.nfcemulator.nfc.emulator

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.nfcemulator.nfc.hal.HceEmulatorHal
import org.koin.android.ext.android.inject

class NfcHostApduService : HostApduService() {

    private val hceHal: HceEmulatorHal by inject()

    companion object {
        private val SELECT_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val UNKNOWN_CMD = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        val currentDump = hceHal.getCurrentDump() ?: return UNKNOWN_CMD

        // Respond to SELECT AID
        if (commandApdu.size >= 2 && commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xA4.toByte()) {
            return SELECT_OK
        }

        // Respond to READ BINARY with dump data
        if (commandApdu.size >= 4 && commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xB0.toByte()) {
            val offset = ((commandApdu[2].toInt() and 0xFF) shl 8) or (commandApdu[3].toInt() and 0xFF)
            val rawData = currentDump.rawData
            if (offset < rawData.size) {
                val length = if (commandApdu.size > 4) commandApdu[4].toInt() and 0xFF else 16
                val end = minOf(offset + length, rawData.size)
                return rawData.sliceArray(offset until end) + SELECT_OK
            }
            return UNKNOWN_CMD
        }

        return UNKNOWN_CMD
    }

    override fun onDeactivated(reason: Int) {
        // No-op
    }
}
