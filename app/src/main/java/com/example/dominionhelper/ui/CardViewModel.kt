package com.example.dominionhelper.ui

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dominionhelper.data.Card
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.Expansion
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
            Log.d("CardViewModel", "Updated isOwned for expansion ${expansion.name} to $newIsOwned")
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
            Log.d("CardViewModel", "Loaded ${_expansionCards.value.size} cards for expansion ${expansion.name}")
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
            _expansionCards.value = emptyList()
            _randomCards.value = sortCards(cardDao.getRandomCardsFromOwnedExpansions(10))
            _basicCards.value = sortCards(cardDao.getBasicCards())
            _dependentCards.value = sortCards(cardDao.getDependentCards())
            _cardsToShow.value = true
            Log.i("CardViewModel", "Generated ${_randomCards.value.size} random cards, ${_basicCards.value.size} basic cards, ${_dependentCards.value.size} dependent cards")
        }
    }

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
            Log.d("CardViewModel", "Searched for $newText, search results: ${_expansionCards.value.size}")
        }
    }

}

enum class SortType {
    ALPHABETICAL,
    COST,
    EXPANSION
}