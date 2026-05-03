package ru.handhophop.core.system.database.work

import androidx.room.ColumnInfo
import androidx.room.Entity
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
    @ColumnInfo(name = "difficulty")
    val difficulty: String? = null,
    @ColumnInfo(name = "url")
    val url: String?,
    @ColumnInfo(name = "image")
    val image: ByteArray? = null,
    @ColumnInfo(name = "grid_width")
    val gridWidth: Int? = null,
    @ColumnInfo(name = "grid_height")
    val gridHeight: Int? = null,
    @ColumnInfo(name = "grid_rle")
    val gridRle: String? = null,
    @ColumnInfo(name = "percentage")
    val percentage: Int? = null,
    @ColumnInfo(name = "spended_time")
    val spendedTime: Long? = null,
)
