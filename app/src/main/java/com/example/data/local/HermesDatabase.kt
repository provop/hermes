package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HermesMessageEntity::class,
        HermesActionLogEntity::class,
        HermesSettingsEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class HermesDatabase : RoomDatabase() {
    abstract fun hermesDao(): HermesDao

    companion object {
        @Volatile
        private var INSTANCE: HermesDatabase? = null

        fun getInstance(context: Context): HermesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HermesDatabase::class.java,
                    "hermes_assistant_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
