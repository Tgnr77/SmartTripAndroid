package com.smarttrip.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smarttrip_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    // In-memory cache initialised asynchronously to avoid blocking the main thread.
    // The OkHttp interceptor reads this field on the network thread.
    // awaitReady() must be called before the first authenticated API request.
    @Volatile
    var cachedToken: String? = null
        private set

    private val _initialized = CompletableDeferred<Unit>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cachedToken = context.dataStore.data.first()[TOKEN_KEY]
            } finally {
                _initialized.complete(Unit)
            }
        }
    }

    /** Suspends until the token has been loaded from DataStore. */
    suspend fun awaitReady() = _initialized.await()

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
        cachedToken = token
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
        cachedToken = null
    }
}
