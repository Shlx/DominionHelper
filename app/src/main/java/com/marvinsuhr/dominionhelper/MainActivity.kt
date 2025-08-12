package com.marvinsuhr.dominionhelper

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
import com.marvinsuhr.dominionhelper.ui.theme.DominionHelperTheme
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
import com.marvinsuhr.dominionhelper.ui.components.CardDetailPager
import com.marvinsuhr.dominionhelper.ui.components.CardList
import com.marvinsuhr.dominionhelper.ui.CardViewModel
import com.marvinsuhr.dominionhelper.ui.UiScreenState
import com.marvinsuhr.dominionhelper.ui.components.DrawerContent
import com.marvinsuhr.dominionhelper.ui.components.ExpansionList
import com.marvinsuhr.dominionhelper.ui.components.KingdomList
import com.marvinsuhr.dominionhelper.ui.components.SearchResultsCardList
import com.marvinsuhr.dominionhelper.ui.components.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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

            // Main content, depending on UI state
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
                UiScreenState.SHOWING_SEARCH_RESULTS -> {
                    Log.i("MainView", "Showing search results (${cardsToShow.size})")
                    SearchResultsCardList(
                        modifier = Modifier.padding(innerPadding),
                        cardList = cardsToShow,
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
