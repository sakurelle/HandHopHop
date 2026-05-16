package ru.handhophop.core.system.database.work

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

data class WorkFavoritePreview(
    val id: Long,
    val url: String?,
    val image: ByteArray?,
)

data class WorkUrlPreview(
    val id: Long,
    @androidx.room.ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
    @androidx.room.ColumnInfo(name = "project_name")
    val projectName: String?,
    @androidx.room.ColumnInfo(name = "scheme_type")
    val schemeType: String?,
    @androidx.room.ColumnInfo(name = "color_count")
    val colorCount: Int?,
    val difficulty: String?,
    val url: String?,
    @androidx.room.ColumnInfo(name = "grid_width")
    val gridWidth: Int?,
    @androidx.room.ColumnInfo(name = "grid_height")
    val gridHeight: Int?,
    val percentage: Int?,
    @androidx.room.ColumnInfo(name = "spended_time")
    val spendedTime: Long?,
)

data class WorkDetailsPreview(
    val id: Long,
    @androidx.room.ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
    @androidx.room.ColumnInfo(name = "project_name")
    val projectName: String?,
    @androidx.room.ColumnInfo(name = "scheme_type")
    val schemeType: String?,
    @androidx.room.ColumnInfo(name = "color_count")
    val colorCount: Int?,
    val difficulty: String?,
    val url: String?,
    @androidx.room.ColumnInfo(name = "grid_width")
    val gridWidth: Int?,
    @androidx.room.ColumnInfo(name = "grid_height")
    val gridHeight: Int?,
    val percentage: Int?,
    @androidx.room.ColumnInfo(name = "spended_time")
    val spendedTime: Long?,
)

@Dao
interface WorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: WorkEntity): Long

    @Query("DELETE FROM work")
    suspend fun deleteAll()

    @Query("DELETE FROM work_progress_chunk")
    suspend fun deleteAllProgressChunks()

    @Transaction
    suspend fun deleteAllWithProgress() {
        deleteAllProgressChunks()
        deleteAll()
    }

    @Query("SELECT COUNT(*) FROM work")
    suspend fun getCount(): Int

    @RawQuery
    suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
    @Update
    suspend fun update(work: WorkEntity)

    @Delete
    suspend fun delete(work: WorkEntity)

    @Query("DELETE FROM work WHERE id = :workId")
    suspend fun deleteById(workId: Long)

    @Transaction
    suspend fun deleteByIdWithProgress(workId: Long) {
        deleteProgressChunks(workId)
        deleteById(workId)
    }

    @Query("DELETE FROM work WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Transaction
    suspend fun deleteByUrlWithProgress(url: String) {
        getDetailsByUrl(url)?.let { deleteProgressChunks(it.id) }
        deleteByUrl(url)
    }

    @Query(
        """
        SELECT id, is_favorite, project_name, scheme_type, color_count, difficulty, url,
            grid_width, grid_height, percentage, spended_time
        FROM work
        WHERE id = :workId
        """,
    )
    suspend fun getDetailsById(workId: Long): WorkDetailsPreview?

    @Query(
        """
        SELECT id, is_favorite, project_name, scheme_type, color_count, difficulty, url,
            grid_width, grid_height, percentage, spended_time
        FROM work
        WHERE url = :url
        ORDER BY id DESC
        LIMIT 1
        """,
    )
    suspend fun getDetailsByUrl(url: String): WorkDetailsPreview?

    @Query("SELECT image FROM work WHERE id = :workId")
    suspend fun getImageById(workId: Long): ByteArray?

    @Query("SELECT grid_rle FROM work WHERE id = :workId")
    suspend fun getLegacyGridRleById(workId: Long): String?

    @Query(
        """
        SELECT id, is_favorite, project_name, scheme_type, color_count, difficulty, url,
            grid_width, grid_height, percentage, spended_time
        FROM work
        WHERE url in (:urls)
        """,
    )
    suspend fun getByUrls(urls: List<String>): List<WorkUrlPreview>

    @Query("SELECT id, url, image FROM work WHERE is_favorite = 1 ORDER BY id DESC")
    suspend fun getFavorites(): List<WorkFavoritePreview>

    @Query("SELECT is_favorite FROM work WHERE url = :url ORDER BY id DESC LIMIT 1")
    suspend fun isFavoriteByUrl(url: String): Boolean?

    @Query(
        """
        SELECT id, is_favorite, project_name, scheme_type, color_count, difficulty, url,
            grid_width, grid_height, percentage, spended_time
        FROM work
        ORDER BY id DESC
        """,
    )
    suspend fun getAll(): List<WorkDetailsPreview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressChunks(chunks: List<WorkProgressChunkEntity>)

    @Query("DELETE FROM work_progress_chunk WHERE work_id = :workId")
    suspend fun deleteProgressChunks(workId: Long)

    @Query("SELECT rle_chunk FROM work_progress_chunk WHERE work_id = :workId ORDER BY chunk_index ASC")
    suspend fun getProgressChunks(workId: Long): List<String>

    @Query("UPDATE work SET grid_rle = NULL WHERE id = :workId")
    suspend fun clearLegacyProgress(workId: Long)

    @Transaction
    suspend fun replaceProgressChunks(
        workId: Long,
        rleChunks: List<String>,
    ) {
        deleteProgressChunks(workId)
        if (rleChunks.isNotEmpty()) {
            insertProgressChunks(
                rleChunks.mapIndexed { index, chunk ->
                    WorkProgressChunkEntity(
                        workId = workId,
                        chunkIndex = index,
                        rleChunk = chunk,
                    )
                },
            )
        }
    }

    @Transaction
    suspend fun insertWithProgress(
        work: WorkEntity,
        rleChunks: List<String>,
    ): Long {
        val workId = insert(work)
        replaceProgressChunks(workId, rleChunks)
        return workId
    }

    @Transaction
    suspend fun updateWithProgress(
        work: WorkEntity,
        rleChunks: List<String>,
    ) {
        update(work)
        replaceProgressChunks(work.id, rleChunks)
    }
}
