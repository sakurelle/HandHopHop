package ru.handhophop.core.system.database.work

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "work")
data class WorkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "project_name")
    val projectName: String? = null,

    @ColumnInfo(name = "scheme_type")
    val schemeType: String? = null,

    @ColumnInfo(name = "color_count")
    val colorCount: Int? = null,

    val difficulty: String? = null,
    val url: String? = null,
    val image: ByteArray? = null,

    @ColumnInfo(name = "grid_width")
    val gridWidth: Int? = null,

    @ColumnInfo(name = "grid_height")
    val gridHeight: Int? = null,

    /**
     * Legacy-поле.
     * Новая логика не должна писать сюда длинный RLE.
     * Новая логика пишет прогресс в work_progress_chunk.
     */
    @ColumnInfo(name = "grid_rle")
    val gridRle: String? = null,

    val percentage: Int? = null,

    @ColumnInfo(name = "spended_time")
    val spendedTime: Long? = null,
)

@Entity(
    tableName = "work_progress_chunk",
    primaryKeys = ["work_id", "chunk_index"],
    foreignKeys = [
        ForeignKey(
            entity = WorkEntity::class,
            parentColumns = ["id"],
            childColumns = ["work_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["work_id"]),
    ],
)
data class WorkProgressChunkEntity(
    @ColumnInfo(name = "work_id")
    val workId: Long,

    @ColumnInfo(name = "chunk_index")
    val chunkIndex: Int,

    @ColumnInfo(name = "rle_chunk")
    val rleChunk: String,
)