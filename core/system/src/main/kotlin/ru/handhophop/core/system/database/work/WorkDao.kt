package ru.handhophop.core.system.database.work

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface WorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(work: WorkEntity): Long

    @Query("DELETE FROM work")
    suspend fun deleteAll()

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

    //ВОзможный фикс для row is too big (не беру тяжкие поля)
    @Query(
        """
    SELECT 
        url,
        is_favorite AS isFavorite,
        CASE 
            WHEN project_name IS NOT NULL
              OR scheme_type IS NOT NULL
              OR color_count IS NOT NULL
              OR difficulty IS NOT NULL
              OR grid_width IS NOT NULL
              OR grid_height IS NOT NULL
              OR percentage IS NOT NULL
              OR spended_time IS NOT NULL
            THEN 1 ELSE 0 
        END AS isStarted,
        percentage,
        project_name AS projectName
    FROM work 
    WHERE url IN (:urls)
    """
    )
    suspend fun getFeedMetaByUrls(urls: List<String>): List<WorkFeedMeta>

    @Query(
        """
        SELECT 
            id,
            url,
            is_favorite AS isFavorite,
            CASE 
                WHEN project_name IS NOT NULL
                  OR scheme_type IS NOT NULL
                  OR color_count IS NOT NULL
                  OR difficulty IS NOT NULL
                  OR grid_width IS NOT NULL
                  OR grid_height IS NOT NULL
                  OR percentage IS NOT NULL
                  OR spended_time IS NOT NULL
                THEN 1 ELSE 0 
            END AS isStarted,
            percentage,
            project_name AS projectName
        FROM work
        WHERE is_favorite = 1
           OR project_name IS NOT NULL
           OR scheme_type IS NOT NULL
           OR color_count IS NOT NULL
           OR difficulty IS NOT NULL
           OR grid_width IS NOT NULL
           OR grid_height IS NOT NULL
           OR percentage IS NOT NULL
           OR spended_time IS NOT NULL
        ORDER BY id DESC
        """
    )
    suspend fun getBookmarkPreviews(): List<WorkBookmarkPreview>
}
