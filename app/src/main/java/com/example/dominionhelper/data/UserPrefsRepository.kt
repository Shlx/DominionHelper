package com.example.dominionhelper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Define DataStore instance at the top level, typically in a separate file or with the repository
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME) // Renamed for clarity if you have another "settings"

object UserPreferencesKeys { // Renamed from SettingsKeys to avoid confusion with the ViewModel's SettingItem
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode_preference")
    val RANDOM_EXPANSION_AMOUNT = intPreferencesKey("random_expansion_amount_preference")
}

const val USER_PREFERENCES_NAME = "settings_pref"
const val DEFAULT_RANDOM_EXPANSION_AMOUNT = 2
const val DEFAULT_IS_DARK_MODE = false

@Singleton
class UserPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.IS_DARK_MODE] ?: DEFAULT_IS_DARK_MODE
        }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.IS_DARK_MODE] = isDarkMode
        }
    }

    val randomExpansionAmount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.RANDOM_EXPANSION_AMOUNT] ?: DEFAULT_RANDOM_EXPANSION_AMOUNT
        }

    suspend fun setRandomExpansionAmount(amount: Int) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.RANDOM_EXPANSION_AMOUNT] = amount
        }
    }
}