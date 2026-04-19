package ru.handhophop.core.system.database.work

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work")
data class WorkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "grid_width")
    val gridWidth: Int,
    @ColumnInfo(name = "grid_height")
    val gridHeight: Int,
    @ColumnInfo(name = "grid_rle")
    val gridRle: String,
    @ColumnInfo(name = "percentage")
    val percentage: Int,
)
