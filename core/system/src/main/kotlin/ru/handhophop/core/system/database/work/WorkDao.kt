package ru.handhophop.core.system.database.work

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

data class WorkFavoritePreview(
    val id: Long,
    val url: String?,
    val image: ByteArray?,
)

data class WorkUrlPreview(
    val id: Long,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "project_name")
    val projectName: String?,

    @ColumnInfo(name = "scheme_type")
    val schemeType: String?,

    @ColumnInfo(name = "color_count")
    val colorCount: Int?,

    val difficulty: String?,
    val url: String?,

    @ColumnInfo(name = "grid_width")
    val gridWidth: Int?,

    @ColumnInfo(name = "grid_height")
    val gridHeight: Int?,

    val percentage: Int?,

    @ColumnInfo(name = "spended_time")
    val spendedTime: Long?,
)

data class WorkDetailsPreview(
    val id: Long,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "project_name")
    val projectName: String?,

    @ColumnInfo(name = "scheme_type")
    val schemeType: String?,

    @ColumnInfo(name = "color_count")
    val colorCount: Int?,

    val difficulty: String?,
    val url: String?,

    @ColumnInfo(name = "grid_width")
    val gridWidth: Int?,

    @ColumnInfo(name = "grid_height")
    val gridHeight: Int?,

    val percentage: Int?,

    @ColumnInfo(name = "spended_time")
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

    @Query("DELETE FROM work WHERE id = :workId")
    suspend fun deleteById(workId: Long)

    @Query("DELETE FROM work_progress_chunk WHERE work_id = :workId")
    suspend fun deleteProgressChunks(workId: Long)

    @Transaction
    suspend fun deleteByIdWithProgress(workId: Long) {
        deleteProgressChunks(workId)
        deleteById(workId)
    }

    @Query("DELETE FROM work WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query(
        """
        DELETE FROM work_progress_chunk
        WHERE work_id IN (
            SELECT id
            FROM work
            WHERE url = :url
        )
        """,
    )
    suspend fun deleteProgressChunksByUrl(url: String)

    @Transaction
    suspend fun deleteByUrlWithProgress(url: String) {
        deleteProgressChunksByUrl(url)
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
    ORDER BY
        CASE
            WHEN project_name IS NOT NULL
                AND project_name != ''
                AND scheme_type IS NOT NULL
                AND scheme_type != ''
                AND color_count IS NOT NULL
                AND color_count > 0
                AND difficulty IS NOT NULL
                AND difficulty != ''
            THEN 0
            ELSE 1
        END,
        id DESC
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
        WHERE url IN (:urls)
        """,
    )
    suspend fun getByUrls(urls: List<String>): List<WorkUrlPreview>

    @Query(
        """
    SELECT w.id, w.url, NULL AS image
    FROM work AS w
    WHERE w.is_favorite = 1
        AND w.url IS NOT NULL
        AND w.url != ''
        AND w.id = (
            SELECT w2.id
            FROM work AS w2
            WHERE w2.url = w.url
                AND w2.is_favorite = 1
            ORDER BY
                CASE
                    WHEN w2.project_name IS NOT NULL
                        AND w2.project_name != ''
                        AND w2.scheme_type IS NOT NULL
                        AND w2.scheme_type != ''
                        AND w2.color_count IS NOT NULL
                        AND w2.color_count > 0
                        AND w2.difficulty IS NOT NULL
                        AND w2.difficulty != ''
                    THEN 0
                    ELSE 1
                END,
                CASE
                    WHEN w2.percentage IS NOT NULL
                        AND w2.percentage > 0
                    THEN 0
                    WHEN EXISTS (
                        SELECT 1
                        FROM work_progress_chunk
                        WHERE work_progress_chunk.work_id = w2.id
                        LIMIT 1
                    )
                    THEN 0
                    ELSE 1
                END,
                w2.id DESC
            LIMIT 1
        )
    ORDER BY w.id DESC
    """,
    )
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
    suspend fun getAll(): List<WorkUrlPreview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressChunks(chunks: List<WorkProgressChunkEntity>)

    @Query(
        """
    SELECT rle_chunk
    FROM work_progress_chunk
    WHERE work_id = :workId
    ORDER BY chunk_index ASC
    """,
    )
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

        clearLegacyProgress(workId)
    }

    @Query(
        """
        UPDATE work
        SET url = :url,
            image = COALESCE(:image, image),
            is_favorite = 1
        WHERE id = :id
        """,
    )
    suspend fun updateFavoriteById(
        id: Long,
        url: String,
        image: ByteArray?,
    )

    @Query(
        """
        UPDATE work
        SET url = :url,
            image = COALESCE(:image, image),
            project_name = :projectName,
            scheme_type = :schemeType,
            color_count = :colorCount,
            difficulty = :difficulty,
            grid_width = :gridWidth,
            grid_height = :gridHeight,
            grid_rle = NULL,
            percentage = :percentage,
            spended_time = :spendedTime
        WHERE id = :id
        """,
    )
    suspend fun updateWorkById(
        id: Long,
        url: String,
        image: ByteArray?,
        projectName: String?,
        schemeType: String?,
        colorCount: Int?,
        difficulty: String?,
        gridWidth: Int?,
        gridHeight: Int?,
        percentage: Int?,
        spendedTime: Long?,
    )
}