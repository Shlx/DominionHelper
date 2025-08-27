package com.marvinsuhr.dominionhelper.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marvinsuhr.dominionhelper.Kingdom
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.model.Expansion
import com.marvinsuhr.dominionhelper.data.ExpansionDao
import com.marvinsuhr.dominionhelper.data.UserPrefsRepository
import com.marvinsuhr.dominionhelper.model.ExpansionWithEditions
import com.marvinsuhr.dominionhelper.model.OwnedEdition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryUiState {
    SHOWING_EXPANSIONS,
    SHOWING_EXPANSION_CARDS,
    SHOWING_SEARCH_RESULTS,
    SHOWING_CARD_DETAIL
 }

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao
) : ViewModel() {

    // Variable for tracking the current state
    private val _libraryUiState = MutableStateFlow(LibraryUiState.SHOWING_EXPANSIONS)
    val libraryUiState: StateFlow<LibraryUiState> = _libraryUiState.asStateFlow()
    private var lastState: LibraryUiState = LibraryUiState.SHOWING_EXPANSIONS

    // Expansion variables
    private val _expansionsWithEditions = MutableStateFlow<List<ExpansionWithEditions>>(emptyList())
    val expansionsWithEditions: StateFlow<List<ExpansionWithEditions>> =
        _expansionsWithEditions.asStateFlow()

    private val _selectedExpansion = MutableStateFlow<ExpansionWithEditions?>(null)
    val selectedExpansion: StateFlow<ExpansionWithEditions?> = _selectedExpansion.asStateFlow()

    private val _selectedEdition = MutableStateFlow(OwnedEdition.NONE)
    val selectedEdition: StateFlow<OwnedEdition> = _selectedEdition.asStateFlow()

    // Card / Kingdom variables
    private val _cardsToShow = MutableStateFlow<List<Card>>(emptyList())
    val cardsToShow: StateFlow<List<Card>> = _cardsToShow.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    // Search related
    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.TYPE)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val topBarTitle: StateFlow<String> = combine(
        libraryUiState,
        selectedExpansion,
        selectedCard,
        cardsToShow
    ) { uiScreenState, selectedExpansion, selectedCard, cardsToShow ->
        when (uiScreenState) {
            LibraryUiState.SHOWING_EXPANSIONS -> "Dominion Helper"
            LibraryUiState.SHOWING_EXPANSION_CARDS -> {
                selectedExpansion?.let { expansion ->
                    "${expansion.name} ${getEnabledCardAmount(cardsToShow)}"
                } ?: "Cards"
            }
            LibraryUiState.SHOWING_SEARCH_RESULTS -> "Search Results" // I think this isn't shown
            LibraryUiState.SHOWING_CARD_DETAIL -> selectedCard?.name ?: "Details"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Dominion Helper"
    )

    init {
        loadExpansionsWithEditions()
    }

    fun handleBackNavigation() {
        when (_libraryUiState.value) {
            LibraryUiState.SHOWING_EXPANSIONS -> {
                Log.i("BackHandler", "Leave expansion list -> Exit app")
                // Exit app
            }

            LibraryUiState.SHOWING_EXPANSION_CARDS -> {
                Log.i("BackHandler", "Leave expansion list -> Return to expansion list")
                clearSelectedExpansion()
            }

            LibraryUiState.SHOWING_SEARCH_RESULTS -> {
                Log.i("BackHandler", "Deactivate search")
                toggleSearch() // -> Deactivate search?
                changeSearchText("")
                clearAllCards()
            }

            LibraryUiState.SHOWING_CARD_DETAIL -> {
                Log.i("BackHandler", "Deselect card -> Return to card list")
                clearSelectedCard()
            }
        }
    }

    // Load all expansions and their editions, grouped by name
    // TODO the expansion and edition entity is ass
    private fun loadExpansionsWithEditions() {
        viewModelScope.launch {
            expansionDao.getAll().collect { allExpansions ->

                val currentExpandedState =
                    _expansionsWithEditions.value.associate { it.name to it.isExpanded }

                // Make a map of name -> list of editions
                val expansionsGrouped = allExpansions.groupBy { it.name }
                val cornGuild = allExpansions.find { it.name == "Cornucopia & Guilds" }

                // Assemble ExpansionWithEditions object for each entry
                val expansionsWithEditions = expansionsGrouped
                    .filter { (expansionName, _) -> expansionName != "Cornucopia & Guilds" }
                    .map { (expansionName, editions) ->

                        val firstEdition = editions.find { it.edition == 1 }
                        val secondEdition =
                            if (firstEdition?.name == "Cornucopia" || firstEdition?.name == "Guilds") {
                                cornGuild
                            } else {
                                editions.find { it.edition == 2 }
                            }

                        // Choose the correct image
                        val image = when {
                            firstEdition != null -> firstEdition.imageName
                            secondEdition != null -> secondEdition.imageName // Only for Cornucopia + Guilds 2nd edition
                            else -> throw java.lang.IllegalArgumentException("No edition found.")
                        }

                        val shouldBeExpanded = currentExpandedState[expansionName] == true

                        ExpansionWithEditions(
                            name = expansionName,
                            firstEdition = firstEdition,
                            secondEdition = secondEdition,
                            image = image,
                            isExpanded = shouldBeExpanded
                        )
                    }

                _expansionsWithEditions.value = expansionsWithEditions
                Log.i(
                    "LibraryViewModel",
                    "Loaded expansions [${expansionsWithEditions.size}] with editions [${allExpansions.size}]"
                )
            }
        }
    }

    fun toggleExpansion(expansionToToggle: ExpansionWithEditions) {
        Log.i(
            "LibraryViewModel",
            "Toggling expansion ${expansionToToggle.name}: ${expansionToToggle.isExpanded}"
        )
        viewModelScope.launch {
            _expansionsWithEditions.value = _expansionsWithEditions.value.map { expansion ->
                if (expansion.name == expansionToToggle.name) {
                    // Create a new ExpansionWithEditions object with the toggled isExpanded flag
                    expansion.copy(isExpanded = !expansion.isExpanded)
                } else {
                    // Keep other expansions as they are
                    expansion
                }
            }
            Log.i(
                "LibraryViewModel",
                "Toggled expansion ${expansionToToggle.name}: ${expansionToToggle.isExpanded}"
            )
        }
    }

    // Update ownership of an expansion
    fun updateExpansionOwnership(expansion: Expansion, newIsOwned: Boolean) {
        viewModelScope.launch {
            // Update the database
            when (expansion.edition) {
                1 -> expansionDao.updateFirstEditionOwned(
                    expansion.name,
                    newIsOwned
                )

                2 -> expansionDao.updateSecondEditionOwned(
                    expansion.name,
                    newIsOwned
                )

                else -> throw java.lang.IllegalArgumentException("Invalid edition.")
            }

            // Update the object
            _expansionsWithEditions.value = _expansionsWithEditions.value.map {
                if (it.name == expansion.name) {
                    when (expansion.edition) {
                        1 -> it.copy(firstEdition = it.firstEdition?.copy(isOwned = newIsOwned))
                        2 -> it.copy(secondEdition = it.secondEdition?.copy(isOwned = newIsOwned))
                        else -> throw java.lang.IllegalArgumentException("Invalid edition.")
                    }
                } else {
                    it
                }
            }
            Log.i(
                "LibraryViewModel",
                "UpdateExpansionOwnership(): Updated isOwned for ${expansion.name}[${expansion.edition}] to $newIsOwned"
            )
        }
    }

    fun getOwnershipText(expansion: ExpansionWithEditions): String {

        val isFirstOwned = expansion.firstEdition?.isOwned == true
        val isSecondOwned = expansion.secondEdition?.isOwned == true

        return when {
            isFirstOwned && isSecondOwned -> "Both Editions Owned"
            isFirstOwned ->
                if (expansion.secondEdition == null) {
                    "Owned"
                } else {
                    "First Edition Owned"
                }

            isSecondOwned -> "Second Edition Owned"
            else -> "Unowned"
        }
    }

    /////////////////////////
    // Expansion functions //
    /////////////////////////

    fun selectExpansion(expansion: ExpansionWithEditions) {
        viewModelScope.launch {

            val ownedEditions = whichEditionIsOwned(expansion)
            val set = getCardsFromOwnedEditions(expansion, ownedEditions)
            _selectedExpansion.value = expansion
            _selectedEdition.value = ownedEditions
            _cardsToShow.value = sortCards(set.toList())

            Log.d(
                "LibraryViewModel",
                "Loaded ${_cardsToShow.value.size} cards for expansion ${expansion.name}"
            )

            _libraryUiState.value = LibraryUiState.SHOWING_EXPANSION_CARDS
            Log.d("LibraryViewModel", "Selected ${expansion.name}")
        }
    }

    private fun whichEditionIsOwned(expansion: ExpansionWithEditions): OwnedEdition {

        if (expansion.firstEdition?.isOwned == true) {
            if (expansion.secondEdition?.isOwned == true) {
                return OwnedEdition.BOTH
            }
            return OwnedEdition.FIRST
        } else if (expansion.secondEdition?.isOwned == true) {
            return OwnedEdition.SECOND
        } else {
            return OwnedEdition.NONE
        }
    }

    private suspend fun getCardsFromOwnedEditions(expansion: ExpansionWithEditions, ownedEdition: OwnedEdition): Set<Card> {

        val set = mutableSetOf<Card>()

        when (ownedEdition) {
            OwnedEdition.FIRST -> {
                set.addAll(cardDao.getCardsByExpansion(expansion.firstEdition!!.id))
            }

            OwnedEdition.SECOND ->  {
                set.addAll(cardDao.getCardsByExpansion(expansion.secondEdition!!.id))
            }

            else -> {
                if (expansion.firstEdition != null) {
                    set.addAll(cardDao.getCardsByExpansion(expansion.firstEdition!!.id))
                }
                if (expansion.secondEdition != null) {
                    set.addAll(cardDao.getCardsByExpansion(expansion.secondEdition.id))
                }
            }
        }

        return set
    }

    fun clearSelectedExpansion() {
        _selectedExpansion.value = null
        _cardsToShow.value = emptyList()
        _libraryUiState.value = LibraryUiState.SHOWING_EXPANSIONS
        Log.d("LibraryViewModel", "Cleared selected expansion")
    }

    // When edition selector in CardList is pressed
    fun selectEdition(expansion: ExpansionWithEditions, clickedEditionNumber: Int, currentOwnedEdition: OwnedEdition) {
        viewModelScope.launch {

            val newSelectedEdition: OwnedEdition

            if (clickedEditionNumber == 1) {
                newSelectedEdition = when (currentOwnedEdition) {
                    OwnedEdition.NONE -> OwnedEdition.FIRST
                    OwnedEdition.FIRST -> OwnedEdition.FIRST
                    OwnedEdition.SECOND -> OwnedEdition.BOTH
                    OwnedEdition.BOTH -> OwnedEdition.SECOND
                }

            } else {
                newSelectedEdition = when (currentOwnedEdition) {
                    OwnedEdition.NONE -> OwnedEdition.SECOND
                    OwnedEdition.FIRST -> OwnedEdition.BOTH
                    OwnedEdition.SECOND -> OwnedEdition.SECOND
                    OwnedEdition.BOTH -> OwnedEdition.FIRST
                }
            }

            val set = getCardsFromOwnedEditions(expansion, newSelectedEdition)
            _cardsToShow.value = sortCards(set.toList())
            _selectedEdition.value = newSelectedEdition
            Log.d("LibraryViewModel", "Selected edition $clickedEditionNumber for ${expansion.name} -> $currentOwnedEdition")
        }
    }

    fun selectEdition(expansion: Expansion) {
        viewModelScope.launch {
            _cardsToShow.value =
                sortCards(cardDao.getCardsByExpansion(expansion.id))
            // We need to set these, so we need ExpansionWithEditions here. But also an int for the clicked edition
            //_selectedExpansion.value = expansion
            //_selectedEdition.value = whichEditionIsOwned(expansion)
            //_uiScreenState.value = UiScreenState.SHOWING_EXPANSION_CARDS

            Log.d("LibraryViewModel", "Selected edition ${expansion.name}")
        }
    }

    fun expansionHasTwoEditions(expansion: ExpansionWithEditions): Boolean {
        return expansion.firstEdition != null && expansion.secondEdition != null
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
        if (libraryUiState.value != LibraryUiState.SHOWING_CARD_DETAIL) {
            lastState = libraryUiState.value // Saving whether we come from search results or expansion cards
        }
        _libraryUiState.value = LibraryUiState.SHOWING_CARD_DETAIL
        Log.d("LibraryViewModel", "Selected card ${card.name}")
    }

    fun clearSelectedCard() {
        _selectedCard.value = null
        _libraryUiState.value = lastState
        Log.d("LibraryViewModel", "Cleared selected card")
        Log.d("LibraryViewModel", "Returned to state: $lastState")
    }

    fun clearAllCards() {
        _cardsToShow.value = emptyList()
        _libraryUiState.value = LibraryUiState.SHOWING_EXPANSIONS
        Log.d("LibraryViewModel", "Cleared all cards")
    }

    private fun sortCards(cards: List<Card>): List<Card> {
        if (cards.isEmpty()) return cards

        val sortedCards = when (_sortType.value) {

            SortType.TYPE -> {
                // String comparison sucks
                val name = _selectedExpansion.value?.name
                if (name == "Base" || name == "Empires") {
                    cards.sortedWith(Card.CardTypeComparator(sortByCostAsTieBreaker = true))
                } else {
                    cards.sortedWith(Card.CardTypeComparator())
                }
            }

            SortType.EXPANSION -> cards.sortedBy { it.sets.first() }
            SortType.ALPHABETICAL -> cards.sortedBy { it.name }
            SortType.COST -> cards.sortedBy { it.cost }
            SortType.ENABLED -> cards.sortedBy { !it.isEnabled }
        }
        Log.d("LibraryViewModel", "Sorted ${sortedCards.size} cards by ${_sortType.value}")
        return sortedCards
    }

    fun updateSortType(newSortType: SortType) {
        _sortType.value = newSortType

        // Sort expansion list
        _cardsToShow.value = sortCards(_cardsToShow.value)
        Log.d("LibraryViewModel", "Updated sort type to ${_sortType.value}")
    }

    fun toggleSearch() {
        _searchActive.value = !_searchActive.value
        if (!_searchActive.value) {
            clearSelectedExpansion()
            clearSearchText()
        }
        Log.d("LibraryViewModel", "Toggled search to ${_searchActive.value}")
    }

    fun clearSearchText() {
        _searchText.value = ""
        Log.d("LibraryViewModel", "Cleared search text")
    }

    fun changeSearchText(newText: String) {
        _searchText.value = newText
        Log.d("LibraryViewModel", "Updated search text to $newText")
    }

    fun searchCards(newText: String) {
        viewModelScope.launch {

            // TODO: Sort? Type sort is broken here
            _cardsToShow.value = cardDao.getFilteredCards("%$newText%")
            _libraryUiState.value = LibraryUiState.SHOWING_SEARCH_RESULTS

            Log.d(
                "LibraryViewModel",
                "Searched for $newText, search results: ${_cardsToShow.value.size}"
            )
        }
    }

    // Card functions
    /*fun loadAllCards() {
        Log.d("LibraryViewModel", "Loading all cards")
        viewModelScope.launch {
            _cards.value = cardDao.getAll()
            sortCards()
            Log.d("LibraryViewModel", "Loaded all ${_cards.value.size} cards")
        }
    }*/

    fun triggerError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun toggleCardEnabled(card: Card) {
        viewModelScope.launch {

            val newIsEnabledState = !card.isEnabled

            // Update database
            cardDao.toggleCardEnabled(card.id, newIsEnabledState)

            // Update object
            _cardsToShow.value = _cardsToShow.value.map { c ->
                if (c.id == card.id) {
                    c.copy(isEnabled = newIsEnabledState)
                } else {
                    c
                }
            }

            // TODO does this make sense? When SortType == ENABLED, changing cards makes them jump
            if (sortType.value == SortType.ENABLED) {
                _cardsToShow.value = sortCards(_cardsToShow.value)
            }

            Log.d("LibraryViewModel", "Toggled card ${card.name} to $newIsEnabledState")
        }
    }
}

private fun getEnabledCardAmount(cards: List<Card>): String {
    val enabledCount = cards.count { it.isEnabled }
    return "(${enabledCount}/${cards.size})"
}

enum class SortType(val text: String) {
    TYPE ("Sort by type"),
    ALPHABETICAL("Sort alphabetically"),
    COST("Sort by cost"),
    EXPANSION("Sort by expansion"),
    ENABLED("Sort by enabled")
    // TODO Sort by edition for library
}