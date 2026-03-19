package com.nfcemulator.nfc.emulator

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class NfcHostApduService : HostApduService() {

    companion object {
        private val SELECT_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val UNKNOWN_CMD = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        val dump = EmulationState.currentDump.value ?: return UNKNOWN_CMD

        if (commandApdu.size < 2) return UNKNOWN_CMD

        // SELECT AID
        if (commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xA4.toByte()) {
            return SELECT_OK
        }

        // READ BINARY
        if (commandApdu[0] == 0x00.toByte() && commandApdu[1] == 0xB0.toByte() && commandApdu.size >= 4) {
            val offset = ((commandApdu[2].toInt() and 0xFF) shl 8) or (commandApdu[3].toInt() and 0xFF)
            val rawData = dump.rawData
            if (rawData.isEmpty()) {
                // Build raw data from sectors
                val sectorData = dump.sectors.flatMap { sector ->
                    sector.blocks.flatMap { block -> block.data.toList() }
                }.toByteArray()
                if (offset < sectorData.size) {
                    val length = if (commandApdu.size > 4) commandApdu[4].toInt() and 0xFF else 16
                    val end = minOf(offset + length, sectorData.size)
                    return sectorData.sliceArray(offset until end) + SELECT_OK
                }
            } else if (offset < rawData.size) {
                val length = if (commandApdu.size > 4) commandApdu[4].toInt() and 0xFF else 16
                val end = minOf(offset + length, rawData.size)
                return rawData.sliceArray(offset until end) + SELECT_OK
            }
            return UNKNOWN_CMD
        }

        // GET UID — custom command to return tag UID
        if (commandApdu[0] == 0xFF.toByte() && commandApdu[1] == 0xCA.toByte()) {
            return dump.uid + SELECT_OK
        }

        return UNKNOWN_CMD
    }

    override fun onDeactivated(reason: Int) {
        // Keep emulation state active
    }
}
