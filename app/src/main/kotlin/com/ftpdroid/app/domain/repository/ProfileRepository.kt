package com.ftpdroid.app.domain.repository

import com.ftpdroid.app.domain.model.ClientProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getAllProfiles(): Flow<List<ClientProfile>>
    suspend fun getProfileById(id: Long): ClientProfile?
    suspend fun getProfileByName(name: String): ClientProfile?
    suspend fun insertProfile(profile: ClientProfile): Long
    suspend fun updateProfile(profile: ClientProfile)
    suspend fun deleteProfile(id: Long)
    suspend fun getProfileCount(): Int
}