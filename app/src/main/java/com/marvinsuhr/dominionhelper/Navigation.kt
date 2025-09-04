package com.marvinsuhr.dominionhelper

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.marvinsuhr.dominionhelper.ui.KingdomUiState
import com.marvinsuhr.dominionhelper.ui.KingdomViewModel
import com.marvinsuhr.dominionhelper.ui.LibraryUiState
import com.marvinsuhr.dominionhelper.ui.LibraryViewModel
import com.marvinsuhr.dominionhelper.ui.SettingsViewModel
import com.marvinsuhr.dominionhelper.ui.components.CardDetailPager
import com.marvinsuhr.dominionhelper.ui.components.ExpansionList
import com.marvinsuhr.dominionhelper.ui.components.KingdomList
import com.marvinsuhr.dominionhelper.ui.components.KingdomList2
import com.marvinsuhr.dominionhelper.ui.components.LibraryCardList
import com.marvinsuhr.dominionhelper.ui.components.SearchResultsCardList
import com.marvinsuhr.dominionhelper.ui.components.SettingsList
import com.marvinsuhr.dominionhelper.utils.Constants
import kotlinx.coroutines.launch

sealed class CurrentScreen(val route: String) {
    object Library : CurrentScreen("library_route")
    object Kingdoms : CurrentScreen("kingdoms_route")
    object Settings : CurrentScreen("settings_route")

    companion object {
        fun fromRoute(route: String?): CurrentScreen {
            return when (route) {
                Library.route -> Library
                Kingdoms.route -> Kingdoms
                Settings.route -> Settings
                // Not possible. When starting up, currentRoute is null.
                //else -> throw IllegalArgumentException("Route $route is not recognized.")
                else -> Constants.START_DESTINATION
            }
        }
    }
}

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
        // TODO Decide on icon
        selectedIcon = Icons.Filled.Style,//Icons.Filled.WebStories,//Icons.Filled.LibraryBooks,
        unselectedIcon = Icons.Outlined.Collections,//Icons.Outlined.WebStories,//Icons.Outlined.LibraryBooks,
        screenRoute = CurrentScreen.Library.route
    ),
    BottomNavItem(
        label = "Kingdoms",
        selectedIcon = Icons.Filled.Castle,
        unselectedIcon = Icons.Outlined.Castle,
        screenRoute = CurrentScreen.Kingdoms.route
    ),
    BottomNavItem(
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        screenRoute = CurrentScreen.Settings.route
    )
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onTitleChanged: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    libraryViewModel: LibraryViewModel,
    kingdomViewModel: KingdomViewModel,
    settingsViewModel: SettingsViewModel,
    performBackNavigation: () -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = Constants.START_DESTINATION.route,
        modifier = Modifier.padding(paddingValues)
    ) {

        // Library
        composable(CurrentScreen.Library.route) {
            LibraryScreen(
                navController = navController,
                snackbarHostState = snackbarHostState,
                onTitleChanged = onTitleChanged,
                viewModel = libraryViewModel,
                performBackNavigation = performBackNavigation
            )
        }

        // Kingdoms
        composable(CurrentScreen.Kingdoms.route) {
            KingdomsScreen(
                navController = navController,
                onTitleChanged = onTitleChanged,
                snackbarHostState = snackbarHostState,
                viewModel = kingdomViewModel,
                performBackNavigation = performBackNavigation
            )
        }

        // Settings
        composable(CurrentScreen.Settings.route) {
            SettingsScreen(
                navController = navController,
                onTitleChanged = onTitleChanged,
                snackbarHostState = snackbarHostState,
                viewModel = settingsViewModel,
                performBackNavigation = performBackNavigation
            )
        }

        // Card Detail Route (Example with argument)
        /*composable(
            route = AppDestinations.CARD_DETAIL_ROUTE,
            arguments = listOf(navArgument(AppDestinations.CARD_DETAIL_ARG_ID) { type =
                NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString(AppDestinations.CARD_DETAIL_ARG_ID)
            CardDetailScreen(
                navController = navController,
                cardId = cardId,
                onTitleChanged = onTitleChanged
            )
        }*/
    }

    // --- Handle complex sub-navigation that's not just a new screen ---
    // This is where you translate your previous libraryUiState logic into navigation if needed.
    // This is a bit advanced and might be better handled *within* LibraryScreen itself
    // or by making CARD_DETAIL a distinct navigable route.

    // Example: If CARD_DETAIL in Library should navigate to the common CardDetailScreen:
    /*LaunchedEffect(libraryUiState, libraryViewModel.selectedCard.collectAsStateWithLifecycle().value) {
        if (libraryUiState == LibraryUiState.CARD_DETAIL) {
            val card = libraryViewModel.selectedCard.value
            if (card != null) {
                // Check current route to avoid navigating if already there or in a loop
                if (navController.currentDestination?.route != AppDestinations.CARD_DETAIL_ROUTE.replace("{${AppDestinations.CARD_DETAIL_ARG_ID}}", card.name /* or card.id */)) {
                    navController.navigate("${AppDestinations.CARD_DETAIL_ROUTE_PREFIX}/${card.name /* or card.id */}")
                }
            }
        }
    }

    // Similar logic for KingdomViewModel if kingdomUiState.CARD_DETAIL should go to CardDetailScreen
    LaunchedEffect(kingdomUiState, kingdomViewModel.selectedCard.collectAsStateWithLifecycle().value) { // Assuming kingdomVM has selectedCard
        if (kingdomUiState == KingdomUiState.CARD_DETAIL) {
            val card = kingdomViewModel.selectedCard.value // Adjust if card comes from elsewhere
            if (card != null) {
                if (navController.currentDestination?.route != AppDestinations.CARD_DETAIL_ROUTE.replace("{${AppDestinations.CARD_DETAIL_ARG_ID}}", card.name)) {
                    navController.navigate("${AppDestinations.CARD_DETAIL_ROUTE_PREFIX}/${card.name}")
                }
            }
        }
    }*/
}

