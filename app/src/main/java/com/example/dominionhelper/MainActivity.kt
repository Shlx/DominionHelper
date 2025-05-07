package com.example.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dominionhelper.ui.theme.DominionHelperTheme
import androidx.compose.material3.DrawerValue.*
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dominionhelper.ui.components.CardDetailPager
import com.example.dominionhelper.ui.components.CardList
import com.example.dominionhelper.ui.CardViewModel
import com.example.dominionhelper.ui.components.DrawerContent
import com.example.dominionhelper.ui.components.ExpansionList2
import com.example.dominionhelper.ui.components.KingdomList
import com.example.dominionhelper.ui.components.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// Random info:
// Prop drilling: passing lots of data down through mutliple levels (bad)
// by lazy: loading data only when needed (good)
// Flows: automatically updates UI elements when data changes
// mutableStateOf automatically updates UI elements reliant on the values when they change

// TODO PROGRAMMING
// Use coil or glide or fresco to load images to avoid "image decoding logging dropped" warnings
// Applicationscope vs LifecycleScope vs CoroutineScope vs whatever
// Flows instead of lists from DAO?
// Use update { in ViewModels
// Save sort type between sessions
// Icons.Filled.??
// Try to thin out some parameters (TopBar)
// After generating kingdom and changing sort type, it is reset after generating a new kingdom

// TODO FEATURES
// Split piles
// Research landscape rules (I think 2 are recommended)
// VP counter
// Add rules for randomization
// Add 6* / 4+ costs (How? cost as a string in json?)
// Warning when navigating back from generated kingdom
// Chose 2 from Events, Landmarks, Projects, Ways, Traits
// Overpay Cards, Coffers, weißer Text auf Schulden
// Veto on generated cards (Swipe away)
// Rating a kingdom afterwards (+ uploading)

// TODO NEXT
// Refactor this mess
// Sort by expansion should ignore editions (does it?)
// Think about how to handle Cornucopia & Guilds
// Swap icon in expansion list depending on expansion owned (Also in kingdom list)
// Sorter class
// Maybe save "hasFirst/SecondEdition" in a variable (ExpansionWithEditions)
// Bookmakrs / Mnemonic??

// TODO DESIGN
// Show behind top + nav bar?
// Improve Expansion view
// Remove search from detail view?
// Close keyboard when scrolling on search results
// Rethink color gradient on mixed cards
// Explanation for card categories
// Search while viewing expansions -> only results from expansion?
// Add sorting for expansions?
// Landscape cards are low res
// Check image sizes, turn placeholders to webp
// Make "no search results" prettier
// Smol discrepancy: the bottom of the player selector padding stacks with the top of the first list element padding, so before scrolling, it's a little too much
// In expansion card list view, the top card has NOT enough space to the top bar. Add padding to top bar?

// TODO BUGS
// Rethink the basic Card flag. I think it's only there for the UI fix?
// -> Nope I think it makes sense for the card randomization. These cards are never pulled without meeting conditions
// Cards that are not in the supply vs cards that cost 0 vs cards that cost nothing??
// Closing search while in detail view fucks shit up


// I think list state is shared between search / expansion and random cards (doesn't reset)
// -> Seems fine between expansion and random cards, expansion to search needs to reset
// Going back from expansion list resets even though it shouldn't


// TODO check: Allies, Menagerie, Renaissance, Nocturne, Empires, Adventures, Guilds, Dark Ages, Hinterlands, Cornucopia, Prosperity, Alchemy, Seaside, Intrigue, Dominion

// TODO: Questions for users?
// - Is there a need to view first and second edition cards in one list?

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val cardViewModel: CardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DominionHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView(cardViewModel)
                }
            }
        }
    }
}

