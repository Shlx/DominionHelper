package com.example.dominionhelper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.dominionhelper.ui.RandomMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = USER_PREFERENCES_NAME) // Renamed for clarity if you have another "settings"

object UserPreferencesKeys { // Renamed from SettingsKeys to avoid confusion with the ViewModel's SettingItem
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode_preference")
    val RANDOM_EXPANSION_AMOUNT = intPreferencesKey("random_expansion_amount_preference")
    val RANDOM_MODE = stringPreferencesKey("random_mode_preference")
}

const val USER_PREFERENCES_NAME = "settings_pref"
const val DEFAULT_RANDOM_EXPANSION_AMOUNT = 2
const val DEFAULT_IS_DARK_MODE = false
val DEFAULT_RANDOM_MODE = RandomMode.FULL_RANDOM

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

    val randomMode: Flow<RandomMode> = context.dataStore.data
        .map { preferences ->
            // Read the string value, defaulting to the name of your default enum constant
            val modeName = preferences[UserPreferencesKeys.RANDOM_MODE] ?: DEFAULT_RANDOM_MODE.name
            try {
                RandomMode.valueOf(modeName) // Convert string back to enum
            } catch (e: IllegalArgumentException) {
                // Handle cases where the stored string might be invalid (e.g., if you renamed enum constants)
                DEFAULT_RANDOM_MODE // Fallback to default
            }
        }

    suspend fun setRandomMode(newMode: RandomMode) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.RANDOM_MODE] = newMode.name
        }
    }
}