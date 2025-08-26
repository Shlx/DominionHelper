package com.marvinsuhr.dominionhelper.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marvinsuhr.dominionhelper.Kingdom
import com.marvinsuhr.dominionhelper.KingdomGenerator
import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.data.ExpansionDao
import com.marvinsuhr.dominionhelper.data.UserPrefsRepository
import com.marvinsuhr.dominionhelper.model.Card
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

enum class KingdomUiState {
    LOADING,
    SHOWING_KINGDOM,
    SHOWING_CARD_DETAIL
}

@HiltViewModel
class KingdomViewModel @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao,
    private val kingdomGenerator: KingdomGenerator,
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    // Variable for tracking the current state
    private val _kingdomUiState = MutableStateFlow(KingdomUiState.LOADING)
    val kingdomUiState: StateFlow<KingdomUiState> = _kingdomUiState.asStateFlow()
    private var lastState: KingdomUiState = KingdomUiState.SHOWING_KINGDOM

    private val _kingdom = MutableStateFlow(Kingdom())
    val kingdom: StateFlow<Kingdom> = _kingdom.asStateFlow()

    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: StateFlow<Card?> = _selectedCard.asStateFlow()

    // Player count
    private val _playerCount = MutableStateFlow(2)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.ALPHABETICAL)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

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

    // TopBarTitle stuff

    fun selectCard(card: Card) {
        _selectedCard.value = card
        lastState = _kingdomUiState.value
        _kingdomUiState.value = KingdomUiState.SHOWING_CARD_DETAIL
        Log.d("LibraryViewModel", "Selected card ${card.name}")
    }

    fun clearSelectedCard() {
        _selectedCard.value = null
        _kingdomUiState.value = lastState
        Log.d("LibraryViewModel", "Cleared selected card")
    }

    fun getRandomKingdom() {
        viewModelScope.launch {

            if (expansionDao.getOwnedOnce().count() < 1) {
                Log.d(
                    "LibraryViewModel",
                    "No kingdom generated, as the user does not own any expansion"
                )
                triggerError("You need at least one expansion to generate a kingdom.")
                return@launch
            }

            _kingdomUiState.value = KingdomUiState.LOADING
            _kingdom.value = kingdomGenerator.generateKingdom()
            updateSortType(SortType.EXPANSION)
            updatePlayerCount(_kingdom.value, 2)
            clearSelectedCard()
            _kingdomUiState.value = KingdomUiState.SHOWING_KINGDOM
        }
    }

    fun updateSortType(newSortType: SortType) {
        _sortType.value = newSortType
        val kingdom = _kingdom.value

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

        Log.d("LibraryViewModel", "Updated sort type to ${_sortType.value}")
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
        Log.d("LibraryViewModel", "Sorted ${sortedCards.size} cards by ${_sortType.value}")
        return sortedCards
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
        Log.d("LibraryViewModel", "Selected player count $count")
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

    fun triggerError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun onCardDismissed(dismissedCard: Card) {

        val currentKingdom = _kingdom.value

        // Check if the card to be dismissed is actually present
        // (only random and landscape cards are dismissable)
        if (!currentKingdom.randomCards.containsKey(dismissedCard)
            && !currentKingdom.landscapeCards.containsKey(dismissedCard)
        ) {
            Log.w(
                "LibraryViewModel",
                "Attempted to dismiss card '${dismissedCard.name}' not found in the current kingdom."
            )
            return
        }

        Log.i("LibraryViewModel", "Dismissing card '${dismissedCard.name}' from the kingdom.")

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
            "LibraryViewModel",
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
                "LibraryViewModel",
                "Failed to generate a replacement card for '${dismissedCard.name}'."
            )
            triggerError("Could not find a replacement card.")
            return
        }

        Log.i("LibraryViewModel", "Replaced '${dismissedCard.name}' with '${newCard.name}'.")
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