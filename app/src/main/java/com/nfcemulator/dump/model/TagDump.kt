package com.nfcemulator.dump.model

import java.time.Instant
import java.util.UUID

data class TagDump(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: TagCategory = TagCategory.OTHER,
    val type: TagType,
    val uid: ByteArray,
    val atqa: ByteArray = byteArrayOf(),
    val sak: Byte = 0,
    val sectors: List<Sector> = emptyList(),
    val rawData: ByteArray = byteArrayOf(),
    val sourceFormat: DumpFormat,
    val createdAt: Instant = Instant.now(),
    val notes: String = ""
) {
    val uidHex: String
        get() = uid.joinToString(":") { "%02X".format(it) }

    val atqaHex: String
        get() = atqa.joinToString(":") { "%02X".format(it) }

    val sakHex: String
        get() = "%02X".format(sak)

    val totalKeys: Int
        get() = sectors.size * 2

    val foundKeys: Int
        get() = sectors.count { it.keyA != null } + sectors.count { it.keyB != null }

    val decryptionProgress: Float
        get() = if (totalKeys > 0) foundKeys.toFloat() / totalKeys else 0f

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TagDump) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