@Composable
fun MainView(cardViewModel: CardViewModel) {

    val expansionsWithEditions by cardViewModel.expansionsWithEditions.collectAsStateWithLifecycle()
    val selectedExpansion by cardViewModel.selectedExpansion.collectAsStateWithLifecycle()
    val selectedEdition by cardViewModel.selectedEdition.collectAsStateWithLifecycle()

    val cardsToShow by cardViewModel.cardsToShow.collectAsStateWithLifecycle()
    val expansionCards by cardViewModel.expansionCards.collectAsStateWithLifecycle()
    val kingdom by cardViewModel.kingdom.collectAsStateWithLifecycle()
    val selectedCard by cardViewModel.selectedCard.collectAsStateWithLifecycle()

    val isSearchActive by cardViewModel.searchActive.collectAsStateWithLifecycle()
    val searchText by cardViewModel.searchText.collectAsStateWithLifecycle()
    val sortType by cardViewModel.sortType.collectAsStateWithLifecycle()

    val playerCount by cardViewModel.playerCount.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = Closed)
    val applicationScope = rememberCoroutineScope()
    var topBarTitle by remember { mutableStateOf("Dominion Helper") }

    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val gridState = rememberSaveable(saver = LazyGridState.Saver) {
        LazyGridState()
    }

    // Set the top bar title according to state
    LaunchedEffect(key1 = cardsToShow, key2 = selectedExpansion) {
        topBarTitle = if (selectedExpansion != null) {
            selectedExpansion!!.name
        } else if (cardsToShow) {
            "Generated Kingdom"
        } else {
            "Dominion Helper"
        }
    }

    // Handle back gesture according to state
    BackHandler(enabled = cardsToShow || drawerState.isOpen || isSearchActive) {

        when {
            drawerState.isOpen -> applicationScope.launch {
                Log.i("BackHandler", "Close drawer")
                drawerState.close()
            }

            selectedCard != null -> {
                Log.i("BackHandler", "Deselect card -> Return to card list")
                cardViewModel.clearSelectedCard()
            }

            isSearchActive -> {
                Log.i("BackHandler", "Deactivate search")
                cardViewModel.toggleSearch()
                cardViewModel.changeSearchText("")
                cardViewModel.clearAllCards()
            }

            cardsToShow -> {
                Log.i("BackHandler", "Leave card list -> Return to expansion list")
                cardViewModel.clearAllCards()
                cardViewModel.clearSelectedExpansion()

                // Return to top
                applicationScope.launch {
                    listState.scrollToItem(0)
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(applicationScope, drawerState, "Home")
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    scope = applicationScope,
                    drawerState = drawerState,
                    isSearchActive = isSearchActive,
                    onSearchClicked = { cardViewModel.toggleSearch() },
                    searchText = searchText,
                    onSearchTextChange = { cardViewModel.changeSearchText(it) },
                    onRandomCardsClicked = {
                        cardViewModel.getRandomKingdom()

                        // Return to top
                        applicationScope.launch {
                            // Looks weird
                            //listState.scrollToItem(0)
                        }
                    },
                    onSortTypeSelected = { cardViewModel.updateSortType(it, kingdom) },
                    selectedSortType = sortType,
                    topBarTitle = topBarTitle,
                    hideSearch = kingdom.isNotEmpty()
                )
            }
        ) { innerPadding ->

            LaunchedEffect(key1 = searchText, key2 = isSearchActive) {
                if (isSearchActive && searchText.length >= 2) {
                    Log.i("LaunchedEffect", "Getting cards by search text $searchText")
                    cardViewModel.searchCards(searchText)
                }
            }

            when {

                // Detail view
                selectedCard != null -> {
                    Log.i("MainView", "View card detail (${selectedCard?.name})")
                    CardDetailPager(
                        modifier = Modifier.padding(innerPadding),
                        // This feels weird but maybe it's ok?
                        cardList = expansionCards + kingdom.randomCards.keys.toList() + kingdom.dependentCards.keys.toList() + kingdom.basicCards.keys.toList() + kingdom.startingCards.keys.toList(),
                        initialCard = selectedCard!!
                    )
                }

                // Show a list of cards
                cardsToShow -> {
                    Log.i(
                        "MainView",
                        "View card list (Expansion: ${expansionCards.size}, Random: ${kingdom.randomCards.size}, Dependent: ${kingdom.dependentCards.size}, Basic: ${kingdom.basicCards.size} cards)"
                    )

                    // Show expansion or search result
                    // Having no separation here is kind of weird I think
                    if (expansionCards.isNotEmpty()) {
                        CardList(
                            cardList = expansionCards,
                            includeEditionSelection = cardViewModel.expansionHasTwoEditions(selectedExpansion!!) && !isSearchActive,
                            onEditionSelected = { editionNumber ->
                                cardViewModel.selectEdition(selectedExpansion!!, editionNumber)
                            },
                            selectedEdition = selectedEdition,
                            onCardClick = { cardViewModel.selectCard(it) },
                            modifier = Modifier.padding(innerPadding),
                            listState = listState
                        )

                        // Show generated kingdom
                    } else if (kingdom != Kingdom()) {
                        KingdomList(
                            kingdom = kingdom,
                            onCardClick = { cardViewModel.selectCard(it) },
                            modifier = Modifier.padding(innerPadding),
                            selectedPlayers = playerCount,
                            onPlayerCountChange = { cardViewModel.updatePlayerCount(kingdom, it) },
                            listState = listState
                        )
                    } else {
                        Text("Nah", modifier = Modifier.padding(innerPadding))
                    }
                }

                // Show all expansions in a grid
                else -> {
                    Log.i("MainView", "View expansion list")
                    /*ExpansionList(
                        expansions = expansionsWithEditions,
                        onExpansionClick = { expansionsWithEditions ->
                            cardViewModel.loadCardsByExpansion(expansionsWithEditions)
                            cardViewModel.selectExpansion((expansionsWithEditions))
                        },
                        onToggleClick = {
                            cardViewModel.toggleIsOwned(it)
                        },
                        ownageText = { cardViewModel.getOwnershipText(it) },
                        modifier = Modifier.padding(innerPadding),
                        gridState = gridState
                    )*/
                    ExpansionList2(
                        expansions = expansionsWithEditions,
                        onExpansionClick = { expansionsWithEditions ->
                            cardViewModel.loadCardsByExpansion(expansionsWithEditions)
                            cardViewModel.selectExpansion((expansionsWithEditions))
                        },
                        onOwnershipToggle = {
                            cardViewModel.toggleIsOwned(it)
                        },
                        onToggleExpansion = { cardViewModel.toggleExpansion(it) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
