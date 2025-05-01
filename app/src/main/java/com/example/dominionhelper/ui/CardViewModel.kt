package com.example.dominionhelper.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dominionhelper.Kingdom
import com.example.dominionhelper.KingdomGenerator
import com.example.dominionhelper.model.Card
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.model.Expansion
import com.example.dominionhelper.data.ExpansionDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao,
    private val kingdomGenerator: KingdomGenerator
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

    private val _kingdom = MutableStateFlow<Kingdom>(Kingdom())
    val kingdom: StateFlow<Kingdom> = _kingdom.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    // Search related
    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.EXPANSION)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    // Player count
    private val _playerCount = MutableStateFlow<Int>(2)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

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
        _expansionCards.value = emptyList()
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

    // TODO: Error when < 10 cards / < 1 expansions are owned
    fun getRandomKingdom() {
        viewModelScope.launch {
            _kingdom.value = kingdomGenerator.generateKingdom()
            updatePlayerCount(_kingdom.value, 2)
            clearSelectedExpansion() // Clear this AFTER the kingdom is generated
            clearSelectedCard()

            if (searchActive.value) {
                toggleSearch()
                changeSearchText("")
            }

            _cardsToShow.value = true
        }
    }

    fun clearAllCards() {
        _expansionCards.value = emptyList()
        _kingdom.value = Kingdom()
        _cardsToShow.value = false
        Log.d("CardViewModel", "Cleared all cards")
    }

    private fun sortCards(cards: List<Card>): List<Card> {
        if (cards.isEmpty()) return cards

        val sortedCards = when (_sortType.value) {
            SortType.EXPANSION -> cards.sortedBy { it.sets.first() }
            SortType.ALPHABETICAL -> cards.sortedBy { it.name }
            SortType.COST -> cards.sortedBy { it.cost }
        }
        Log.d("CardViewModel", "Sorted ${sortedCards.size} cards by ${_sortType.value}")
        return sortedCards
    }

    private fun sortCards(cards: LinkedHashMap<Card, Int>): LinkedHashMap<Card, Int> {
        if (cards.isEmpty()) return linkedMapOf()

        val sortedEntries = when (_sortType.value) {
            SortType.EXPANSION -> cards.entries.sortedBy { it.key.sets.first() }
            SortType.ALPHABETICAL -> cards.entries.sortedBy { it.key.name }
            SortType.COST -> cards.entries.sortedBy { it.key.cost }
        }

        val sortedCards = LinkedHashMap<Card, Int>()
        sortedEntries.forEach { sortedCards[it.key] = it.value }
        Log.d("CardViewModel", "Sorted ${sortedCards.size} cards by ${_sortType.value}")
        return sortedCards
    }

    fun updateSortType(newSortType: SortType, kingdom: Kingdom) {
        _sortType.value = newSortType

        // TODO: Only sort what's appropriate

        // Sort expansion list
        _expansionCards.value = sortCards(_expansionCards.value)

        // Sort kingdom lists
        val sortedRandomCards = sortCards(kingdom.randomCards)
        val sortedBasicCards = sortCards(kingdom.basicCards)
        val sortedDependentCards = sortCards(kingdom.dependentCards)
        val sortedStartingCards = sortCards(kingdom.startingCards)

        viewModelScope.launch {

            // TODO: Figure this out
            // Why the hell is this necessary huh
            _kingdom.value.randomCards[cardDao.getCardByName("Copper")] = 3
            _kingdom.value = kingdom.copy(
                randomCards = sortedRandomCards,
                dependentCards = sortedDependentCards,
                basicCards = sortedBasicCards,
                startingCards = sortedStartingCards
            )
        }

        Log.d("CardViewModel", "Updated sort type to ${_sortType.value}")
    }

    fun toggleSearch() {
        _searchActive.value = !_searchActive.value
        if (!_searchActive.value) {
            _cardsToShow.value = false
            clearSelectedExpansion()
            clearSearchText()
        }
        Log.d("CardViewModel", "Toggled search to ${_searchActive.value}")
    }

    fun clearSearchText() {
        _searchText.value = ""
        Log.d("CardViewModel", "Cleared search text")
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

    fun updatePlayerCount(kingdom: Kingdom, count: Int) {
        _playerCount.value = count
        val updatedRandomCards = getCardAmounts(kingdom.randomCards, count)
        val updatedDependentCards = getCardAmounts(kingdom.dependentCards, count)
        val updatedBasicCards = getCardAmounts(kingdom.basicCards, count)

        _kingdom.value = kingdom.copy(
            randomCards = updatedRandomCards,
            dependentCards = updatedDependentCards,
            basicCards = updatedBasicCards
        )
        Log.d("CardViewModel", "Selected player count $count")
    }

    fun getCardAmounts(cards: LinkedHashMap<Card, Int>, playerCount: Int): LinkedHashMap<Card, Int> {
        assert(playerCount in 2..4)

        val cardAmounts = linkedMapOf<Card, Int>()

        cards.forEach { card, amount ->
            val amount = when (card.name) {
                "Copper" -> when (playerCount) {
                    2 -> 46
                    3 -> 39
                    4 -> 32
                    else -> throw IllegalArgumentException("Invalid player count: $playerCount")
                }

                "Silver" -> 40
                "Gold" -> 30
                "Curse" -> when (playerCount) {
                    2 -> 10
                    3 -> 20
                    4 -> 30
                    else -> throw IllegalArgumentException("Invalid player count: $playerCount")
                }

                "Estate" -> if (playerCount == 2) 8 else 12
                "Duchy" -> if (playerCount == 2) 8 else 12
                "Province" -> if (playerCount == 2) 8 else 12
                else -> 1
            }
            cardAmounts[card] = amount
        }
        return cardAmounts
    }

}

enum class SortType {
    ALPHABETICAL,
    COST,
    EXPANSION
}