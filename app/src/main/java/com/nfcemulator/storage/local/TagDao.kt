package com.nfcemulator.storage.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY createdAt DESC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: String): TagEntity?

    @Query("SELECT * FROM tags WHERE category = :category ORDER BY createdAt DESC")
    fun getTagsByCategory(category: String): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' OR uid LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchTags(query: String): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity)

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: String)

    @Query("UPDATE tags SET lastEmulatedAt = :timestamp WHERE id = :id")
    suspend fun updateLastEmulated(id: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int

    @Query("SELECT * FROM tags ORDER BY createdAt DESC")
    fun getAllTagsSync(): List<TagEntity>
}
