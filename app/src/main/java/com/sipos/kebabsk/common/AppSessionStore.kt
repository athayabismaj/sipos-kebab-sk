package com.sipos.kebabsk.common

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface SessionStore {
    fun saveSession(session: AuthSession)
    fun loadSession(): AuthSession?
    fun clearSession()
}

object AppSessionStore : SessionStore {
    private const val PREF_NAME = "sipos_session"
    private const val KEY_ALIAS = "sipos_session_key"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val ENCRYPTED_PREFIX = "enc_"
    @Volatile
    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs != null) return
        synchronized(this) {
            if (prefs == null) {
                prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                migrateLegacyPlaintextSession()
            }
        }
    }

    override fun saveSession(session: AuthSession) {
        val current = loadSession()
        if (current == session) return

        val editor = prefs?.edit() ?: return
        AuthSessionPersistence.keys.forEach { key ->
            editor.remove(key).remove(encryptedKey(key))
        }
        AuthSessionPersistence.toValues(session).forEach { (key, value) ->
            editor.putEncryptedString(key, value)
        }
        editor.apply()
    }

    override fun loadSession(): AuthSession? {
        val values = AuthSessionPersistence.keys.associateWith(::getEncryptedString)
        return AuthSessionPersistence.fromValues(values)
    }

    override fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }

    private fun migrateLegacyPlaintextSession() {
        val store = prefs ?: return
        if (!store.contains(AuthSessionPersistence.TOKEN)) return
        val legacyValues = AuthSessionPersistence.keys.associateWith { key -> store.getString(key, null) }
        val legacy = AuthSessionPersistence.fromValues(legacyValues)
        if (legacy == null) {
            clearSession()
            return
        }
        saveSession(legacy)
    }

    private fun SharedPreferences.Editor.putEncryptedString(key: String, value: String?): SharedPreferences.Editor {
        if (value == null) {
            remove(encryptedKey(key))
            return this
        }

        return putString(encryptedKey(key), encrypt(value))
    }

    private fun getEncryptedString(key: String): String? {
        val encrypted = prefs?.getString(encryptedKey(key), null) ?: return null
        return runCatching { decrypt(encrypted) }
            .getOrElse {
                clearSession()
                null
            }
    }

    private fun encryptedKey(key: String): String = "$ENCRYPTED_PREFIX$key"

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val cipherText = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return "${cipher.iv.toBase64()}:${cipherText.toBase64()}"
    }

    private fun decrypt(value: String): String {
        val parts = value.split(':')
        require(parts.size == 2) { "Invalid encrypted session value" }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(cipherText), StandardCharsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
}
