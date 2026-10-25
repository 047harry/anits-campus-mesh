package `in`.anits.mesh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import `in`.anits.mesh.data.dao.*
import `in`.anits.mesh.data.entity.*

@Database(
    entities = [
        MessageEntity::class,
        PeerEntity::class,
        KeyRingEntity::class,
        SeenCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MeshDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun peerDao(): PeerDao
    abstract fun keyRingDao(): KeyRingDao
    abstract fun seenCacheDao(): SeenCacheDao

    companion object {
        @Volatile
        private var INSTANCE: MeshDatabase? = null

        // Migration v1 -> v2: add is_read column (default 0 = unread)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE table_messages ADD COLUMN is_read INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): MeshDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeshDatabase::class.java,
                    "campus_mesh.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}