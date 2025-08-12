package com.marvinsuhr.dominionhelper.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marvinsuhr.dominionhelper.Kingdom
import com.marvinsuhr.dominionhelper.KingdomGenerator
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.model.Expansion
import com.marvinsuhr.dominionhelper.data.ExpansionDao
import com.marvinsuhr.dominionhelper.data.UserPrefsRepository
import com.marvinsuhr.dominionhelper.model.ExpansionWithEditions
import com.marvinsuhr.dominionhelper.model.OwnedEdition
import com.marvinsuhr.dominionhelper.utils.Constants
import com.marvinsuhr.dominionhelper.utils.insertOrReplaceAtKeyPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class UiScreenState {
    SHOWING_EXPANSIONS,
    SHOWING_EXPANSION_CARDS,
    SHOWING_KINGDOM,
    SHOWING_SEARCH_RESULTS,
    SHOWING_CARD_DETAIL
 }

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao,
    private val kingdomGenerator: KingdomGenerator,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    // Variable for tracking the current state
    private val _uiScreenState = MutableStateFlow(UiScreenState.SHOWING_EXPANSIONS)
    val uiScreenState: StateFlow<UiScreenState> = _uiScreenState.asStateFlow()
    private var lastState: UiScreenState = UiScreenState.SHOWING_EXPANSIONS

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

    private val _kingdom = MutableStateFlow(Kingdom())
    val kingdom: StateFlow<Kingdom> = _kingdom.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    // Search related
    private val _searchActive = MutableStateFlow(false)
    val searchActive: StateFlow<Boolean> = _searchActive.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.ALPHABETICAL)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    // Player count
    private val _playerCount = MutableStateFlow(2)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val vetoMode: StateFlow<VetoMode> = userPrefsRepository.vetoMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Constants.DEFAULT_VETO_MODE
        )

    val isCardDismissalEnabled: StateFlow<Boolean> = combine(
        vetoMode,
        _kingdom
    ) { currentVetoMode, currentKingdom ->
        currentVetoMode != VetoMode.NO_REROLL || currentKingdom.randomCards.size > 10
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val topBarTitle: StateFlow<String> = combine(
        uiScreenState,
        selectedExpansion,
        selectedCard,
        cardsToShow
    ) { uiScreenState, selectedExpansion, selectedCard, cardsToShow ->
        when (uiScreenState) {
            UiScreenState.SHOWING_EXPANSIONS -> "Dominion Helper"
            UiScreenState.SHOWING_EXPANSION_CARDS -> {
                selectedExpansion?.let { expansion ->
                    "${expansion.name} ${getEnabledCardAmount(cardsToShow)}"
                } ?: "Cards"
            }
            UiScreenState.SHOWING_KINGDOM -> "Generated Kingdom"
            UiScreenState.SHOWING_SEARCH_RESULTS -> "Search Results" // I think this isn't shown
            UiScreenState.SHOWING_CARD_DETAIL -> selectedCard?.name ?: "Details"
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Dominion Helper"
    )

    init {
        loadExpansionsWithEditions()
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
                    "CardViewModel",
                    "Loaded expansions [${expansionsWithEditions.size}] with editions [${allExpansions.size}]"
                )
            }
        }
    }

    fun toggleExpansion(expansionToToggle: ExpansionWithEditions) {
        Log.i(
            "CardViewModel",
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
                "CardViewModel",
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
                "CardViewModel",
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
            _cardsToShow.value = sortCards(set.toList())

            Log.d(
                "CardViewModel",
                "Loaded ${_cardsToShow.value.size} cards for expansion ${expansion.name}"
            )

            _selectedExpansion.value = expansion
            _selectedEdition.value = ownedEditions
            _uiScreenState.value = UiScreenState.SHOWING_EXPANSION_CARDS
            Log.d("CardViewModel", "Selected ${expansion.name}")
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
        _uiScreenState.value = UiScreenState.SHOWING_EXPANSIONS
        Log.d("CardViewModel", "Cleared selected expansion")
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
            Log.d("CardViewModel", "Selected edition $clickedEditionNumber for ${expansion.name} -> $currentOwnedEdition")
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

            Log.d("CardViewModel", "Selected edition ${expansion.name}")
        }
    }

    fun expansionHasTwoEditions(expansion: ExpansionWithEditions): Boolean {
        return expansion.firstEdition != null && expansion.secondEdition != null
    }

    fun selectCard(card: Card) {
        _selectedCard.value = card
        lastState = _uiScreenState.value
        _uiScreenState.value = UiScreenState.SHOWING_CARD_DETAIL
        Log.d("CardViewModel", "Selected card ${card.name}")
    }

    fun clearSelectedCard() {
        _selectedCard.value = null
        _uiScreenState.value = lastState
        Log.d("CardViewModel", "Cleared selected card")
    }

    fun getRandomKingdom() {
        viewModelScope.launch {

            if (expansionDao.getOwnedOnce().count() < 1) {
                Log.d(
                    "CardViewModel",
                    "No kingdom generated, as the user does not own any expansion"
                )
                triggerError("You need at least one expansion to generate a kingdom.")
                return@launch
            }

            _kingdom.value = kingdomGenerator.generateKingdom()
            updateSortType(SortType.EXPANSION, _kingdom.value)
            updatePlayerCount(_kingdom.value, 2)
            clearSelectedExpansion() // Clear this AFTER the kingdom is generated
            clearSelectedCard()

            if (searchActive.value) {
                toggleSearch()
                changeSearchText("")
            }

            _uiScreenState.value = UiScreenState.SHOWING_KINGDOM
        }
    }

    fun clearAllCards() {
        _cardsToShow.value = emptyList()
        _kingdom.value = Kingdom()
        _uiScreenState.value = UiScreenState.SHOWING_EXPANSIONS
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
            // This does not work correctly
            SortType.EXPANSION -> cards.entries.sortedBy { it.key.sets.first().name.take(3) }
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
        _cardsToShow.value = sortCards(_cardsToShow.value)

        // Sort kingdom lists
        val sortedRandomCards = sortCards(kingdom.randomCards)
        val sortedBasicCards = sortCards(kingdom.basicCards)
        val sortedDependentCards = sortCards(kingdom.dependentCards)
        val sortedStartingCards = sortCards(kingdom.startingCards)
        val sortedLandscapeCards = sortCards(kingdom.landscapeCards)

        viewModelScope.launch {

            // TODO: Figure this out
            // Why the hell is this necessary huh
            _kingdom.value.randomCards[cardDao.getCardByName("Copper")!!] = 3
            _kingdom.value = kingdom.copy(
                randomCards = sortedRandomCards,
                dependentCards = sortedDependentCards,
                basicCards = sortedBasicCards,
                startingCards = sortedStartingCards,
                landscapeCards = sortedLandscapeCards
            )
        }

        Log.d("CardViewModel", "Updated sort type to ${_sortType.value}")
    }

    fun toggleSearch() {
        _searchActive.value = !_searchActive.value
        if (!_searchActive.value) {
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

            _cardsToShow.value = sortCards(cardDao.getFilteredCards("%$newText%"))
            _uiScreenState.value = UiScreenState.SHOWING_SEARCH_RESULTS

            Log.d(
                "CardViewModel",
                "Searched for $newText, search results: ${_cardsToShow.value.size}"
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

    fun getCardAmounts(
        cards: LinkedHashMap<Card, Int>,
        playerCount: Int
    ): LinkedHashMap<Card, Int> {
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

    // Card functions
    /*fun loadAllCards() {
        Log.d("CardViewModel", "Loading all cards")
        viewModelScope.launch {
            _cards.value = cardDao.getAll()
            sortCards()
            Log.d("CardViewModel", "Loaded all ${_cards.value.size} cards")
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

            Log.d("CardViewModel", "Toggled card ${card.name} to $newIsEnabledState")
        }
    }

    fun onCardDismissed(dismissedCard: Card) {

        val currentKingdom = _kingdom.value

        // Check if the card to be dismissed is actually present
        // (only random and landscape cards are dismissable)
        if (!currentKingdom.randomCards.containsKey(dismissedCard)
            && !currentKingdom.landscapeCards.containsKey(dismissedCard)
        ) {
            Log.w(
                "CardViewModel",
                "Attempted to dismiss card '${dismissedCard.name}' not found in the current kingdom."
            )
            return
        }

        Log.i("CardViewModel", "Dismissing card '${dismissedCard.name}' from the kingdom.")

        viewModelScope.launch {

            if (vetoMode.first() == VetoMode.NO_REROLL) {
                handleNoRerollDismissal(dismissedCard, dismissedCard.landscape)
            } else {
                handleRerollDismissal(dismissedCard, currentKingdom, dismissedCard.landscape)
            }
        }
    }

    private fun handleNoRerollDismissal(
        dismissedCard: Card,
        wasLandscape: Boolean
    ) {
        Log.i(
            "CardViewModel",
            "VetoMode is NO_REROLL. Removing '${dismissedCard.name}' without replacement."
        )
        _kingdom.update { currentKingdom ->
            when {
                wasLandscape -> currentKingdom.copy(
                    landscapeCards = LinkedHashMap(
                        currentKingdom.landscapeCards.toMutableMap()
                            .apply { remove(dismissedCard) })
                )

                else -> currentKingdom.copy(
                    randomCards = LinkedHashMap(
                        currentKingdom.randomCards.toMutableMap()
                            .apply { remove(dismissedCard) })
                )
            }
        }
    }

    private suspend fun handleRerollDismissal(
        dismissedCard: Card,
        kingdomSnapshot: Kingdom,
        wasLandscape: Boolean
    ) {
        // Determine which list to use for exclusion and replacement target
        val originalCardsMap =
            if (wasLandscape) kingdomSnapshot.landscapeCards else kingdomSnapshot.randomCards
        val cardsToExclude = originalCardsMap.keys.toMutableSet()

        val newCard = kingdomGenerator.replaceCardInKingdom(dismissedCard, cardsToExclude)

        if (newCard == null) {
            Log.e(
                "CardViewModel",
                "Failed to generate a replacement card for '${dismissedCard.name}'."
            )
            triggerError("Could not find a replacement card.")
            return
        }

        Log.i("CardViewModel", "Replaced '${dismissedCard.name}' with '${newCard.name}'.")
        _kingdom.update { currentKingdom ->
            if (newCard.landscape) {
                currentKingdom.copy(
                    landscapeCards = insertOrReplaceAtKeyPosition(
                        map = kingdomSnapshot.landscapeCards,
                        targetKey = dismissedCard,
                        newKey = newCard,
                        newValue = 1
                    )
                )
            } else {
                currentKingdom.copy(
                    randomCards = insertOrReplaceAtKeyPosition(
                        map = kingdomSnapshot.randomCards,
                        targetKey = dismissedCard,
                        newKey = newCard,
                        newValue = 1
                    )
                )
            }
        }
    }
}

private fun getEnabledCardAmount(cards: List<Card>): String {
    val enabledCount = cards.count { it.isEnabled }
    return "(${enabledCount}/${cards.size})"
}

enum class SortType(val text: String) {
    ALPHABETICAL("Sort alphabetically"),
    COST("Sort by cost"),
    EXPANSION("Sort by expansion")
}