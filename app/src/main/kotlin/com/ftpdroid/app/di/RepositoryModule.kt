package com.ftpdroid.app.di

import com.ftpdroid.app.data.repository.ClientRepositoryImpl
import com.ftpdroid.app.data.repository.ProfileRepositoryImpl
import com.ftpdroid.app.data.repository.ServerRepositoryImpl
import com.ftpdroid.app.data.repository.TransferRepositoryImpl
import com.ftpdroid.app.domain.repository.ClientRepository
import com.ftpdroid.app.domain.repository.ProfileRepository
import com.ftpdroid.app.domain.repository.ServerRepository
import com.ftpdroid.app.domain.repository.TransferRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindServerRepository(
        serverRepositoryImpl: ServerRepositoryImpl
    ): ServerRepository

    @Binds
    @Singleton
    abstract fun bindClientRepository(
        clientRepositoryImpl: ClientRepositoryImpl
    ): ClientRepository

    @Binds
    @Singleton
    abstract fun bindTransferRepository(
        transferRepositoryImpl: TransferRepositoryImpl
    ): TransferRepository
}
