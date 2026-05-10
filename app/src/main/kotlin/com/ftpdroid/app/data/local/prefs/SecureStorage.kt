package com.ftpdroid.app.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun savePassword(profileId: Long, password: String) {
        encryptedPrefs.edit().putString("$KEY_PREFIX$profileId", password).apply()
    }

    fun getPassword(profileId: Long): String? {
        return encryptedPrefs.getString("$KEY_PREFIX$profileId", null)
    }

    fun deletePassword(profileId: Long) {
        encryptedPrefs.edit().remove("$KEY_PREFIX$profileId").apply()
    }

    fun savePrivateKeyPath(profileId: Long, path: String) {
        encryptedPrefs.edit().putString("$KEY_PRIVATE_KEY_PREFIX$profileId", path).apply()
    }

    fun getPrivateKeyPath(profileId: Long): String? {
        return encryptedPrefs.getString("$KEY_PRIVATE_KEY_PREFIX$profileId", null)
    }

    fun deletePrivateKeyPath(profileId: Long) {
        encryptedPrefs.edit().remove("$KEY_PRIVATE_KEY_PREFIX$profileId").apply()
    }

    fun saveKeystorePassword(password: String) {
        encryptedPrefs.edit().putString(KEY_KEYSTORE_PASSWORD, password).apply()
    }

    fun getKeystorePassword(): String? {
        return encryptedPrefs.getString(KEY_KEYSTORE_PASSWORD, null)
    }

    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password) == hash
    }

    companion object {
        private const val PREFS_NAME = "ftpdroid_secure_prefs"
        private const val KEY_PREFIX = "profile_password_"
        private const val KEY_PRIVATE_KEY_PREFIX = "private_key_"
        private const val KEY_KEYSTORE_PASSWORD = "keystore_password"
    }
}