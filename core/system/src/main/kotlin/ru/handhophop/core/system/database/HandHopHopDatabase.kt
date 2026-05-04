package ru.handhophop.core.system.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.handhophop.core.system.database.work.WorkDao
import ru.handhophop.core.system.database.work.WorkEntity

@Database(
    entities = [
        WorkEntity::class,
    ],
    version = 3,
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
