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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardDao: CardDao
) : ViewModel() {

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    /*private val _cardsWithCategories = MutableStateFlow<List<CardWithCategories>>(emptyList())
    val cardsWithCategories: StateFlow<List<CardWithCategories>> = _cardsWithCategories.asStateFlow()*/

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    private val _searchActive = MutableStateFlow<Boolean>(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    private val _searchText = MutableStateFlow<String>("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _showRandomCards = MutableStateFlow<Boolean>(false)
    val showRandomCards: StateFlow<Boolean> = _showRandomCards.asStateFlow()

    fun loadCardsByExpansion(set: Set) {
        viewModelScope.launch {
            _cards.value = cardDao.getCardsByExpansion(set)
            Log.d("CardViewModel", "Loaded ${_cards.value.size} cards for expansion ${set.name}")
        }
    }

    fun loadAllCards() {
        viewModelScope.launch {
            _cards.value = cardDao.getAll()
        }
    }

    fun selectCard(card: Card) {
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
            Log.d("CardViewModel", "Search results: ${_cards.value.size}")
        }
    }

    fun setRandomCards(){
        viewModelScope.launch {
            _cards.value = cardDao.getRandomCardsFromOwnedExpansions(10)
            _showRandomCards.value = true
        }
    }

    fun clearRandomCards(){
        _showRandomCards.value = false
    }

    /*fun getAllCardsWithCategories(){
        viewModelScope.launch {
            _cardsWithCategories.value = cardDao.getAllCardsWithCategories()
        }
    }*/

}