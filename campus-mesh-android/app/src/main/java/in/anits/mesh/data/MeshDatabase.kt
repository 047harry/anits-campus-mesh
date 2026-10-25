package `in`.anits.mesh.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import `in`.anits.mesh.data.dao.*
import `in`.anits.mesh.data.entity.*

@Database(
    entities = [
        MessageEntity::class,
        PeerEntity::class,
        KeyRingEntity::class,
        SeenCacheEntity::class
    ],
    version = 1,
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

        fun getDatabase(context: Context): MeshDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeshDatabase::class.java,
                    "campus_mesh.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
