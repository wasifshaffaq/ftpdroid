package com.ftpdroid.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ftpdroid.app.domain.model.ServerUser

@Entity(tableName = "server_users")
data class ServerUserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    val homeDirectory: String,
    val canRead: Boolean = true,
    val canWrite: Boolean = true,
    val canDelete: Boolean = false,
    val maxLoginFailures: Int = 3
) {
    fun toDomain(): ServerUser = ServerUser(
        id = id,
        username = username,
        password = password,
        homeDirectory = homeDirectory,
        canRead = canRead,
        canWrite = canWrite,
        canDelete = canDelete,
        maxLoginFailures = maxLoginFailures
    )

    companion object {
        fun fromDomain(user: ServerUser): ServerUserEntity = ServerUserEntity(
            id = user.id,
            username = user.username,
            password = user.password,
            homeDirectory = user.homeDirectory,
            canRead = user.canRead,
            canWrite = user.canWrite,
            canDelete = user.canDelete,
            maxLoginFailures = user.maxLoginFailures
        )
    }
}