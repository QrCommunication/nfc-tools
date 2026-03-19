package com.nfcemulator.util

import com.nfcemulator.dump.model.TagDump
import com.nfcemulator.dump.model.TagType
import com.nfcemulator.dump.model.TagCategory
import com.nfcemulator.dump.model.DumpFormat
import com.nfcemulator.storage.local.TagEntity

object TagMapper {

    fun toEntity(dump: TagDump, filePath: String): TagEntity {
        return TagEntity(
            id = dump.id,
            name = dump.name,
            category = dump.category.name,
            type = dump.type.displayName,
            uid = dump.uidHex,
            sak = dump.sak.toInt(),
            atqa = dump.atqaHex,
            sectorCount = dump.sectors.size,
            keysFound = dump.foundKeys,
            keysTotal = dump.totalKeys,
            sourceFormat = dump.sourceFormat.name,
            filePath = filePath,
            notes = dump.notes,
            createdAt = dump.createdAt.toEpochMilli(),
            lastEmulatedAt = null
        )
    }
}
