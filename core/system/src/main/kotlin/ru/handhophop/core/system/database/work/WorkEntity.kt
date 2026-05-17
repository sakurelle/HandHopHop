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

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,

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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WorkEntity

        if (id != other.id) return false
        if (isFavorite != other.isFavorite) return false
        if (colorCount != other.colorCount) return false
        if (gridWidth != other.gridWidth) return false
        if (gridHeight != other.gridHeight) return false
        if (percentage != other.percentage) return false
        if (spendedTime != other.spendedTime) return false
        if (projectName != other.projectName) return false
        if (schemeType != other.schemeType) return false
        if (difficulty != other.difficulty) return false
        if (url != other.url) return false
        if (!image.contentEquals(other.image)) return false
        if (imagePath != other.imagePath) return false
        if (gridRle != other.gridRle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + (colorCount ?: 0)
        result = 31 * result + (gridWidth ?: 0)
        result = 31 * result + (gridHeight ?: 0)
        result = 31 * result + (percentage ?: 0)
        result = 31 * result + (spendedTime?.hashCode() ?: 0)
        result = 31 * result + (projectName?.hashCode() ?: 0)
        result = 31 * result + (schemeType?.hashCode() ?: 0)
        result = 31 * result + (difficulty?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + (imagePath?.hashCode() ?: 0)
        result = 31 * result + (gridRle?.hashCode() ?: 0)
        return result
    }
}

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

@Entity(
    tableName = "work_activity_day",
    primaryKeys = ["work_id", "day"],
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
data class WorkActivityDayEntity(
    @ColumnInfo(name = "work_id")
    val workId: Long,

    @ColumnInfo(name = "day")
    val day: String,

    @ColumnInfo(name = "spent_time")
    val spentTime: Long,
)
