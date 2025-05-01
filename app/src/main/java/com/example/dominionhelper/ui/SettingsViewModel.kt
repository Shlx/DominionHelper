package com.example.dominionhelper.ui

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingItem {
    data class SwitchSetting(
        val key: String,
        val title: String,
        val isChecked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingItem()

    data class TextSetting(
        val key: String,
        val title: String,
        val text: String,
        val onTextChange: (String) -> Unit
    ) : SettingItem()
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    /*private val _settings = MutableStateFlow<List<SettingItem>>(emptyList())
    val settings: StateFlow<List<SettingItem>> = _settings.asStateFlow()*/

    data class SettingsUiState(
        val settings: List<SettingItem> = emptyList()
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSettings().collect { updatedSettings ->
                _uiState.update {
                    it.copy(settings = updatedSettings)
                }
            }
        }
    }

    private fun getSettings(): Flow<List<SettingItem>> {
        return combine(getDarkMode(), getUserName()) { isDarkMode, userName ->
            listOf(
                SettingItem.SwitchSetting(
                    key = "isDarkMode",
                    title = "Dark Mode",
                    isChecked = isDarkMode,
                    onCheckedChange = { newIsDarkMode -> setDarkMode(newIsDarkMode) }
                ),
                SettingItem.TextSetting(
                    key = "userName",
                    title = "User Name",
                    text = userName,
                    onTextChange = { newUserName -> setUserName(newUserName) }
                )
            )
        }
    }

    fun setDarkMode(isDarkMode: Boolean) {
        Log.i("SettingsViewModel", "setDarkMode called $isDarkMode")
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[SettingsKeys.IS_DARK_MODE] = isDarkMode
                Log.i("SettingsViewModel", "dataStore.edit DarkMode $isDarkMode")
            }
        }
    }

    fun setUserName(userName: String) {
        Log.i("SettingsViewModel", "setUserName called")
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[SettingsKeys.USER_NAME] = userName
            }
        }
    }

    private fun getDarkMode(): Flow<Boolean> {
        return context.dataStore.data.map { settings ->
            settings[SettingsKeys.IS_DARK_MODE] == true // Default value if not set
        }
    }

    private fun getUserName(): Flow<String> {
        return context.dataStore.data.map { settings ->
            settings[SettingsKeys.USER_NAME] ?: "" // Default value if not set
        }
    }
}

object SettingsKeys {
    val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    val USER_NAME = stringPreferencesKey("user_name")
}