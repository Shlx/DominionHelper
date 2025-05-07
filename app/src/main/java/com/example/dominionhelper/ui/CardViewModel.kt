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
import com.example.dominionhelper.model.ExpansionWithEditions
import com.example.dominionhelper.model.OwnedEdition
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
    private val _expansionsWithEditions = MutableStateFlow<List<ExpansionWithEditions>>(emptyList())
    val expansionsWithEditions: StateFlow<List<ExpansionWithEditions>> =
        _expansionsWithEditions.asStateFlow()

    private val _selectedExpansion = MutableStateFlow<ExpansionWithEditions?>(null)
    val selectedExpansion: StateFlow<ExpansionWithEditions?> = _selectedExpansion.asStateFlow()

    private val _selectedEdition = MutableStateFlow<OwnedEdition>(OwnedEdition.NONE)
    val selectedEdition: StateFlow<OwnedEdition> = _selectedEdition.asStateFlow()

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

    private val _sortType = MutableStateFlow(SortType.DEFAULT)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    // Player count
    private val _playerCount = MutableStateFlow<Int>(2)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    init {
        loadExpansionsWithEditions()
    }

    fun toggleExpansion(expansionToToggle: ExpansionWithEditions) {
        Log.i("CardViewModel", "Toggling expansion ${expansionToToggle.name}: ${expansionToToggle.isExpanded}")
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
            Log.i("CardViewModel", "Toggled expansion ${expansionToToggle.name}: ${expansionToToggle.isExpanded}")
        }
    }

    // Load all expansions and their editions, grouped by name
    private fun loadExpansionsWithEditions() {
        viewModelScope.launch {
            expansionDao.getAll().collect { allExpansions ->

                // Make a map of name -> list of editions
                val expansionsGrouped = allExpansions.groupBy { it.name }

                // Assemble ExpansionWithEditions object for each entry
                val expansionsWithEditions = expansionsGrouped.map { (expansionName, editions) ->
                    val firstEdition = editions.find { it.edition == 1 }
                    val secondEdition = editions.find { it.edition == 2 }

                    // Choose the correct image
                    val image = when {
                        firstEdition != null -> firstEdition.imageName
                        secondEdition != null -> secondEdition.imageName // Only for Cornucopia + Guilds 2nd edition
                        else -> throw java.lang.IllegalArgumentException("No edition found.")
                    }

                    ExpansionWithEditions(
                        name = expansionName,
                        firstEdition = firstEdition,
                        secondEdition = secondEdition,
                        image = image
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

    // Toggle through expansion ownership options (Unowned, first / second / both owned)
    fun toggleIsOwned(expansion: ExpansionWithEditions) {
        viewModelScope.launch {

            val isFirstOwned = expansion.firstEdition?.isOwned == true
            val isSecondOwned = expansion.secondEdition?.isOwned == true
            Log.i(
                "CardViewModel",
                "Toggling ownage for ${expansion.name} (First: $isFirstOwned, Second: $isSecondOwned)"
            )

            when {
                isFirstOwned && isSecondOwned -> {
                    // Both editions owned -> Unowned
                    Log.i("CardViewModel", "Both editions owned -> Unowned")
                    updateExpansionOwnership(expansion.firstEdition, false)
                    updateExpansionOwnership(expansion.secondEdition, false)
                }

                !isFirstOwned && !isSecondOwned -> {
                    if (expansion.firstEdition != null) {
                        // Unowned -> First edition owned
                        Log.i("CardViewModel", "Unowned -> First Edition Owned")
                        updateExpansionOwnership(expansion.firstEdition, true)
                    } else {
                        // Unowned -> Second edition owned
                        Log.i(
                            "CardViewModel",
                            "Unowned -> Second Edition Owned (There is no first edition)"
                        )
                        expansion.secondEdition?.let { updateExpansionOwnership(it, true) }
                    }
                }

                isFirstOwned -> {
                    if (expansion.secondEdition != null) {
                        // First edition owned -> Second edition owned
                        Log.i("CardViewModel", "First Edition Owned -> Second Edition Owned")
                        updateExpansionOwnership(expansion.secondEdition, true)
                        updateExpansionOwnership(expansion.firstEdition, false)
                    } else {
                        // First edition owned -> Unowned
                        Log.i(
                            "CardViewModel",
                            "First Edition Owned -> Unowned (There is no second edition)"
                        )
                        updateExpansionOwnership(expansion.firstEdition, false)
                    }

                }

                isSecondOwned -> {
                    if (expansion.firstEdition != null) {
                        // Second edition owned -> Both editions owned
                        Log.i("CardViewModel", "Second Edition Owned -> Both editions owned")
                        updateExpansionOwnership(expansion.firstEdition, true)
                    } else {
                        // Second edition owned -> Unowned
                        Log.i(
                            "CardViewModel",
                            "Second Edition Owned -> Unowned (There is no first edition)"
                        )
                        updateExpansionOwnership(expansion.secondEdition, false)
                    }
                }
            }
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
            // TODO: Understand this
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
                "UpdateExpansionOwndership(): Updated isOwned for ${expansion.name}[${expansion.edition}] to $newIsOwned"
            )
        }
    }

    fun getOwnershipText(expansion: ExpansionWithEditions): String {

        val isFirstOwned = expansion.firstEdition?.isOwned == true
        val isSecondOwned = expansion.secondEdition?.isOwned == true

        return when {
            isFirstOwned && isSecondOwned -> "Both Editions Owned"
            !isFirstOwned && !isSecondOwned -> "Unowned"
            isFirstOwned ->
                if (expansion.secondEdition == null) {
                    "Owned"
                } else {
                    "First Edition Owned"
                }
            isSecondOwned -> "Second Edition Owned"
            else -> throw java.lang.IllegalArgumentException("Invalid ownership state.")
        }
    }

    fun whichEditionIsOwned(expansion: ExpansionWithEditions): OwnedEdition {

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

    // Expansion functions
    fun selectExpansion(expansion: ExpansionWithEditions) {
        _selectedExpansion.value = expansion
        _selectedEdition.value = whichEditionIsOwned(expansion)
        Log.d("CardViewModel", "Selected ${expansion.name}")
    }

    fun clearSelectedExpansion() {
        _selectedExpansion.value = null
        _expansionCards.value = emptyList()
        Log.d("CardViewModel", "Cleared selected expansion")
    }

    // TODO: vgl. loadCardsByExpansion
    // When edition selector in CardList is pressed
    fun selectEdition(expansion: ExpansionWithEditions, editionNumber: Int) {
        viewModelScope.launch {
            if (editionNumber == 1) {
                _expansionCards.value =
                    sortCards(cardDao.getCardsByExpansion(expansion.firstEdition!!.id))
                _selectedEdition.value = OwnedEdition.FIRST
            } else {
                _expansionCards.value =
                    sortCards(cardDao.getCardsByExpansion(expansion.secondEdition!!.id))
                _selectedEdition.value = OwnedEdition.SECOND
            }
            Log.d("CardViewModel", "Selected edition $editionNumber for ${expansion.name}")
        }
    }

    // TODO: vgl. selectEdition
    // When expansion is selected
    fun loadCardsByExpansion(expansion: ExpansionWithEditions) {

        viewModelScope.launch {

            if (expansion.firstEdition?.isOwned == true) {
                _expansionCards.value =
                    sortCards(cardDao.getCardsByExpansion(expansion.firstEdition.id))
            } else if (expansion.secondEdition?.isOwned == true) {
                _expansionCards.value =
                    sortCards(cardDao.getCardsByExpansion(expansion.secondEdition.id))
            } else {
                // If neither or both editions are owned, show all cards
                val set = mutableSetOf<Card>()
                if (expansion.firstEdition != null) {
                    set.addAll(cardDao.getCardsByExpansion(expansion.firstEdition.id))
                }
                if (expansion.secondEdition != null) {
                    set.addAll(cardDao.getCardsByExpansion(expansion.secondEdition.id))
                }
                _expansionCards.value = sortCards(set.toList())
            }

            _cardsToShow.value = true
            Log.d(
                "CardViewModel",
                "Loaded ${_expansionCards.value.size} cards for expansion ${expansion.name}"
            )
        }
    }

    fun expansionHasTwoEditions(expansion: ExpansionWithEditions): Boolean {
        return expansion.firstEdition != null && expansion.secondEdition != null
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
            updateSortType(SortType.EXPANSION, _kingdom.value)
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
            SortType.DEFAULT -> cards.sortedBy { it.id }
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
            SortType.DEFAULT -> cards.entries.sortedBy { it.key.id }
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
}

enum class SortType(val text: String) {
    ALPHABETICAL("Sort alphabetically"),
    COST("Sort by cost"),
    EXPANSION("Sort by expansion"),
    DEFAULT("Sort by default")
}