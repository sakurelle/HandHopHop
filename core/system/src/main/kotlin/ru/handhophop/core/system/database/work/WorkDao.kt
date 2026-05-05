package ru.handhophop.core.system.database.work

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: WorkEntity): Long

    @Update
    suspend fun update(work: WorkEntity)

    @Delete
    suspend fun delete(work: WorkEntity)

    @Query("DELETE FROM work WHERE id = :workId")
    suspend fun deleteById(workId: Long)

    @Query("DELETE FROM work WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("SELECT * FROM work WHERE id = :workId")
    suspend fun getById(workId: Long): WorkEntity?

    @Query("SELECT * FROM work WHERE url = :url ORDER BY id DESC LIMIT 1")
    suspend fun getByUrl(url: String): WorkEntity?

    @Query("SELECT * FROM work WHERE url in (:urls)")
    suspend fun getByUrls(urls: List<String>): List<WorkEntity>

    @Query("SELECT * FROM work WHERE is_favorite = 1 ORDER BY id DESC")
    suspend fun getFavorites(): List<WorkEntity>

    @Query("SELECT * FROM work ORDER BY id DESC")
    suspend fun getAll(): List<WorkEntity>
}
