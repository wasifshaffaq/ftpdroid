package com.ftpdroid.app.di

import android.content.Context
import androidx.room.Room
import com.ftpdroid.app.data.local.db.AppDatabase
import com.ftpdroid.app.data.local.db.dao.ConnectionLogDao
import com.ftpdroid.app.data.local.db.dao.ProfileDao
import com.ftpdroid.app.data.local.db.dao.ServerUserDao
import com.ftpdroid.app.data.local.db.dao.TransferDao
import com.ftpdroid.app.data.network.ftp.FtpClientManager
import com.ftpdroid.app.data.network.ftp.FtpServerManager
import com.ftpdroid.app.data.network.ftp.SftpClientManager
import com.ftpdroid.app.data.repository.TransferRepositoryImpl
import com.ftpdroid.app.domain.repository.TransferRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ftpdroid_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: AppDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideTransferDao(database: AppDatabase): TransferDao {
        return database.transferDao()
    }

    @Provides
    @Singleton
    fun provideServerUserDao(database: AppDatabase): ServerUserDao {
        return database.serverUserDao()
    }

    @Provides
    @Singleton
    fun provideConnectionLogDao(database: AppDatabase): ConnectionLogDao {
        return database.connectionLogDao()
    }

    @Provides
    @Singleton
    fun provideFtpClientManager(): FtpClientManager {
        return FtpClientManager()
    }

    @Provides
    @Singleton
    fun provideSftpClientManager(): SftpClientManager {
        return SftpClientManager()
    }
}