@Composable
fun LibraryScreen(
    navController: NavHostController,
    onTitleChanged: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: LibraryViewModel,
    performBackNavigation: () -> Unit
) {

    Log.i(
        "MainActivity",
        "Library Screen Content. UI State: ${viewModel.uiState.collectAsState().value}"
    )

    val title by viewModel.topBarTitle.collectAsState()
    LaunchedEffect(title) { onTitleChanged(title) }

    val libraryListState = rememberLazyListState()
    val cardListState = rememberLazyListState()
    val searchListState = rememberLazyListState()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val expansionsWithEditions by viewModel.expansionsWithEditions.collectAsStateWithLifecycle()
    val selectedExpansion by viewModel.selectedExpansion.collectAsStateWithLifecycle()
    val selectedEdition by viewModel.selectedEdition.collectAsStateWithLifecycle()

    val cardsToShow by viewModel.cardsToShow.collectAsStateWithLifecycle()
    val selectedCard by viewModel.selectedCard.collectAsStateWithLifecycle()
    val sortType by viewModel.sortType.collectAsStateWithLifecycle()

    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.searchActive.collectAsStateWithLifecycle()

    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.scrollToTopEvent.collect {
            when (uiState) {
                LibraryUiState.EXPANSION_CARDS -> cardListState.animateScrollToItem(0)
                LibraryUiState.SEARCH_RESULTS -> searchListState.animateScrollToItem(0)
                LibraryUiState.EXPANSIONS -> libraryListState.animateScrollToItem(0)
                else -> {}
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(key1 = searchText, key2 = isSearchActive) {
        if (isSearchActive) {
            viewModel.searchCards(searchText)
        }
    }

    BackHandler {
        performBackNavigation()
    }

    val applicationScope = rememberCoroutineScope()

    when (uiState) {

        // Show all expansions in a list
        LibraryUiState.EXPANSIONS -> {
            Log.i(
                "MainView",
                "View expansion list (${expansionsWithEditions.size})"
            )
            ExpansionList(
                expansions = expansionsWithEditions,
                onExpansionClick = {
                    viewModel.selectExpansion(it)
                    applicationScope.launch {
                        cardListState.scrollToItem(0)
                    }
                },
                onEditionClick = { viewModel.selectEdition(it) },
                ownershipText = { viewModel.getOwnershipText(it) },
                onOwnershipToggle = { expansion, newOwned ->
                    viewModel.updateExpansionOwnership(expansion, newOwned)
                },
                onToggleExpansion = { viewModel.toggleExpansion(it) },
                listState = libraryListState
            )
        }

        // Show the cards within the selected expansion
        LibraryUiState.EXPANSION_CARDS -> {
            Log.i(
                "MainView",
                "View card list of expansion ${selectedExpansion!!.name} (${cardsToShow.size})"
            )
            LibraryCardList(
                cardList = cardsToShow,
                sortType = sortType,
                includeEditionSelection = viewModel.expansionHasTwoEditions(
                    selectedExpansion!!
                ),
                selectedEdition = selectedEdition,
                onEditionSelected = { editionClicked, ownedEdition ->
                    viewModel.selectEdition(
                        selectedExpansion!!,
                        editionClicked,
                        ownedEdition
                    )
                },
                onCardClick = { viewModel.selectCard(it) },
                onToggleEnable = { viewModel.toggleCardEnabled(it) },
                listState = cardListState
            )
        }

        // Show search results
        LibraryUiState.SEARCH_RESULTS -> {
            Log.i("MainView", "Showing search results (${cardsToShow.size})")
            SearchResultsCardList(
                cardList = cardsToShow,
                onCardClick = { viewModel.selectCard(it) },
                onToggleEnable = { viewModel.toggleCardEnabled(it) },
                listState = searchListState
            )
        }

        // Show detail view of a single card
        LibraryUiState.CARD_DETAIL -> {
            Log.i("MainView", "View card detail (${selectedCard?.name})")
            CardDetailPager(
                cardList = cardsToShow,
                initialCard = selectedCard!!,
                onClick = { viewModel.clearSelectedCard() },
                onPageChanged = { viewModel.selectCard(it) }
            )
        }
    }
}

@Composable
fun KingdomsScreen(
    navController: NavHostController,
    onTitleChanged: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: KingdomViewModel,
    performBackNavigation: () -> Unit
) {
    LaunchedEffect(Unit) { onTitleChanged("Kingdoms") }

    val kingdomListState = rememberLazyListState()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val kingdom by viewModel.kingdom.collectAsStateWithLifecycle()
    val playerCount by viewModel.playerCount.collectAsStateWithLifecycle()
    val isDismissEnabled by viewModel.isCardDismissalEnabled.collectAsState()
    val selectedCard by viewModel.selectedCard.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val allKingdoms by viewModel.allKingdoms.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()

    Log.i(
        "MainActivity",
        "Kingdom Screen Content. UI State: ${viewModel.uiState.collectAsState().value}"
    )

    LaunchedEffect(Unit) {
        viewModel.scrollToTopEvent.collect {
            kingdomListState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
                viewModel.clearError()
            }
        }
    }

    BackHandler {
        performBackNavigation()
    }

    when (uiState) {

        KingdomUiState.KINGDOM_LIST -> {

            KingdomList2(
                kingdomList = allKingdoms,
                onGenerateKingdom = { viewModel.getRandomKingdom() },
                onKingdomClicked = { viewModel.selectKingdom(it) },
                onDeleteClick = { viewModel.deleteKingdom(it.uuid) },
                onFavoriteClick = { viewModel.toggleFavorite(it) },
                onKingdomNameChange = { uuid, newName -> viewModel.updateKingdomName(uuid, newName) }
            )
        }

        KingdomUiState.LOADING -> {
            //KingdomListSkeleton()#
        }

        // Show generated kingdom
        KingdomUiState.SINGLE_KINGDOM -> {
            Log.i(
                "MainView",
                "View card list (Random: ${kingdom.randomCards.size}, Dependent: ${kingdom.dependentCards.size}, Basic: ${kingdom.basicCards.size} cards, Landscape: ${kingdom.landscapeCards.size})"
            )
            KingdomList(
                kingdom = kingdom,
                onCardClick = { viewModel.selectCard(it) },
                selectedPlayers = playerCount,
                onPlayerCountChange = {
                    viewModel.userChangedPlayerCount(it)
                },
                listState = kingdomListState,
                isDismissEnabled = isDismissEnabled,
                onCardDismissed = { viewModel.onCardDismissed(it) },
                onRandomClick = { viewModel.getRandomKingdom() }
            )
        }

        KingdomUiState.CARD_DETAIL -> {
            Log.i("MainView", "View card detail (${selectedCard?.name})")
            CardDetailPager(
                cardList = kingdom.getAllCards(),
                initialCard = selectedCard!!,
                onClick = { viewModel.clearSelectedCard() },
                onPageChanged = { viewModel.selectCard(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    onTitleChanged: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: SettingsViewModel,
    performBackNavigation: () -> Unit
) {

    Log.i("MainActivity", "Settings Screen Content. UI State: not implemented")

    LaunchedEffect(Unit) { onTitleChanged("Settings") }

    val settingsListState = rememberLazyListState()

    /*LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            applicationScope.launch {
                snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
                viewModel.clearError()
            }
        }
    }*/

    LaunchedEffect(Unit) {
        viewModel.scrollToTopEvent.collect {
            settingsListState.animateScrollToItem(0)
        }
    }

    BackHandler {
        performBackNavigation()
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsList(
        uiState.settings,
        listState = settingsListState
    )
}
