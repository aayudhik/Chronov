package com.example.domain.privacy

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import android.util.Base64

class PrivacyManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "privacy_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _isPinEnabled = MutableStateFlow(sharedPreferences.contains("pin_hash"))
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()
    
    private val _isBiometricEnabled = MutableStateFlow(sharedPreferences.getBoolean("biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isBackupEncrypted = MutableStateFlow(sharedPreferences.getBoolean("backup_encrypted", false))
    val isBackupEncrypted: StateFlow<Boolean> = _isBackupEncrypted.asStateFlow()

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        sharedPreferences.edit().putString("pin_hash", hash).apply()
        _isPinEnabled.value = true
    }
    
    fun removePin() {
        sharedPreferences.edit().remove("pin_hash").apply()
        _isPinEnabled.value = false
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = sharedPreferences.getString("pin_hash", null) ?: return false
        return hashPin(pin) == storedHash
    }

    fun setBiometricEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun setBackupEncrypted(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("backup_encrypted", enabled).apply()
        _isBackupEncrypted.value = enabled
    }

    fun getDatabasePassphrase(): CharArray {
        var passphraseStr = sharedPreferences.getString("db_passphrase", null)
        if (passphraseStr == null) {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            passphraseStr = Base64.encodeToString(bytes, Base64.NO_WRAP)
            sharedPreferences.edit().putString("db_passphrase", passphraseStr).apply()
        }
        return passphraseStr.toCharArray()
    }

    private fun hashPin(pin: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
