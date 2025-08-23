package com.marvinsuhr.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.marvinsuhr.dominionhelper.ui.theme.DominionHelperTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marvinsuhr.dominionhelper.ui.components.CardDetailPager
import com.marvinsuhr.dominionhelper.ui.components.CardList
import com.marvinsuhr.dominionhelper.ui.LibraryViewModel
import com.marvinsuhr.dominionhelper.ui.KingdomViewModel
import com.marvinsuhr.dominionhelper.ui.LibraryUiState
import com.marvinsuhr.dominionhelper.ui.SettingsViewModel
import com.marvinsuhr.dominionhelper.ui.KingdomUiState
import com.marvinsuhr.dominionhelper.ui.components.ExpansionList
import com.marvinsuhr.dominionhelper.ui.components.KingdomList
import com.marvinsuhr.dominionhelper.ui.components.SearchResultsCardList
import com.marvinsuhr.dominionhelper.ui.components.SettingsList
import com.marvinsuhr.dominionhelper.ui.components.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screenRoute: String
)

// Items in the bottom navigation bar
val bottomNavItems = listOf(
    BottomNavItem(
        label = "Library",
        selectedIcon = Icons.Filled.LibraryBooks,
        unselectedIcon = Icons.Outlined.LibraryBooks,
        screenRoute = "library"
    ),
    BottomNavItem(
        label = "Kingdom",
        selectedIcon = Icons.Filled.Castle,
        unselectedIcon = Icons.Outlined.Castle,
        screenRoute = "kingdom"
    ),
    BottomNavItem(
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        screenRoute = "settings"
    )
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // TODO: Try to only use these in the corresponsing Screens
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val kingdomViewModel: KingdomViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        //WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DominionHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView(libraryViewModel, kingdomViewModel, settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun MainView(
    libraryViewModel: LibraryViewModel,
    kingdomViewModel: KingdomViewModel,
    settingsViewModel: SettingsViewModel
) {
    val isSearchActive by libraryViewModel.searchActive.collectAsStateWithLifecycle()
    val searchText by libraryViewModel.searchText.collectAsStateWithLifecycle()
    val sortType by libraryViewModel.sortType.collectAsStateWithLifecycle()

    val libraryUiState by libraryViewModel.libraryUiState.collectAsStateWithLifecycle()
    val errorMessage by libraryViewModel.errorMessage.collectAsStateWithLifecycle()
    val errorMessagex by kingdomViewModel.errorMessage.collectAsStateWithLifecycle()
    val topBarTitle by libraryViewModel.topBarTitle.collectAsStateWithLifecycle()

    val kingdom by kingdomViewModel.kingdom.collectAsStateWithLifecycle()

    val applicationScope = rememberCoroutineScope()

    val libraryListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val cardListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val kingdomListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val settingsListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // TODO: Center this message?
    // To display error messages
    LaunchedEffect(errorMessage, errorMessagex) {
        errorMessage?.let { message ->
            applicationScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                libraryViewModel.clearError()
            }
        }

        // TODO This is ugly. Instead, if no expansion is owned, switch to the kingdom screen but display an error there
        errorMessagex?.let { message ->
            applicationScope.launch {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
                kingdomViewModel.clearError()
            }
        }
    }

    var selectedScreenRoute by rememberSaveable { mutableStateOf(bottomNavItems[0].screenRoute) }
    var selectedBottomNavItem = bottomNavItems.first { it.screenRoute == selectedScreenRoute }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                isSearchActive = isSearchActive,
                onSearchClicked = { libraryViewModel.toggleSearch() },
                searchText = searchText,
                onSearchTextChange = { libraryViewModel.changeSearchText(it) },
                onSortTypeSelected = { kingdomViewModel.updateSortType(it, kingdom) },
                selectedSortType = sortType,
                topBarTitle = topBarTitle,
                showSearch = selectedScreenRoute == "library"
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = item.screenRoute == selectedBottomNavItem.screenRoute,
                        onClick = {

                            Log.i(
                                "NavigationBarItem",
                                "Selected ${item.label} (Previous: $selectedScreenRoute)"
                            )

                            if (selectedScreenRoute == item.screenRoute) {

                                when (item.screenRoute) {
                                    "library" -> {
                                        applicationScope.launch {
                                            if (libraryUiState == LibraryUiState.SHOWING_EXPANSIONS) {
                                                libraryListState.animateScrollToItem(0)
                                            } else if (libraryUiState == LibraryUiState.SHOWING_EXPANSION_CARDS) {
                                                cardListState.animateScrollToItem(0)
                                            }
                                        }
                                    }

                                    "kingdom" -> {
                                        kingdomViewModel.getRandomKingdom()
                                        applicationScope.launch {
                                            kingdomListState.animateScrollToItem(
                                                0
                                            )
                                        }
                                    }

                                    "settings" -> {
                                        applicationScope.launch {
                                            settingsListState.animateScrollToItem(
                                                0
                                            )
                                        }
                                    }
                                }
                            }

                            if (item.screenRoute == "kingdom" && kingdom.isEmpty()) {
                                kingdomViewModel.getRandomKingdom()
                            }

                            selectedScreenRoute = item.screenRoute
                            selectedBottomNavItem = item // Needed?
                        },
                        icon = {
                            Icon(
                                imageVector = if (item.screenRoute == selectedBottomNavItem.screenRoute) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->

        // Hoch zum anderen LaunchedEffect?
        LaunchedEffect(key1 = searchText, key2 = isSearchActive) {
            if (isSearchActive && searchText.length >= 2) {
                Log.i("LaunchedEffect", "Getting cards by search text $searchText")
                libraryViewModel.searchCards(searchText)
            }
        }

        // Main content, depending on UI state
        // TODO This switches to kingdom view before content is loaded
        when (selectedScreenRoute) {

            "library" -> {
                LibraryScreen(
                    libraryViewModel,
                    libraryUiState,
                    libraryListState,
                    cardListState,
                    innerPadding
                )
            }

            "kingdom" -> {
                KingdomScreen(kingdomViewModel, kingdomListState, innerPadding)
            }

            "settings" -> {
                SettingsScreen(settingsViewModel, settingsListState, innerPadding)
            }
        }
    }
}

