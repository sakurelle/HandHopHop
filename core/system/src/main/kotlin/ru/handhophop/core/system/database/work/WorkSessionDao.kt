package ru.handhophop.core.system.database.work

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WorkSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkSessionEntity): Long

    @Query(
        """
        SELECT * FROM work_session
        WHERE work_id = :workId
        ORDER BY started_at ASC
        """
    )
    suspend fun getByWorkId(workId: Long): List<WorkSessionEntity>

    @Query("DELETE FROM work_session WHERE work_id = :workId")
    suspend fun deleteByWorkId(workId: Long)
}
