package com.crazy_coder.everfit_wear.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.crazy_coder.everfit_wear.data.local.datastore.PreferenceDataStore
import com.crazy_coder.everfit_wear.data.local.datastore.PreferenceDataStoreDefault
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by Lam Bui on 4/6/23.
 */

@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {

    private const val DATA_STORE_FILE_NAME = "everfit_app_prefs.pb"

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { appContext.preferencesDataStoreFile(DATA_STORE_FILE_NAME) }
        )
    }

    @Singleton
    @Provides
    fun providePreferenceDataStore(
        dataStore: DataStore<Preferences>,
        moshi: Moshi
    ): PreferenceDataStore {
        return PreferenceDataStoreDefault(dataStore, moshi)
    }
}