// TODO Parameter order
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    uiState: LibraryUiState,
    libraryListState: LazyListState,
    cardListState: LazyListState,
    innerPadding: PaddingValues
) {
    val expansionsWithEditions by libraryViewModel.expansionsWithEditions.collectAsStateWithLifecycle()
    val selectedExpansion by libraryViewModel.selectedExpansion.collectAsStateWithLifecycle()
    val selectedEdition by libraryViewModel.selectedEdition.collectAsStateWithLifecycle()

    val cardsToShow by libraryViewModel.cardsToShow.collectAsStateWithLifecycle()
    val selectedCard by libraryViewModel.selectedCard.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {
        when (uiState) {

            LibraryUiState.SHOWING_EXPANSIONS -> {
                Log.i("BackHandler", "Leave expansion list -> Exit app")
                // Exit app
            }

            LibraryUiState.SHOWING_EXPANSION_CARDS -> {
                Log.i("BackHandler", "Leave expansion list -> Return to expansion list")
                libraryViewModel.clearSelectedExpansion()
            }

            LibraryUiState.SHOWING_SEARCH_RESULTS -> {
                Log.i("BackHandler", "Deactivate search")
                libraryViewModel.toggleSearch() // -> Deactivate search?
                libraryViewModel.changeSearchText("")
                libraryViewModel.clearAllCards()
            }

            LibraryUiState.SHOWING_CARD_DETAIL -> {
                Log.i("BackHandler", "Deselect card -> Return to card list")
                libraryViewModel.clearSelectedCard()
            }
        }
    }

    when (uiState) {

        // Show all expansions in a list
        LibraryUiState.SHOWING_EXPANSIONS -> {
            Log.i(
                "MainView",
                "View expansion list (${expansionsWithEditions.size})"
            )
            ExpansionList(
                expansions = expansionsWithEditions,
                onExpansionClick = {
                    libraryViewModel.selectExpansion(it)
                },
                onEditionClick = { libraryViewModel.selectEdition(it) },
                ownershipText = { libraryViewModel.getOwnershipText(it) },
                onOwnershipToggle = { expansion, newOwned ->
                    libraryViewModel.updateExpansionOwnership(expansion, newOwned)
                },
                onToggleExpansion = { libraryViewModel.toggleExpansion(it) },
                modifier = Modifier.padding(innerPadding),
                listState = libraryListState
            )
        }

        // Show the cards within the selected expansion
        LibraryUiState.SHOWING_EXPANSION_CARDS -> {
            Log.i(
                "MainView",
                "View card list of expansion ${selectedExpansion!!.name} (${cardsToShow.size})"
            )
            CardList(
                modifier = Modifier.padding(innerPadding),
                cardList = cardsToShow,
                includeEditionSelection = libraryViewModel.expansionHasTwoEditions(
                    selectedExpansion!!
                ),
                selectedEdition = selectedEdition,
                onEditionSelected = { editionClicked, ownedEdition ->
                    libraryViewModel.selectEdition(
                        selectedExpansion!!,
                        editionClicked,
                        ownedEdition
                    )
                },
                onCardClick = { libraryViewModel.selectCard(it) },
                onToggleEnable = { libraryViewModel.toggleCardEnabled(it) },
                listState = cardListState
            )
        }

        // Show search results
        LibraryUiState.SHOWING_SEARCH_RESULTS -> {
            Log.i("MainView", "Showing search results (${cardsToShow.size})")
            SearchResultsCardList(
                modifier = Modifier.padding(innerPadding),
                cardList = cardsToShow,
                onCardClick = { libraryViewModel.selectCard(it) },
                onToggleEnable = { libraryViewModel.toggleCardEnabled(it) }
            )
        }

        // Show detail view of a single card
        LibraryUiState.SHOWING_CARD_DETAIL -> {
            Log.i("MainView", "View card detail (${selectedCard?.name})")
            CardDetailPager(
                modifier = Modifier.padding(innerPadding),
                cardList = cardsToShow,
                initialCard = selectedCard!!,
                onClick = { libraryViewModel.clearSelectedCard() }
            )
        }
    }
}

