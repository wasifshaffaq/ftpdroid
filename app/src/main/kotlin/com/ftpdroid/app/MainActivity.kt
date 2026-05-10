package com.ftpdroid.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.ftpdroid.app.ui.MainScreen
import com.ftpdroid.app.ui.theme.FtpDroidTheme
import com.ftpdroid.app.data.local.prefs.AppPreferences
import com.ftpdroid.app.data.local.prefs.AppTheme
import androidx.compose.foundation.isSystemInDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor
import javax.inject.Inject

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    private lateinit var executor: Executor
    private var biometricAuthenticating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        executor = ContextCompat.getMainExecutor(this)

        setContent {
            val appTheme by appPreferences.appThemeFlow.collectAsState(initial = AppTheme.SYSTEM)
            val darkTheme = when (appTheme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            FtpDroidTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                biometricAuthenticating = false
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                biometricAuthenticating = false
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    finish()
                }
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }

        val prompt = BiometricPrompt(this, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock FTPDroid")
            .setSubtitle("Verify your identity to access FTP credentials")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        prompt.authenticate(promptInfo)
    }

    override fun onResume() {
        super.onResume()
        checkBiometricLock()
    }

    private fun checkBiometricLock() {
        if (biometricAuthenticating) return

        val prefs = getSharedPreferences("security_prefs", MODE_PRIVATE)
        val lockEnabled = prefs.getBoolean("biometric_lock", false)

        if (lockEnabled && !biometricAuthenticating) {
            biometricAuthenticating = true
            showBiometricPrompt { }
        }
    }
}