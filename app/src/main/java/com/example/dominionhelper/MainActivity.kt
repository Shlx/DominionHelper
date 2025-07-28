package com.example.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dominionhelper.ui.theme.DominionHelperTheme
import androidx.compose.material3.DrawerValue.*
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dominionhelper.model.OwnedEdition
import com.example.dominionhelper.ui.components.CardDetailPager
import com.example.dominionhelper.ui.components.CardList
import com.example.dominionhelper.ui.CardViewModel
import com.example.dominionhelper.ui.UiScreenState
import com.example.dominionhelper.ui.components.DrawerContent
import com.example.dominionhelper.ui.components.ExpansionList
import com.example.dominionhelper.ui.components.KingdomList
import com.example.dominionhelper.ui.components.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// Random info:
// Prop drilling: passing lots of data down through multiple levels (bad)
// by lazy: loading data only when needed (good)
// Flows: automatically updates UI elements when data changes
// mutableStateOf automatically updates UI elements reliant on the values when they change
// When passing a lambda function like (Card) -> Unit down to another function, should you put in the parameter as early as possible?
// -> As early as possible is good for clarity and separation of concerns. Capture card as early as possible

// It's a strong convention in Jetpack Compose that most non-trivial composables accept a modifier: Modifier = Modifier as their first optional parameter.

// adb pair <ip>:<port>
// adb tcpip 5555

// TODO PROGRAMMING
// Use coil or glide or fresco to load images to avoid "image decoding logging dropped" warnings
// Applicationscope vs LifecycleScope vs CoroutineScope vs whatever
// Flows instead of lists from DAO?
// Use update { in ViewModels
// Save sort type between sessions
// Icons.Filled.??
// Try to thin out some parameters (TopBar)
// After generating kingdom and changing sort type, it is reset after generating a new kingdom
// Add modifier parameter to all Composables?
// Use _somethingFromViewModel.update {} instead of setting _something.value

// TODO FEATURES
// Split piles
// Research landscape rules (I think 2 are recommended)
// VP counter
// Add 6* / 4+ costs (How? cost as a string in json?)
// Warning when navigating back from generated kingdom
// Chose 2 from Events, Landmarks, Projects, Ways, Traits
// Overpay Cards, Coffers, weißer Text auf Schulden
// Rating a kingdom afterwards (+ uploading)
// Kingdom landscape view to show all cards
// Andere Sprachen

// TODO NEXT
// RETHINK DATABASE SCHEME
// Add banned card count to expansions / exp card view)
// Check if there are 10 enabled cards to randomize
// Bug: Crash when clicking an edition!!
// Reenable all cards
// Work on randomization rules
// Bottom navigation bar instead of drawer?
// Refactor this mess
// Sort by expansion should ignore editions (does it?)
// Swap icon in expansion list depending on expansion owned (Also in kingdom list)
// Sorter class
// Bookmarks / Mnemonic??
// Fix differences between ExpansionList and CardList items (padding etc.)

// TODO DESIGN
// Show behind top + nav bar?
// Remove search from detail view?
// Close keyboard when scrolling on search results
// Rethink color gradient on mixed cards
// Explanation for card categories
// Search while viewing expansions -> only results from expansion?
// Add sorting for expansions
// Landscape cards are low res
// Check image sizes, turn placeholders to webp
// Make "no search results" prettier
// Smol discrepancy: the bottom of the player selector padding stacks with the top of the first list element padding, so before scrolling, it's a little too much
// In expansion card list view, the top card has NOT enough space to the top bar. Add padding to top bar?
// Make list items "invisible"
// Icon padding + icons white?
// Font: Princeps
// Split normal and landscape cards in expansion card view
// Search bar: Not aligned with title, show hamburger instead of <-

// TODO BUGS
// Rethink the basic Card flag. I think it's only there for the UI fix?
// -> Nope I think it makes sense for the card randomization. These cards are never pulled without meeting conditions
// Cards that are not in the supply vs cards that cost 0 vs cards that cost nothing??
// Closing search while in detail view fucks shit up
// Mountain Shrine Schulden
// Curse money card bei wunderheilerin
// I think 'set' property can be removed from sets.json
// Don't switch  Cornucopia & Guilds ownedship at the same time. There are regions where the second edition is split
// Wenn man auf Basic geht sind Copper etc mittendrin. Wenn man ein kingdom generated und dann noch mal reingeht, sind die basic cards unten
// Vetoing the last card looks weird
// 1st edition of Cornucopia has 2nd edition cards (at least Rewards)