// TODO Parameter order
@Composable
fun KingdomScreen(
    kingdomViewModel: KingdomViewModel,
    kingdomListState: LazyListState,
    innerPadding: PaddingValues
) {

    val uiState by kingdomViewModel.kingdomUiState.collectAsStateWithLifecycle()
    val kingdom by kingdomViewModel.kingdom.collectAsStateWithLifecycle()
    val playerCount by kingdomViewModel.playerCount.collectAsStateWithLifecycle()
    val isDismissEnabled by kingdomViewModel.isCardDismissalEnabled.collectAsState()
    val selectedCard by kingdomViewModel.selectedCard.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {
        when (uiState) {

            KingdomUiState.LOADING -> {

            }

            KingdomUiState.SHOWING_KINGDOM -> {
            }

            KingdomUiState.SHOWING_CARD_DETAIL -> {
                kingdomViewModel.clearSelectedCard()
            }
        }
    }

    when (uiState) {

        KingdomUiState.LOADING -> {
            //KingdomListSkeleton()
        }

        // Show generated kingdom
        KingdomUiState.SHOWING_KINGDOM -> {
            Log.i(
                "MainView",
                "View card list (Random: ${kingdom.randomCards.size}, Dependent: ${kingdom.dependentCards.size}, Basic: ${kingdom.basicCards.size} cards, Landscape: ${kingdom.landscapeCards.size})"
            )
            KingdomList(
                kingdom = kingdom,
                onCardClick = { kingdomViewModel.selectCard(it) },
                modifier = Modifier.padding(innerPadding),
                selectedPlayers = playerCount,
                onPlayerCountChange = {
                    kingdomViewModel.updatePlayerCount(
                        kingdom,
                        it
                    )
                },
                listState = kingdomListState,
                isDismissEnabled = isDismissEnabled,
                onCardDismissed = { kingdomViewModel.onCardDismissed(it) }
            )
        }

        KingdomUiState.SHOWING_CARD_DETAIL -> {
            Log.i("MainView", "View card detail (${selectedCard?.name})")
            CardDetailPager(
                modifier = Modifier.padding(innerPadding),
                cardList = kingdom.getAllCards(),
                initialCard = selectedCard!!,
                onClick = { kingdomViewModel.clearSelectedCard() }
            )
        }
    }
}

// TODO Parameter order
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    settingsListState: LazyListState,
    innerPadding: PaddingValues
) {

    BackHandler(enabled = true) {
    }

    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    SettingsList(uiState.settings, modifier = Modifier.padding(innerPadding), settingsListState)
}

