package com.example.job_connect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main local Room database for JobConnect.
 *
 * The database is created as a singleton so that the application
 * uses only one database connection.
 */
@Database(
    entities = [CachedJobEntity::class],
    version = 1,
    exportSchema = false
)
abstract class JobConnectDatabase : RoomDatabase() {

    abstract fun cachedJobDao(): CachedJobDao

    companion object {

        @Volatile
        private var databaseInstance:
                JobConnectDatabase? = null

        fun getDatabase(
            context: Context
        ): JobConnectDatabase {

            return databaseInstance
                ?: synchronized(this) {

                    val newInstance =
                        Room.databaseBuilder(
                            context.applicationContext,
                            JobConnectDatabase::class.java,
                            "job_connect_database"
                        ).build()

                    databaseInstance = newInstance

                    newInstance
                }
        }
    }
}