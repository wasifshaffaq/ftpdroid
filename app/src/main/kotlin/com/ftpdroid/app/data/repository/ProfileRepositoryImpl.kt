package com.ftpdroid.app.data.repository

import com.ftpdroid.app.data.local.db.dao.ProfileDao
import com.ftpdroid.app.data.local.db.entity.ProfileEntity
import com.ftpdroid.app.data.local.prefs.SecureStorage
import com.ftpdroid.app.domain.model.ClientProfile
import com.ftpdroid.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val secureStorage: SecureStorage
) : ProfileRepository {

    override fun getAllProfiles(): Flow<List<ClientProfile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { entity ->
                val profile = entity.toDomain()
                // Load password from secure storage
                secureStorage.getPassword(entity.id)?.let { password ->
                    profile.copy(password = password)
                } ?: profile
            }
        }
    }

    override suspend fun getProfileById(id: Long): ClientProfile? {
        return profileDao.getProfileById(id)?.let { entity ->
            val profile = entity.toDomain()
            secureStorage.getPassword(entity.id)?.let { password ->
                profile.copy(password = password)
            } ?: profile
        }
    }

    override suspend fun getProfileByName(name: String): ClientProfile? {
        return profileDao.getProfileByName(name)?.let { entity ->
            val profile = entity.toDomain()
            secureStorage.getPassword(entity.id)?.let { password ->
                profile.copy(password = password)
            } ?: profile
        }
    }

    override suspend fun insertProfile(profile: ClientProfile): Long {
        val entity = ProfileEntity.fromDomain(profile)
        val id = profileDao.insertProfile(entity)
        // Save password securely
        if (profile.password.isNotEmpty()) {
            secureStorage.savePassword(id, profile.password)
        }
        // Save private key path if present
        profile.privateKeyPath?.let { path ->
            secureStorage.savePrivateKeyPath(id, path)
        }
        return id
    }

    override suspend fun updateProfile(profile: ClientProfile) {
        val entity = ProfileEntity.fromDomain(profile)
        profileDao.updateProfile(entity)
        // Update password in secure storage
        if (profile.password.isNotEmpty()) {
            secureStorage.savePassword(profile.id, profile.password)
        }
        // Update private key path if present
        profile.privateKeyPath?.let { path ->
            secureStorage.savePrivateKeyPath(profile.id, path)
        }
    }

    override suspend fun deleteProfile(id: Long) {
        profileDao.deleteProfileById(id)
        secureStorage.deletePassword(id)
        secureStorage.deletePrivateKeyPath(id)
    }

    override suspend fun getProfileCount(): Int {
        return profileDao.getProfileCount()
    }
}