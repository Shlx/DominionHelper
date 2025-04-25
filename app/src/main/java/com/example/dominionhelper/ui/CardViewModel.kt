package com.example.dominionhelper.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dominionhelper.data.Card
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.CardDao.Companion.BASIC_CARD_NAMES
import com.example.dominionhelper.data.Category
import com.example.dominionhelper.data.Expansion
import com.example.dominionhelper.data.ExpansionDao
import com.example.dominionhelper.data.Set
import com.example.dominionhelper.data.Type
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao
) : ViewModel() {

    // Expansion variables
    private val _expansions = MutableStateFlow<List<Expansion>>(emptyList())
    val expansions: StateFlow<List<Expansion>> = _expansions.asStateFlow()

    private val _selectedExpansion = MutableStateFlow<Expansion?>(null)
    val selectedExpansion: StateFlow<Expansion?> = _selectedExpansion.asStateFlow()

    // Card variables
    private val _cardsToShow = MutableStateFlow(false)
    val cardsToShow: StateFlow<Boolean> = _cardsToShow.asStateFlow()

    private val _expansionCards = MutableStateFlow<List<Card>>(emptyList())
    val expansionCards: StateFlow<List<Card>> = _expansionCards.asStateFlow()

    private val _randomCards = MutableStateFlow<List<Card>>(emptyList())
    val randomCards: StateFlow<List<Card>> = _randomCards.asStateFlow()

    private val _dependentCards = MutableStateFlow<List<Card>>(emptyList())
    val dependentCards: StateFlow<List<Card>> = _dependentCards.asStateFlow()

    private val _basicCards = MutableStateFlow<List<Card>>(emptyList())
    val basicCards: StateFlow<List<Card>> = _basicCards.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    // Search related
    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.EXPANSION)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    init {
        viewModelScope.launch {
            _expansions.value = expansionDao.getAll()
            Log.d("CardViewModel", "Showing all ${_expansions.value.size} expansions")
        }
    }

    // Expansion functions
    fun selectExpansion(expansion: Expansion) {
        _selectedExpansion.value = expansion
        Log.d("CardViewModel", "Selected ${expansion.name}")
    }

    fun clearSelectedExpansion() {
        _selectedExpansion.value = null
        Log.d("CardViewModel", "Cleared selected expansion")
    }


    fun updateIsOwned(expansion: Expansion, newIsOwned: Boolean) {
        viewModelScope.launch {
            expansionDao.updateIsOwned(expansion.id, newIsOwned)

            val currentExpansions = _expansions.value.toMutableList()
            val index = currentExpansions.indexOfFirst { it.id == expansion.id }

            if (index != -1) {
                val updatedExpansion = expansion.copy(isOwned = newIsOwned)
                currentExpansions[index] = updatedExpansion
                _expansions.value = currentExpansions
            }
            Log.i("CardViewModel", "Updated isOwned for expansion ${expansion.name} to $newIsOwned")
        }
    }

    // Card functions
    /*fun loadAllCards() {
        Log.d("CardViewModel", "Loading all cards")
        viewModelScope.launch {
            _cards.value = cardDao.getAll()
            sortCards()
            Log.d("CardViewModel", "Loaded all ${_cards.value.size} cards")
        }
    }*/

    fun loadCardsByExpansion(expansion: Expansion) {
        viewModelScope.launch {
            _expansionCards.value = sortCards(cardDao.getCardsByExpansion(expansion.set))
            _cardsToShow.value = true
            Log.d(
                "CardViewModel",
                "Loaded ${_expansionCards.value.size} cards for expansion ${expansion.name}"
            )
        }
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
        Log.d("CardViewModel", "Selected card ${card.name}")
    }

    fun clearSelectedCard() {
        _selectedCard.value = null
        Log.d("CardViewModel", "Cleared selected card")
    }

    // TODO: Error when < 10 cards are owned
    fun getRandomCards() {
        viewModelScope.launch {
            //clearAllCards()??
            _expansionCards.value = emptyList()
            _randomCards.value = sortCards(cardDao.getRandomCardsFromOwnedExpansions(10))
            _basicCards.value = sortCards(cardDao.getCardsByNameList(BASIC_CARD_NAMES))

            val dependentCardsToLoad = getDependentCards(_randomCards.value)
            _dependentCards.value = sortCards(cardDao.getCardsByNameList(dependentCardsToLoad))

            _cardsToShow.value = true
            Log.i(
                "CardViewModel",
                "Generated ${_randomCards.value.size} random cards, ${_basicCards.value.size} basic cards, ${_dependentCards.value.size} dependent cards"
            )
        }
    }

    private fun getDependentCards(cards: List<Card>): List<String> {

        val dependencyRules = listOf(

            // If there is a Curser present, add Curse card
            DependencyRule(
                condition = { it.categories.contains(Category.CURSER) },
                dependentCardNames = listOf("Curse")
            ),

            // If there is an Alchemy card present, add Potion
            DependencyRule(
                condition = { it.set == Set.ALCHEMY },
                dependentCardNames = listOf("Potion")
            ),

            // If there is a Fate card present, add all Boons
            DependencyRule(
                condition = { it.types.contains(Type.FATE) },
                dependentCardNames = listOf("All 12 Boons", "Will-o'-Wisp")
            ),

            // If there is a Fate card present, add all Hexes and corresponding States
            DependencyRule(
                condition = { it.types.contains(Type.DOOM) },
                dependentCardNames = listOf("All 12 Hexes", "Curse", "Deluded", "Envious", "Miserable", "Twice Miserable")
            ),

            // If there is Border Guard present, add Lantern and Horn
            DependencyRule(
                condition = { it.name == "Border Guard" },
                dependentCardNames = listOf("Lantern", "Horn")
            ),
            // If there is Flag Bearer present, add Flag
            DependencyRule(
                condition = { it.name == "Flag Bearer" },
                dependentCardNames = listOf("Flag")
            ),

            // If there is Swashbuckler present, add Treasure Chest
            DependencyRule(
                condition = { it.name == "Swashbuckler" },
                dependentCardNames = listOf("Treasure Chest")
            ),

            // If there is Treasurer present, add Key
            DependencyRule(
                condition = { it.name == "Treasurer" },
                dependentCardNames = listOf("Key")
            ),

            // Watch out here that there is truly only ONE card added
            // This might not even make sense since people will probably prefer to pull a physical card

            // If there is a Liaison card present, add an Ally card
            DependencyRule(
                condition = { it.types.contains(Type.LIAISON) },
                dependentCardNames = listOf("Key") // TODO: Add ONE random Ally card
            ),

            // If there is an Omen card present, add a Prophecy card
            DependencyRule(
                condition = { it.types.contains(Type.OMEN) },
                dependentCardNames = listOf("Key") // TODO: Add ONE random Prophecy card
            ),

            // It would be better to add these to the database entries directly

            // If there is Fool present, add Lost in the Woods and Lucky Coin
            DependencyRule(
                condition = { it.name == "Fool" },
                dependentCardNames = listOf("Lost in the Woods", "Lucky Coin")
            ),

            // If there is Cemetery present, add Haunted Mirror
            DependencyRule(
                condition = { it.name == "Cemetery" },
                dependentCardNames = listOf("Haunted Mirror")
            ),

            // If there is Secret Cave present, add Magic Lamp
            DependencyRule(
                condition = { it.name == "Secret Cave" },
                dependentCardNames = listOf("Magic Lamp")
            ),

            // If there is Pixie present, add Goat
            DependencyRule(
                condition = { it.name == "Pixie" },
                dependentCardNames = listOf("Goat")
            ),

            // If there is Shepherd present, add Pasture
            DependencyRule(
                condition = { it.name == "Shepherd" },
                dependentCardNames = listOf("Pasture")
            ),

            // If there is Tracker present, add Pouch
            DependencyRule(
                condition = { it.name == "Tracker" },
                dependentCardNames = listOf("Pouch")
            ),

            // If there is Pooka present, add Cursed Gold
            DependencyRule(
                condition = { it.name == "Pooka" },
                dependentCardNames = listOf("Cursed Gold")
            ),

            // If there is Necromancer present, add zombies
            DependencyRule(
                condition = { it.name == "Necromancer" },
                dependentCardNames = listOf("Zombie Apprentice", "Zombie Mason", "Zombie Spy")
            ),

            // If there is Vampire present, add Bat
            DependencyRule(
                condition = { it.name == "Vampire" },
                dependentCardNames = listOf("Bat")
            ),

            // Count Prosperity cards and add Platinum and Colony
            DependencyRuleCount(
                condition = { it.set == Set.PROSPERITY || it.set == Set.PROSPERITY_1E || it.set == Set.PROSPERITY_2E },
                dependentCardNames = listOf("Platinum", "Colony"),
                minCount = 1
            )
        )

        val dependentCardNames = mutableListOf<String>()
        dependencyRules.forEach { rule ->

            var count = 0
            cards.forEach { card ->

                when (rule) {
                    is DependencyRuleCount -> {
                        if (rule.condition(card)) {
                            count++
                        }
                    }

                    is DependencyRule -> {
                        if (rule.condition(card)) {
                            dependentCardNames += rule.dependentCardNames
                        }
                    }
                }
            }

            if (rule is DependencyRuleCount && count >= rule.minCount) {
                dependentCardNames += rule.dependentCardNames
            }
        }

        return dependentCardNames.toList()
    }

    // Data class to represent a dependency rule
    data class DependencyRule(
        val condition: (Card) -> Boolean,
        val dependentCardNames: List<String>
    )

    // Data class to represent a dependency rule
    data class DependencyRuleCount(
        val condition: (Card) -> Boolean,
        val minCount: Int = 1,
        val dependentCardNames: List<String>
    )

    fun clearAllCards() {
        _expansionCards.value = emptyList()
        _randomCards.value = emptyList()
        _basicCards.value = emptyList()
        _dependentCards.value = emptyList()
        _cardsToShow.value = false
        Log.d("CardViewModel", "Cleared all cards")
    }

    private fun sortCards(cards: List<Card>): List<Card> {
        if (cards.isEmpty()) return cards

        val sortedCards = when (_sortType.value) {
            SortType.EXPANSION -> cards.sortedBy { it.set }
            SortType.ALPHABETICAL -> cards.sortedBy { it.name }
            SortType.COST -> cards.sortedBy { it.cost }
        }
        Log.d("CardViewModel", "Sorted ${sortedCards.size} cards by ${_sortType.value}")
        return sortedCards
    }

    fun updateSortType(newSortType: SortType) {
        _sortType.value = newSortType
        _expansionCards.value = sortCards(_expansionCards.value)
        _randomCards.value = sortCards(_randomCards.value)
        _basicCards.value = sortCards(_basicCards.value)
        _dependentCards.value = sortCards(_dependentCards.value)
        Log.d("CardViewModel", "Updated sort type to ${_sortType.value}")
    }

    fun toggleSearch() {
        _searchActive.value = !_searchActive.value
        Log.d("CardViewModel", "Toggled search to ${_searchActive.value}")
    }

    fun changeSearchText(newText: String) {
        _searchText.value = newText
        Log.d("CardViewModel", "Updated search text to $newText")
    }

    fun searchCards(newText: String) {
        viewModelScope.launch {
            // TODO: _expansionCards is also responsible for search results, which is weird
            _expansionCards.value = sortCards(cardDao.getFilteredCards("%$newText%"))
            _cardsToShow.value = true
            Log.d(
                "CardViewModel",
                "Searched for $newText, search results: ${_expansionCards.value.size}"
            )
        }
    }

}

enum class SortType {
    ALPHABETICAL,
    COST,
    EXPANSION
}