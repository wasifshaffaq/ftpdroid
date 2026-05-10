package com.ftpdroid.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ftpdroid.app.domain.model.ClientProfile
import com.ftpdroid.app.domain.model.Protocol

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 21,
    val protocol: String = "FTP",
    val username: String,
    val password: String = "",
    val initialRemotePath: String = "/",
    val isAnonymous: Boolean = false,
    val privateKeyPath: String? = null,
    val passiveMode: Boolean = true,
    val characterEncoding: String = "UTF-8",
    val timeout: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ClientProfile = ClientProfile(
        id = id,
        name = name,
        host = host,
        port = port,
        protocol = Protocol.valueOf(protocol),
        username = username,
        password = password,
        initialRemotePath = initialRemotePath,
        isAnonymous = isAnonymous,
        privateKeyPath = privateKeyPath,
        passiveMode = passiveMode,
        characterEncoding = characterEncoding,
        timeout = timeout,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(profile: ClientProfile): ProfileEntity = ProfileEntity(
            id = profile.id,
            name = profile.name,
            host = profile.host,
            port = profile.port,
            protocol = profile.protocol.name,
            username = profile.username,
            password = profile.password,
            initialRemotePath = profile.initialRemotePath,
            isAnonymous = profile.isAnonymous,
            privateKeyPath = profile.privateKeyPath,
            passiveMode = profile.passiveMode,
            characterEncoding = profile.characterEncoding,
            timeout = profile.timeout,
            createdAt = profile.createdAt
        )
    }
}