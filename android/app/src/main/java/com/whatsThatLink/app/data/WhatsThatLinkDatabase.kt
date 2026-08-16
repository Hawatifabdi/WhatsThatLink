package com.whatsThatLink.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecentScan::class], version = 1, exportSchema = false)
abstract class WhatsThatLinkDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        @Volatile
        private var INSTANCE: WhatsThatLinkDatabase? = null

        fun getDatabase(context: Context): WhatsThatLinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WhatsThatLinkDatabase::class.java,
                    "whatsthatlink_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
