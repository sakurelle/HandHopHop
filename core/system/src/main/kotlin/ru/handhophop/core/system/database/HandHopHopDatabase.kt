package ru.handhophop.core.system.database

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.handhophop.core.system.database.work.WorkDao
import ru.handhophop.core.system.database.work.WorkActivityDayEntity
import ru.handhophop.core.system.database.work.WorkEntity
import ru.handhophop.core.system.database.work.WorkProgressChunkEntity

private const val PROGRESS_CHUNK_SIZE = 1024

@Database(
    entities = [
        WorkEntity::class,
        WorkProgressChunkEntity::class,
        WorkActivityDayEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class HandHopHopDatabase : RoomDatabase() {

    abstract fun workDao(): WorkDao
}

object HandHopHopDatabaseProvider {
    private var instance: HandHopHopDatabase? = null

    fun get(context: Context): HandHopHopDatabase {
        instance = instance ?: Room.databaseBuilder(
                context = context.applicationContext,
                klass = HandHopHopDatabase::class.java,
                name = "hand_hop_hop.db",
            )
            .fallbackToDestructiveMigration()
            .build()

        return checkNotNull(instance)
    }
}
