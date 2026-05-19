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

    @ColumnInfo(name = "image_path")
    val imagePath: String?,
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

    @ColumnInfo(name = "image_path")
    val imagePath: String?,

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

    @ColumnInfo(name = "image_path")
    val imagePath: String?,

    @ColumnInfo(name = "grid_width")
    val gridWidth: Int?,

    @ColumnInfo(name = "grid_height")
    val gridHeight: Int?,

    val percentage: Int?,

    @ColumnInfo(name = "spended_time")
    val spendedTime: Long?,
)

data class WorkActivityStats(
    val todaySpentTimeMillis: Long = 0L,
    val weekSpentTimeMillisByDay: List<Long> = List(7) { 0L },
)

@Dao
interface WorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: WorkEntity): Long

    @Query("DELETE FROM work")
    suspend fun deleteAll()

    @Query("DELETE FROM work_progress_chunk")
    suspend fun deleteAllProgressChunks()

    @Query("DELETE FROM work_activity_day")
    suspend fun deleteAllActivityDays()

    @Transaction
    suspend fun deleteAllWithProgress() {
        deleteAllActivityDays()
        deleteAllProgressChunks()
        deleteAll()
    }

    @Query("SELECT COUNT(*) FROM work")
    suspend fun getCount(): Int

    @Query("DELETE FROM work WHERE id = :workId")
    suspend fun deleteById(workId: Long)

    @Query("DELETE FROM work_progress_chunk WHERE work_id = :workId")
    suspend fun deleteProgressChunks(workId: Long)

    @Query("DELETE FROM work_activity_day WHERE work_id = :workId")
    suspend fun deleteActivityDaysByWorkId(workId: Long)

    @Transaction
    suspend fun deleteByIdWithProgress(workId: Long) {
        deleteActivityDaysByWorkId(workId)
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

    @Query(
        """
        DELETE FROM work_activity_day
        WHERE work_id IN (
            SELECT id
            FROM work
            WHERE url = :url
        )
        """,
    )
    suspend fun deleteActivityDaysByUrl(url: String)

    @Transaction
    suspend fun deleteByUrlWithProgress(url: String) {
        deleteActivityDaysByUrl(url)
        deleteProgressChunksByUrl(url)
        deleteByUrl(url)
    }

    @Query(
        """
        SELECT id, is_favorite, project_name, scheme_type, color_count, difficulty, url,
               image_path, grid_width, grid_height, percentage, spended_time
        FROM work
        WHERE id = :workId
        """,
    )
    suspend fun getDetailsById(workId: Long): WorkDetailsPreview?

    @Query(
        """
        SELECT id, is_favorite, project_name, scheme_type, color_count, difficulty, url,
               image_path, grid_width, grid_height, percentage, spended_time
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
            CASE
                WHEN percentage IS NOT NULL
                    AND percentage > 0
                THEN 0
                WHEN EXISTS (
                    SELECT 1
                    FROM work_progress_chunk
                    WHERE work_progress_chunk.work_id = work.id
                    LIMIT 1
                )
                THEN 0
                ELSE 1
            END,
            id DESC
        LIMIT 1
        """,
    )
    suspend fun getDetailsByUrl(url: String): WorkDetailsPreview?

    @Query("SELECT grid_rle FROM work WHERE id = :workId")
    suspend fun getLegacyGridRleById(workId: Long): String?

    @Query(
        """
        SELECT w.id, w.is_favorite, w.project_name, w.scheme_type, w.color_count, w.difficulty,
               w.url, w.image_path, w.grid_width, w.grid_height, w.percentage, w.spended_time
        FROM work AS w
        WHERE w.url IN (:urls)
            AND w.id = (
                SELECT w2.id
                FROM work AS w2
                WHERE w2.url = w.url
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
        """,
    )
    suspend fun getByUrls(urls: List<String>): List<WorkUrlPreview>

    @Query(
        """
        SELECT
            w.url AS url,
            w.is_favorite AS isFavorite,
            CASE
                WHEN (
                    w.project_name IS NOT NULL
                    AND w.project_name != ''
                    AND w.scheme_type IS NOT NULL
                    AND w.scheme_type != ''
                    AND w.color_count IS NOT NULL
                    AND w.color_count > 0
                    AND w.difficulty IS NOT NULL
                    AND w.difficulty != ''
                )
                OR (
                    w.percentage IS NOT NULL
                    AND w.percentage > 0
                )
                OR EXISTS (
                    SELECT 1
                    FROM work_progress_chunk
                    WHERE work_progress_chunk.work_id = w.id
                    LIMIT 1
                )
                THEN 1
                ELSE 0
            END AS isStarted,
            w.percentage AS percentage,
            w.project_name AS projectName
        FROM work AS w
        WHERE w.url IN (:urls)
            AND w.id = (
                SELECT w2.id
                FROM work AS w2
                WHERE w2.url = w.url
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
        """,
    )
    suspend fun getFeedMetaByUrls(urls: List<String>): List<WorkFeedMeta>

    @Query(
        """
        SELECT w.id, w.url, w.image_path
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

    @Query(
        """
        SELECT
            w.id AS id,
            w.url AS url,
            w.image_path AS imagePath,
            1 AS isFavorite,
            CASE
                WHEN (
                    w.project_name IS NOT NULL
                    AND w.project_name != ''
                    AND w.scheme_type IS NOT NULL
                    AND w.scheme_type != ''
                    AND w.color_count IS NOT NULL
                    AND w.color_count > 0
                    AND w.difficulty IS NOT NULL
                    AND w.difficulty != ''
                )
                OR (
                    w.percentage IS NOT NULL
                    AND w.percentage > 0
                )
                OR EXISTS (
                    SELECT 1
                    FROM work_progress_chunk
                    WHERE work_progress_chunk.work_id = w.id
                    LIMIT 1
                )
                THEN 1
                ELSE 0
            END AS isStarted,
            w.percentage AS percentage,
            w.project_name AS projectName
        FROM work AS w
        WHERE w.url IS NOT NULL
            AND w.url != ''
            AND EXISTS (
                SELECT 1
                FROM work AS wf
                WHERE wf.url = w.url
                    AND wf.is_favorite = 1
            )
            AND w.id = (
                SELECT w2.id
                FROM work AS w2
                WHERE w2.url = w.url
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
    suspend fun getBookmarkPreviews(): List<WorkBookmarkPreview>

    @Query("SELECT is_favorite FROM work WHERE url = :url ORDER BY id DESC LIMIT 1")
    suspend fun isFavoriteByUrl(url: String): Boolean?

    @Query(
        """
        SELECT id, is_favorite, project_name, scheme_type, color_count, difficulty, url,
               image_path, grid_width, grid_height, percentage, spended_time
        FROM work
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
            CASE
                WHEN percentage IS NOT NULL
                    AND percentage > 0
                THEN 0
                WHEN EXISTS (
                    SELECT 1
                    FROM work_progress_chunk
                    WHERE work_progress_chunk.work_id = work.id
                    LIMIT 1
                )
                THEN 0
                ELSE 1
            END,
            id DESC
        """,
    )
    suspend fun getAll(): List<WorkUrlPreview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressChunks(chunks: List<WorkProgressChunkEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertActivityDay(activity: WorkActivityDayEntity): Long

    @Query(
        """
        UPDATE work_activity_day
        SET spent_time = spent_time + :spentTimeToAdd
        WHERE work_id = :workId AND day = :day
        """,
    )
    suspend fun addSpentTimeToActivityDay(
        workId: Long,
        day: String,
        spentTimeToAdd: Long,
    )

    @Query(
        """
        SELECT *
        FROM work_activity_day
        WHERE work_id = :workId
            AND day BETWEEN :startDay AND :endDay
        ORDER BY day ASC
        """,
    )
    suspend fun getActivityDays(
        workId: Long,
        startDay: String,
        endDay: String,
    ): List<WorkActivityDayEntity>

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

    @Query("SELECT image_path FROM work WHERE id = :workId")
    suspend fun getImagePathById(workId: Long): String?

    @Query("SELECT image_path FROM work WHERE url = :url")
    suspend fun getImagePathsByUrl(url: String): List<String?>

    @Query("SELECT image_path FROM work WHERE image_path IS NOT NULL AND image_path != ''")
    suspend fun getAllImagePaths(): List<String>

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

    @Transaction
    suspend fun addWorkActivityTime(
        workId: Long,
        day: String,
        spentTimeToAdd: Long,
    ) {
        if (spentTimeToAdd <= 0L) {
            return
        }

        val inserted = insertActivityDay(
            WorkActivityDayEntity(
                workId = workId,
                day = day,
                spentTime = spentTimeToAdd,
            ),
        )

        if (inserted == -1L) {
            addSpentTimeToActivityDay(
                workId = workId,
                day = day,
                spentTimeToAdd = spentTimeToAdd,
            )
        }
    }

    @Query(
        """
        UPDATE work
        SET url = :url,
            image = NULL,
            image_path = COALESCE(:imagePath, image_path),
            is_favorite = 1
        WHERE id = :id
        """,
    )
    suspend fun updateFavoriteById(
        id: Long,
        url: String,
        imagePath: String?,
    )

    @Query(
        """
        UPDATE work
        SET url = :url,
            image = NULL,
            image_path = COALESCE(:imagePath, image_path),
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
        imagePath: String?,
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
