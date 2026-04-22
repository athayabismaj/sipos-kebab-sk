package com.sipos.kebabsk.common

import android.content.Context
import android.content.SharedPreferences
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession

object AppSessionStore {
    private const val PREF_NAME = "sipos_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"

    @Volatile
    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs != null) return
        synchronized(this) {
            if (prefs == null) {
                prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            }
        }
    }

    fun saveSession(session: AuthSession) {
        prefs?.edit()
            ?.putString(KEY_TOKEN, session.token)
            ?.putString(KEY_DISPLAY_NAME, session.displayName)
            ?.putString(KEY_USERNAME, session.username)
            ?.putString(KEY_EMAIL, session.email)
            ?.putString(KEY_ROLE, session.role)
            ?.apply()
    }

    fun loadSession(): AuthSession? {
        val token = prefs?.getString(KEY_TOKEN, null) ?: return null
        return AuthSession(
            token = token,
            displayName = prefs?.getString(KEY_DISPLAY_NAME, "") ?: "",
            username = prefs?.getString(KEY_USERNAME, "") ?: "",
            email = prefs?.getString(KEY_EMAIL, "") ?: "",
            role = prefs?.getString(KEY_ROLE, null)
        )
    }

    fun clearSession() {
        prefs?.edit()?.clear()?.apply()
    }
}

