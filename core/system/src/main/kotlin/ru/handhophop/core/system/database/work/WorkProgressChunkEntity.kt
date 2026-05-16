package ru.handhophop.core.system.database.work

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

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
