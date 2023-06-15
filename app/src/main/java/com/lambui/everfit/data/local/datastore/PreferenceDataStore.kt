package com.lambui.everfit.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.squareup.moshi.Moshi
import com.lambui.everfit.data.local.datastore.PreferenceDataStoreDefault.PreferencesKeys.PREF_BASE_URL
import com.lambui.everfit.data.local.datastore.PreferenceDataStoreDefault.PreferencesKeys.PREF_HAS_LOGIN
import com.lambui.everfit.data.local.datastore.PreferenceDataStoreDefault.PreferencesKeys.PREF_TOKEN
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Lam Bui on 4/1/22.
 */

interface PreferenceDataStore {
    suspend fun token(token: String)
    val token: Flow<String>
    suspend fun setBaseUrl(url: String)
    val baseUrl: Flow<String>
    suspend fun isHasLogin(isHasLogin: Boolean)
    val isHasLogin: Flow<Boolean>
    suspend fun clearDataStore()
}

@Singleton
class PreferenceDataStoreDefault @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val moshi: Moshi
) : PreferenceDataStore {

    object PreferencesKeys {
        val PREF_TOKEN = stringPreferencesKey("pref_token")
        val PREF_HAS_LOGIN = booleanPreferencesKey("pref_has_login")
        val PREF_BASE_URL = stringPreferencesKey("pref_base_url")
    }

    override suspend fun token(token: String) {
        dataStore.edit { it[PREF_TOKEN] = token }
    }


    override val token: Flow<String> = dataStore.data.map { it[PREF_TOKEN] ?: "" }
    override suspend fun setBaseUrl(url: String) {
        dataStore.edit { it[PREF_BASE_URL] = url }
    }

    override val baseUrl: Flow<String> = dataStore.data.map { it[PREF_BASE_URL] ?: "" }

    override suspend fun isHasLogin(isHasLogin: Boolean) {
        dataStore.edit { it[PREF_HAS_LOGIN] = isHasLogin }
    }

    override val isHasLogin: Flow<Boolean> = dataStore.data.map { it[PREF_HAS_LOGIN] ?: false }

    override suspend fun clearDataStore() {
        dataStore.edit { it.clear() }
        isHasLogin(false)
    }
}
