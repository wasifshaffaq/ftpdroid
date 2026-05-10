package com.ftpdroid.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ftpdroid.app.data.local.db.entity.ServerUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerUserDao {
    @Query("SELECT * FROM server_users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<ServerUserEntity>>

    @Query("SELECT * FROM server_users WHERE id = :id")
    suspend fun getUserById(id: Long): ServerUserEntity?

    @Query("SELECT * FROM server_users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): ServerUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: ServerUserEntity): Long

    @Update
    suspend fun updateUser(user: ServerUserEntity)

    @Delete
    suspend fun deleteUser(user: ServerUserEntity)

    @Query("DELETE FROM server_users WHERE id = :id")
    suspend fun deleteUserById(id: Long)

    @Query("SELECT COUNT(*) FROM server_users")
    suspend fun getUserCount(): Int
}