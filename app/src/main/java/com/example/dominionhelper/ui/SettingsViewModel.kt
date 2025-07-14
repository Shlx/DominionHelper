package com.example.dominionhelper.ui

import com.example.dominionhelper.data.UserPrefsRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingItem {
    data class SwitchSetting(
        //val key: String,
        val title: String,
        val isChecked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingItem()

    data class TextSetting(
        val title: String,
        val text: String,
        val onTextChange: (String) -> Unit
    ) : SettingItem()

    data class NumberSetting(
        val title: String,
        val number: Int,
        val onNumberChange: (Int) -> Unit
    ) : SettingItem()

    data class ChoiceSetting<E : Enum<E>>(
        val title: String,
        val selectedOption: E,
        val allOptions: List<E>,
        val optionDisplayFormatter: (E) -> String = { it.name }, // Default display is enum constant name
        val onOptionSelected: (E) -> Unit
    ) : SettingItem()
}

enum class RandomMode(val displayName: String) {
    FULL_RANDOM("Full Random"),
    EVEN_AMOUNTS("Even Amounts"), // Example, adjust as needed
    X_OF_EACH_SET("X from Each Set")    // Example, adjust as needed
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository
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
        return combine(userPrefsRepository.isDarkMode,
            userPrefsRepository.randomExpansionAmount,
            userPrefsRepository.randomMode
        ) { isDarkMode, amount, newMode ->
            listOf(
                SettingItem.SwitchSetting(
                    title = "Dark Mode",
                    isChecked = isDarkMode,
                    onCheckedChange = { newIsDarkMode -> setDarkMode(newIsDarkMode) }
                ),
                SettingItem.NumberSetting(
                    title = "Number of random expansions selected",
                    number = amount,
                    onNumberChange = { newAmount -> setRandomExpansionAmount(newAmount) }
                ),
                SettingItem.ChoiceSetting(
                    title = "Random mode",
                    selectedOption = RandomMode.FULL_RANDOM,
                    allOptions = RandomMode.entries,
                    optionDisplayFormatter = { it.displayName },
                    onOptionSelected = { newMode -> setRandomMode(newMode) }
                )
            )
        }
    }

    fun setRandomMode(mode: RandomMode) {
        viewModelScope.launch {
            userPrefsRepository.setRandomMode(mode)
        }
    }

    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            userPrefsRepository.setDarkMode(isDarkMode)
        }
    }

    fun setRandomExpansionAmount(amount: Int) {
        viewModelScope.launch {
            userPrefsRepository.setRandomExpansionAmount(amount)
        }
    }
}
