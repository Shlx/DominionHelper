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
    ) : SettingItem() {
        override fun toString(): String {
            return "SwitchSetting(title='$title', isChecked=$isChecked)"
        }
    }

    data class TextSetting(
        val title: String,
        val text: String,
        val onTextChange: (String) -> Unit
    ) : SettingItem() {
        override fun toString(): String {
            return "TextSetting(title='$title', text=$text)"
        }
    }

    data class NumberSetting(
        val title: String,
        val number: Int,
        val onNumberChange: (Int) -> Unit
    ) : SettingItem() {
        override fun toString(): String {
            return "NumberSetting(title='$title', number=$number)"
        }
    }

    data class ChoiceSetting<E : Enum<E>>(
        val title: String,
        val selectedOption: E,
        val allOptions: List<E>,
        val optionDisplayFormatter: (E) -> String = { it.name }, // Default display is enum constant name
        val onOptionSelected: (E) -> Unit
    ) : SettingItem() {
        override fun toString(): String {
            return "ChoiceSetting(title='$title', selectedOption=$selectedOption)"
        }
    }
}

enum class RandomMode(val displayName: String) {
    FULL_RANDOM("Full Random"),
    EVEN_AMOUNTS("Even Amounts"),

    // TODO does this make sense?
    //X_OF_EACH_SET("X from Each Set")
}

enum class VetoMode(val displayName: String) {
    REROLL_SAME("Reroll from the same expansion"),
    // TODO: This rerolls from any OWNED expansion, but we need to reroll from any SELECTED expansion probably
    REROLL_ANY("Reroll from any owned expansion"),
    NO_REROLL("Don't reroll")
}

enum class DarkAgesMode(val displayName: String) {
    TEN_PERCENT_PER_CARD("10% per card"),
    IF_PRESENT("Always when present"),
    NEVER("Never")
}

enum class ProsperityMode(val displayName: String) {
    TEN_PERCENT_PER_CARD("10% per card"),
    IF_PRESENT("Always when present"),
    NEVER("Never")
    // ALWAYS_IF_PROSPERITY_OWNED ??
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

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
        return combine(
            userPrefsRepository.isDarkMode,
            userPrefsRepository.randomMode,
            userPrefsRepository.randomExpansionAmount,
            userPrefsRepository.vetoMode,
            userPrefsRepository.numberOfCardsToGenerate,
            userPrefsRepository.landscapeCategories,
            userPrefsRepository.landscapeDifferentCategories,
            userPrefsRepository.darkAgesStarterCardsMode,
            userPrefsRepository.prosperityBasicCardsMode
        ) { values ->
            val isDarkMode = values[0] as Boolean
            val currentRandomMode = values[1] as RandomMode
            val currentRandomExpAmount = values[2] as Int
            val currentVetoMode = values[3] as VetoMode
            val currentNumCardsToGen = values[4] as Int
            val currentLandscapeCategories = values[5] as Int
            val currentLandscapeDiffCat = values[6] as Boolean
            val currentDarkAgesMode = values[7] as DarkAgesMode
            val currentProsperityMode = values[8] as ProsperityMode

            listOfNotNull( // Use listOfNotNull if some settings might be conditionally absent
                SettingItem.SwitchSetting(
                    title = "Dark Mode",
                    isChecked = isDarkMode,
                    onCheckedChange = { setDarkMode(it) }
                ),
                SettingItem.ChoiceSetting(
                    title = "Random mode",
                    selectedOption = currentRandomMode,
                    allOptions = RandomMode.entries.toList(),
                    optionDisplayFormatter = { it.displayName },
                    onOptionSelected = { setRandomMode(it) }
                ),
                SettingItem.NumberSetting(
                    title = "Expansions for random cards",
                    number = currentRandomExpAmount,
                    onNumberChange = { setRandomExpansionAmount(it) }
                ),
                SettingItem.ChoiceSetting(
                    title = "Veto mode",
                    selectedOption = currentVetoMode,
                    allOptions = VetoMode.entries.toList(),
                    optionDisplayFormatter = { it.displayName },
                    onOptionSelected = { setVetoMode(it) }
                ),
                SettingItem.NumberSetting(
                    title = "Number of cards to generate",
                    number = currentNumCardsToGen,
                    onNumberChange = { setNumberOfCardsToGenerate(it) }
                ),
                SettingItem.NumberSetting(
                    title = "Landscape categories to include",
                    number = currentLandscapeCategories,
                    onNumberChange = { setLandscapeCategories(it) }
                ),
                SettingItem.SwitchSetting(
                    title = "Use different landscape categories",
                    isChecked = currentLandscapeDiffCat,
                    onCheckedChange = { setLandscapeDifferentCategories(it) }
                ),
                SettingItem.ChoiceSetting(
                    title = "Dark Ages starter cards",
                    selectedOption = currentDarkAgesMode,
                    allOptions = DarkAgesMode.entries.toList(),
                    optionDisplayFormatter = { it.displayName },
                    onOptionSelected = { setDarkAgesStarterCardsMode(it) }
                ),
                SettingItem.ChoiceSetting(
                    title = "Prosperity basic cards",
                    selectedOption = currentProsperityMode,
                    allOptions = ProsperityMode.entries.toList(),
                    optionDisplayFormatter = { it.displayName },
                    onOptionSelected = { setProsperityBasicCardsMode(it) }
                )
            )
        }
    }

    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            userPrefsRepository.setDarkMode(isDarkMode)
        }
    }

    fun setRandomMode(mode: RandomMode) {
        viewModelScope.launch {
            userPrefsRepository.setRandomMode(mode)
        }
    }

    fun setRandomExpansionAmount(amount: Int) {
        viewModelScope.launch {
            userPrefsRepository.setRandomExpansionAmount(amount)
        }
    }

    fun setVetoMode(mode: VetoMode) {
        viewModelScope.launch {
            userPrefsRepository.setVetoMode(mode)
        }
    }

    fun setNumberOfCardsToGenerate(amount: Int) {
        viewModelScope.launch {
            userPrefsRepository.setNumberOfCardsToGenerate(amount)
        }
    }

    fun setLandscapeCategories(amount: Int) {
        viewModelScope.launch {
            userPrefsRepository.setLandscapeCategories(amount)
        }
    }

    fun setLandscapeDifferentCategories(isDifferent: Boolean) {
        viewModelScope.launch {
            userPrefsRepository.setLandscapeDifferentCategories(isDifferent)
        }
    }

    fun setDarkAgesStarterCardsMode(mode: DarkAgesMode) {
        viewModelScope.launch {
            userPrefsRepository.setDarkAgesStarterCardsMode(mode)
        }
    }

    fun setProsperityBasicCardsMode(mode: ProsperityMode) {
        viewModelScope.launch {
            userPrefsRepository.setProsperityBasicCardsMode(mode)
        }
    }
}
