package com.example.dominionhelper.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dominionhelper.data.Card
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.Set
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardDao: CardDao
) : ViewModel() {

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _cardsToShow = MutableStateFlow(false)
    val cardsToShow: StateFlow<Boolean> = _cardsToShow.asStateFlow()

    /*private val _cardsWithCategories = MutableStateFlow<List<CardWithCategories>>(emptyList())
    val cardsWithCategories: StateFlow<List<CardWithCategories>> = _cardsWithCategories.asStateFlow()*/

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    private val _searchActive = MutableStateFlow<Boolean>(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    private val _searchText = MutableStateFlow<String>("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _sortType = MutableStateFlow<SortType>(SortType.EXPANSION)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _showRandomCards = MutableStateFlow<Boolean>(false)
    val showRandomCards: StateFlow<Boolean> = _showRandomCards.asStateFlow()

    private val _randomCards = MutableStateFlow<List<Card>>(emptyList())
    val randomCards: StateFlow<List<Card>> = _randomCards.asStateFlow()

    private val _basicCards = MutableStateFlow<List<Card>>(emptyList())
    val basicCards: StateFlow<List<Card>> = _basicCards.asStateFlow()

    private val _dependentCards = MutableStateFlow<List<Card>>(emptyList())
    val dependentCards: StateFlow<List<Card>> = _dependentCards.asStateFlow()

    fun loadCardsByExpansion(set: Set) {
        _isLoading.value = true
        Log.d("CardViewModel", "Loading cards for expansion ${set.name}")
        viewModelScope.launch {

            cardDao.getCardsByExpansion(set).collectLatest { cards -> // Collect the Flow
                _cards.value = cards // Update with the list
                sortCards()
                Log.d("CardViewModel", "Loaded ${cards.size} cards for expansion ${set.name}")
                _isLoading.value = false
                _cardsToShow.value = true
            }

            /*_cards.value = cardDao.getCardsByExpansion(set)
            sortCards()
            Log.d("CardViewModel", "Loaded ${_cards.value.size} cards for expansion ${set.name}")*/
        }
    }

    fun cardsToShow(): Boolean {
        return _cards.value.isNotEmpty() || _randomCards.value.isNotEmpty()
    }

    /*fun loadAllCards() {
        Log.d("CardViewModel", "Loading all cards")
        viewModelScope.launch {
            _cards.value = cardDao.getAll()
            sortCards()
            Log.d("CardViewModel", "Loaded all ${_cards.value.size} cards")
        }
    }*/

    fun selectCard(card: Card) {
        Log.d("CardViewModel", "Selected card: ${card.name}")
        _selectedCard.value = card
    }

    fun clearSelectedCard() {
        _selectedCard.value = null
    }

    fun toggleSearch(){
        _searchActive.value = !_searchActive.value
    }

    fun changeSearchText(newText: String){
        _searchText.value = newText
    }

    fun searchCards(newText: String){
        viewModelScope.launch {
            _cards.value = cardDao.getFilteredCards("%$newText%")
            sortCards()
            Log.d("CardViewModel", "Search results: ${_cards.value.size}")
        }
    }

    fun setRandomCards(){
        Log.d("CardViewModel", "Setting random cards")
        viewModelScope.launch {
            _cards.value = emptyList()
            _randomCards.value = cardDao.getRandomCardsFromOwnedExpansions(10)
            _basicCards.value = cardDao.getBasicCards()
            _dependentCards.value = cardDao.getDependentCards()
            _showRandomCards.value = true
            _cardsToShow.value = true
            //sortCards() // Only sort random cards or sort each list separately
            Log.d("CardViewModel", "Random cards set")
        }
    }

    fun clearRandomCards(){
        _showRandomCards.value = false
        _randomCards.value = emptyList()
        _basicCards.value = emptyList()
        _dependentCards.value = emptyList()
        _cardsToShow.value = false
    }

    private fun sortCards() {
        val sortedCards = when (_sortType.value) {
            SortType.EXPANSION -> _cards.value.sortedBy { it.set } // No sorting, keep the current order
            SortType.ALPHABETICAL -> _cards.value.sortedBy { it.name }
            SortType.COST -> _cards.value.sortedBy { it.cost }
        }
        _cards.value = sortedCards

        val sortedRandomCards = when (_sortType.value) {
            SortType.EXPANSION -> _randomCards.value.sortedBy { it.set } // No sorting, keep the current order
            SortType.ALPHABETICAL -> _randomCards.value.sortedBy { it.name }
            SortType.COST -> _randomCards.value.sortedBy { it.cost }
        }
        _randomCards.value = sortedRandomCards

        val sortedDependentCards = when (_sortType.value) {
            SortType.EXPANSION -> _dependentCards.value.sortedBy { it.set } // No sorting, keep the current order
            SortType.ALPHABETICAL -> _dependentCards.value.sortedBy { it.name }
            SortType.COST -> _dependentCards.value.sortedBy { it.cost }
        }
        _dependentCards.value = sortedDependentCards

        val sortedBasicCards = when (_sortType.value) {
            SortType.EXPANSION -> _basicCards.value.sortedBy { it.set } // No sorting, keep the current order
            SortType.ALPHABETICAL -> _basicCards.value.sortedBy { it.name }
            SortType.COST -> _basicCards.value.sortedBy { it.cost }
        }
        _basicCards.value = sortedBasicCards
    }

    fun updateSortType(newSortType: SortType) {
        _sortType.value = newSortType
        sortCards()
    }

    fun clearCards() {
        _cards.value = emptyList()
        _cardsToShow.value = false
    }

    /*fun getAllCardsWithCategories(){
        viewModelScope.launch {
            _cardsWithCategories.value = cardDao.getAllCardsWithCategories()
        }
    }*/

}

enum class SortType {
    ALPHABETICAL,
    COST,
    EXPANSION
}