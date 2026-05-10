package com.ftpdroid.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ftpdroid.app.data.local.db.dao.ConnectionLogDao
import com.ftpdroid.app.data.local.db.dao.ProfileDao
import com.ftpdroid.app.data.local.db.dao.ServerUserDao
import com.ftpdroid.app.data.local.db.dao.TransferDao
import com.ftpdroid.app.data.local.db.entity.ConnectionLogEntity
import com.ftpdroid.app.data.local.db.entity.Converters
import com.ftpdroid.app.data.local.db.entity.ProfileEntity
import com.ftpdroid.app.data.local.db.entity.ServerUserEntity
import com.ftpdroid.app.data.local.db.entity.TransferEntity

@Database(
    entities = [
        ProfileEntity::class,
        TransferEntity::class,
        ServerUserEntity::class,
        ConnectionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun transferDao(): TransferDao
    abstract fun serverUserDao(): ServerUserDao
    abstract fun connectionLogDao(): ConnectionLogDao

    companion object {
        private const val DATABASE_NAME = "ftpdroid_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}