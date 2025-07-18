package com.example.dominionhelper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.dominionhelper.ui.DarkAgesMode
import com.example.dominionhelper.ui.ProsperityMode
import com.example.dominionhelper.ui.RandomMode
import com.example.dominionhelper.ui.VetoMode
import com.example.dominionhelper.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.USER_PREFERENCES_NAME) // Renamed for clarity if you have another "settings"

object UserPreferencesKeys { // Renamed from SettingsKeys to avoid confusion with the ViewModel's SettingItem
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode_preference")

    val RANDOM_MODE = stringPreferencesKey("random_mode_preference")
    val RANDOM_EXPANSION_AMOUNT = intPreferencesKey("random_expansion_amount_preference")

    val VETO_MODE = stringPreferencesKey("veto_mode_preference")
    val NUMBER_OF_CARDS_TO_GENERATE = intPreferencesKey("number_of_cards_to_generate_preference")

    val LANDSCAPE_CATEGORIES = intPreferencesKey("landscape_categories_preference")
    val LANDSCAPE_DIFFERENT_CATEGORIES = booleanPreferencesKey("landscape_different_categories_preference")

    val DARK_AGES_STARTER_CARDS = stringPreferencesKey("dark_ages_starter_preference")
    val PROSPERITY_BASIC_CARDS = stringPreferencesKey("prosperity_basic_preference")
}

@Singleton
class UserPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.IS_DARK_MODE] ?: Constants.DEFAULT_IS_DARK_MODE
        }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.IS_DARK_MODE] = isDarkMode
        }
    }

    val randomMode: Flow<RandomMode> = context.dataStore.data
        .map { preferences ->
            // Read the string value, defaulting to the name of your default enum constant
            val modeName = preferences[UserPreferencesKeys.RANDOM_MODE] ?: Constants.DEFAULT_RANDOM_MODE.name
            try {
                RandomMode.valueOf(modeName) // Convert string back to enum
            } catch (e: IllegalArgumentException) {
                // Handle cases where the stored string might be invalid (e.g., if you renamed enum constants)
                Constants.DEFAULT_RANDOM_MODE // Fallback to default
            }
        }

    suspend fun setRandomMode(newMode: RandomMode) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.RANDOM_MODE] = newMode.name
        }
    }

    val randomExpansionAmount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.RANDOM_EXPANSION_AMOUNT] ?: Constants.DEFAULT_RANDOM_EXPANSION_AMOUNT
        }

    suspend fun setRandomExpansionAmount(amount: Int) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.RANDOM_EXPANSION_AMOUNT] = amount
        }
    }

    // Veto mode
    val vetoMode: Flow<VetoMode> = context.dataStore.data
        .map { preferences ->
            val modeName = preferences[UserPreferencesKeys.VETO_MODE] ?: Constants.DEFAULT_VETO_MODE.name
            try {
                VetoMode.valueOf(modeName)
            } catch (e: IllegalArgumentException) {
                Constants.DEFAULT_VETO_MODE
            }
        }

    suspend fun setVetoMode(newMode: VetoMode) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.VETO_MODE] = newMode.name
        }
    }

    // Number of cards to generate
    val numberOfCardsToGenerate: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.NUMBER_OF_CARDS_TO_GENERATE] ?: Constants.DEFAULT_NUMBER_OF_CARDS_TO_GENERATE
        }

    suspend fun setNumberOfCardsToGenerate(amount: Int) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.NUMBER_OF_CARDS_TO_GENERATE] = amount
        }
    }

    // Landscape categories
    val landscapeCategories: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.LANDSCAPE_CATEGORIES] ?: Constants.DEFAULT_LANDSCAPE_CATEGORIES
        }

    suspend fun setLandscapeCategories(amount: Int) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.LANDSCAPE_CATEGORIES] = amount
        }
    }

    // Landscape different categories
    val landscapeDifferentCategories: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.LANDSCAPE_DIFFERENT_CATEGORIES] ?: Constants.DEFAULT_LANDSCAPE_DIFFERENT_CATEGORIES
        }

    suspend fun setLandscapeDifferentCategories(isDifferent: Boolean) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.LANDSCAPE_DIFFERENT_CATEGORIES] = isDifferent
        }
    }

    // Dark Ages starter cards
    val darkAgesStarterCardsMode: Flow<DarkAgesMode> = context.dataStore.data
        .map { preferences ->
            val modeName = preferences[UserPreferencesKeys.DARK_AGES_STARTER_CARDS] ?: Constants.DEFAULT_DARK_AGES_STARTER_CARDS.name
            try {
                DarkAgesMode.valueOf(modeName)
            } catch (e: IllegalArgumentException) {
                Constants.DEFAULT_DARK_AGES_STARTER_CARDS
            }
        }

    suspend fun setDarkAgesStarterCardsMode(newMode: DarkAgesMode) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.DARK_AGES_STARTER_CARDS] = newMode.name
        }
    }

    // Prosperity starter cards
    val prosperityBasicCardsMode: Flow<ProsperityMode> = context.dataStore.data
        .map { preferences ->
            val modeName = preferences[UserPreferencesKeys.PROSPERITY_BASIC_CARDS] ?: Constants.DEFAULT_PROSPERITY_BASIC_CARDS.name
            try {
                ProsperityMode.valueOf(modeName)
            } catch (e: IllegalArgumentException) {
                Constants.DEFAULT_PROSPERITY_BASIC_CARDS
            }
        }

    suspend fun setProsperityBasicCardsMode(newMode: ProsperityMode) {
        context.dataStore.edit { settings ->
            settings[UserPreferencesKeys.PROSPERITY_BASIC_CARDS] = newMode.name
        }
    }
}