// Back arrow in top bar, generell überdenken. Macht nur Sinn wenn die view betitelt werden muss
//Fab rund?
//Player Count Auswahl weiterunten
//Crashlytics (firebase)
//League of Shopkeepers, way of the chameleon too long
// Show 0 cost? It's different than {} cost
// In expansion view: if both edition are shown, show icon or text on each card


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

    val uiState by cardViewModel.uiScreenState.collectAsStateWithLifecycle()

    val expansionsWithEditions by cardViewModel.expansionsWithEditions.collectAsStateWithLifecycle()
    val selectedExpansion by cardViewModel.selectedExpansion.collectAsStateWithLifecycle()
    val selectedEdition by cardViewModel.selectedEdition.collectAsStateWithLifecycle()

    val cardsToShow by cardViewModel.cardsToShow.collectAsStateWithLifecycle()
    val kingdom by cardViewModel.kingdom.collectAsStateWithLifecycle()
    val selectedCard by cardViewModel.selectedCard.collectAsStateWithLifecycle()

    val isSearchActive by cardViewModel.searchActive.collectAsStateWithLifecycle()
    val searchText by cardViewModel.searchText.collectAsStateWithLifecycle()
    val sortType by cardViewModel.sortType.collectAsStateWithLifecycle()

    val playerCount by cardViewModel.playerCount.collectAsStateWithLifecycle()

    val errorMessage by cardViewModel.errorMessage.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = Closed)
    val applicationScope = rememberCoroutineScope()
    val topBarTitle by cardViewModel.topBarTitle.collectAsStateWithLifecycle()

    val isDismissEnabled by cardViewModel.isCardDismissalEnabled.collectAsState()

    val cardListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val expansionListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // TODO: Center this message?
    // To display error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            applicationScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                cardViewModel.clearError()
            }
        }
    }

    // Handle back gesture according to state
    BackHandler(enabled = uiState != UiScreenState.SHOWING_EXPANSIONS) {

        when {
            drawerState.isOpen -> applicationScope.launch {
                Log.i("BackHandler", "Close drawer")
                drawerState.close()
            }

            uiState == UiScreenState.SHOWING_EXPANSION_CARDS -> {
                Log.i("BackHandler", "Leave expansion list -> Return to expansion list")
                cardViewModel.clearAllCards()
                cardViewModel.clearSelectedExpansion()

                // Return to top
                applicationScope.launch {
                    cardListState.scrollToItem(0)
                }
            }

            uiState == UiScreenState.SHOWING_KINGDOM -> {
                Log.i("BackHandler", "Leave kingdom -> Return to expansion list")
                cardViewModel.clearAllCards()
                cardViewModel.clearSelectedExpansion()

                // Return to top
                applicationScope.launch {
                    cardListState.scrollToItem(0)
                }
            }

            uiState == UiScreenState.SHOWING_SEARCH_RESULTS -> {
                Log.i("BackHandler", "Deactivate search")
                cardViewModel.toggleSearch() // -> Deactivate search?
                cardViewModel.changeSearchText("")
                cardViewModel.clearAllCards()
            }

            uiState == UiScreenState.SHOWING_CARD_DETAIL -> {
                Log.i("BackHandler", "Deselect card -> Return to card list")
                cardViewModel.clearSelectedCard()
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
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

                        // Return to top, looks weird
                        /*applicationScope.launch {
                            listState.scrollToItem(0)
                        }*/
                    },
                    onSortTypeSelected = { cardViewModel.updateSortType(it, kingdom) },
                    selectedSortType = sortType,
                    topBarTitle = topBarTitle,
                    showSearch = kingdom.isEmpty()
                )
            },
            floatingActionButton = {
                LargeFloatingActionButton(
                    onClick = {
                        Log.i("MainActivity", "Large FAB Clicked")
                        cardViewModel.getRandomKingdom()
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.dice3),
                        contentDescription = "Generate a random kingdom",
                        modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize)
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { innerPadding ->

            // Hoch zum anderen LaunchedEffect?
            LaunchedEffect(key1 = searchText, key2 = isSearchActive) {
                if (isSearchActive && searchText.length >= 2) {
                    Log.i("LaunchedEffect", "Getting cards by search text $searchText")
                    cardViewModel.searchCards(searchText)
                }
            }

            when (uiState) {

                // Show all expansions in a list
                UiScreenState.SHOWING_EXPANSIONS -> {
                    Log.i("MainView", "View expansion list (${expansionsWithEditions.size})")
                    ExpansionList(
                        expansions = expansionsWithEditions,
                        onExpansionClick = {
                            cardViewModel.selectExpansion(it)
                        },
                        onEditionClick = { cardViewModel.selectEdition(it) },
                        ownershipText = { cardViewModel.getOwnershipText(it) },
                        onOwnershipToggle = { expansion, newOwned ->
                            cardViewModel.updateExpansionOwnership(expansion, newOwned)
                        },
                        onToggleExpansion = { cardViewModel.toggleExpansion(it) },
                        modifier = Modifier.padding(innerPadding),
                        listState = expansionListState
                    )
                }

                // Show the cards within the selected expansion
                UiScreenState.SHOWING_EXPANSION_CARDS -> {

                    Log.i(
                        "MainView",
                        "View card list of expansion ${selectedExpansion!!.name} (${cardsToShow.size})"
                    )
                    CardList(
                        modifier = Modifier.padding(innerPadding),
                        cardList = cardsToShow,
                        includeEditionSelection = cardViewModel.expansionHasTwoEditions(
                            selectedExpansion!!) && !isSearchActive,
                        selectedEdition = selectedEdition,
                        onEditionSelected = { editionClicked, ownedEdition ->
                            cardViewModel.selectEdition(
                                selectedExpansion!!,
                                editionClicked,
                                ownedEdition
                            )
                        },
                        onCardClick = { cardViewModel.selectCard(it) },
                        onToggleEnable = { cardViewModel.toggleCardEnabled(it) },
                        listState = cardListState
                    )
                }

                // Show generated kingdom
                UiScreenState.SHOWING_KINGDOM -> {
                    Log.i(
                        "MainView",
                        "View card list (Random: ${kingdom.randomCards.size}, Dependent: ${kingdom.dependentCards.size}, Basic: ${kingdom.basicCards.size} cards, Landscape: ${kingdom.landscapeCards.size})"
                    )
                    KingdomList(
                        kingdom = kingdom,
                        onCardClick = { cardViewModel.selectCard(it) },
                        modifier = Modifier.padding(innerPadding),
                        selectedPlayers = playerCount,
                        onPlayerCountChange = { cardViewModel.updatePlayerCount(kingdom, it) },
                        listState = cardListState,
                        isDismissEnabled = isDismissEnabled,
                        onCardDismissed = { cardViewModel.onCardDismissed(it) }
                    )
                }

                // Show search results
                // Rework this. Too much stuff from expansion list is irrelevant here
                UiScreenState.SHOWING_SEARCH_RESULTS -> {
                    Log.i("MainView", "Showing search results (${cardsToShow.size})")
                    CardList(
                        modifier = Modifier.padding(innerPadding),
                        cardList = cardsToShow,
                        includeEditionSelection = false,
                        selectedEdition = OwnedEdition.NONE,
                        onEditionSelected = { editionClicked, ownedEdition ->
                            cardViewModel.selectEdition(
                                selectedExpansion!!,
                                editionClicked,
                                ownedEdition
                            )
                        },
                        onCardClick = { cardViewModel.selectCard(it) },
                        onToggleEnable = { cardViewModel.toggleCardEnabled(it) },
                        listState = cardListState
                    )
                }

                // Show detail view of a single card
                UiScreenState.SHOWING_CARD_DETAIL -> {
                    Log.i("MainView", "View card detail (${selectedCard?.name})")
                    CardDetailPager(
                        modifier = Modifier.padding(innerPadding),
                        // This feels weird but maybe it's ok?
                        cardList = cardsToShow + kingdom.randomCards.keys.toList() + kingdom.dependentCards.keys.toList() + kingdom.basicCards.keys.toList() + kingdom.startingCards.keys.toList(),
                        initialCard = selectedCard!!,
                        onClick = { cardViewModel.clearSelectedCard() }
                    )
                }
            }
        }
    }
}
