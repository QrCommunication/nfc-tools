package com.nfcemulator.storage.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val type: String,
    val uid: String,
    val sak: Int,
    val atqa: String,
    val sectorCount: Int,
    val keysFound: Int,
    val keysTotal: Int,
    val sourceFormat: String,
    val filePath: String,
    val notes: String,
    val createdAt: Long,
    val lastEmulatedAt: Long?
)
