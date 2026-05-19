package ru.handhophop.core.system.database

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
            .addMigrations(
                MIGRATION_1_6,
                MIGRATION_2_6,
                MIGRATION_3_6,
                MIGRATION_4_6,
                MIGRATION_5_6,
            )
            .build()

        return checkNotNull(instance)
    }

    private val MIGRATION_1_6 = object : Migration(1, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            rebuildWorkTableAndCreateSupportTables(db)
        }
    }

    private val MIGRATION_2_6 = object : Migration(2, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            rebuildWorkTableAndCreateSupportTables(db)
        }
    }

    private val MIGRATION_3_6 = object : Migration(3, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            rebuildWorkTableAndCreateSupportTables(db)
        }
    }

    private val MIGRATION_4_6 = object : Migration(4, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE work ADD COLUMN image_path TEXT")
            createActivityDayTable(db)
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            createActivityDayTable(db)
        }
    }
}

private fun rebuildWorkTableAndCreateSupportTables(db: SupportSQLiteDatabase) {
    val columns = db.readTableColumns("work")

    db.execSQL("ALTER TABLE work RENAME TO work_legacy")
    createWorkTable(db)
    createProgressChunkTable(db)
    createActivityDayTable(db)

    db.execSQL(
        """
        INSERT INTO work (
            id, is_favorite, project_name, scheme_type, color_count, difficulty, url, image, image_path,
            grid_width, grid_height, grid_rle, percentage, spent_time
        )
        SELECT
            ${columns.sqlValue("id", "0")},
            ${columns.sqlValue("is_favorite", "0")},
            ${columns.sqlValue("project_name", "NULL")},
            ${columns.sqlValue("scheme_type", "NULL")},
            ${columns.sqlValue("color_count", "NULL")},
            ${columns.sqlValue("difficulty", "NULL")},
            ${columns.sqlValue("url", "NULL")},
            ${columns.sqlValue("image", "NULL")},
            ${columns.sqlValue("image_path", "NULL")},
            ${columns.sqlValue("grid_width", "NULL")},
            ${columns.sqlValue("grid_height", "NULL")},
            ${columns.sqlValue("grid_rle", "NULL")},
            ${columns.sqlValue("percentage", "NULL")},
            ${columns.sqlValue("spent_time", "NULL")}
        FROM work_legacy
        """.trimIndent(),
    )

    migrateLegacyProgressChunks(db)

    db.execSQL("UPDATE work SET grid_rle = NULL WHERE grid_rle IS NOT NULL")
    db.execSQL("DROP TABLE IF EXISTS work_session")
    db.execSQL("DROP TABLE work_legacy")
}

private fun createWorkTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS work (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            is_favorite INTEGER NOT NULL,
            project_name TEXT,
            scheme_type TEXT,
            color_count INTEGER,
            difficulty TEXT,
            url TEXT,
            image BLOB,
            image_path TEXT,
            grid_width INTEGER,
            grid_height INTEGER,
            grid_rle TEXT,
            percentage INTEGER,
            spent_time INTEGER
        )
        """.trimIndent(),
    )
}

private fun createActivityDayTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS work_activity_day (
            work_id INTEGER NOT NULL,
            day TEXT NOT NULL,
            spent_time INTEGER NOT NULL,
            PRIMARY KEY(work_id, day),
            FOREIGN KEY(work_id) REFERENCES work(id) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS index_work_activity_day_work_id ON work_activity_day(work_id)",
    )
}

private fun createProgressChunkTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS work_progress_chunk (
            work_id INTEGER NOT NULL,
            chunk_index INTEGER NOT NULL,
            rle_chunk TEXT NOT NULL,
            PRIMARY KEY(work_id, chunk_index),
            FOREIGN KEY(work_id) REFERENCES work(id) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS index_work_progress_chunk_work_id ON work_progress_chunk(work_id)",
    )
}

private fun migrateLegacyProgressChunks(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        WITH RECURSIVE chunks(work_id, chunk_index, rle_chunk, rest) AS (
            SELECT
                id,
                0,
                substr(grid_rle, 1, $PROGRESS_CHUNK_SIZE),
                substr(grid_rle, ${PROGRESS_CHUNK_SIZE + 1})
            FROM work
            WHERE grid_rle IS NOT NULL AND length(grid_rle) > 0
            UNION ALL
            SELECT
                work_id,
                chunk_index + 1,
                substr(rest, 1, $PROGRESS_CHUNK_SIZE),
                substr(rest, ${PROGRESS_CHUNK_SIZE + 1})
            FROM chunks
            WHERE length(rest) > 0
        )
        INSERT INTO work_progress_chunk(work_id, chunk_index, rle_chunk)
        SELECT work_id, chunk_index, rle_chunk
        FROM chunks
        WHERE length(rle_chunk) > 0
        """.trimIndent(),
    )
}

private fun SupportSQLiteDatabase.readTableColumns(tableName: String): Set<String> {
    val columns = linkedSetOf<String>()
    query("PRAGMA table_info($tableName)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            columns += cursor.getString(nameIndex)
        }
    }
    return columns
}

private fun Set<String>.sqlValue(
    columnName: String,
    fallback: String,
): String {
    return if (columnName in this) {
        columnName
    } else {
        fallback
    }
}
