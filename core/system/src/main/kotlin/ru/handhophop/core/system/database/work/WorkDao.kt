package ru.handhophop.core.system.database.work

import androidx.room.Dao
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

    @Query("SELECT * FROM work WHERE id = :workId")
    suspend fun getById(workId: Long): WorkEntity?

    @Query("SELECT * FROM work ORDER BY id DESC")
    suspend fun getAll(): List<WorkEntity>
}
