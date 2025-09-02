package com.marvinsuhr.dominionhelper

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.marvinsuhr.dominionhelper.ui.components.LibraryCardList
import com.marvinsuhr.dominionhelper.ui.components.SearchResultsCardList
import com.marvinsuhr.dominionhelper.ui.components.SettingsList
import com.marvinsuhr.dominionhelper.utils.Constants
import kotlinx.coroutines.launch

// Define all your routes (constants are good practice)
object AppDestinations {
    const val LIBRARY_ROUTE = "library"
    const val KINGDOMS_ROUTE = "kingdoms"
    const val SETTINGS_ROUTE = "settings"
    const val CARD_DETAIL_ROUTE_PREFIX = "cardDetail" // If it takes an argument
    const val CARD_DETAIL_ARG_ID = "cardId"
    const val CARD_DETAIL_ROUTE = "$CARD_DETAIL_ROUTE_PREFIX/{$CARD_DETAIL_ARG_ID}" // Example: "cardDetail/{cardId}"
    // Add other routes as needed, e.g., for expansion details if that's a separate screen
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
        screenRoute = AppDestinations.LIBRARY_ROUTE
    ),
    BottomNavItem(
        label = "Kingdoms",
        selectedIcon = Icons.Filled.Castle,
        unselectedIcon = Icons.Outlined.Castle,
        screenRoute = AppDestinations.KINGDOMS_ROUTE
    ),
    BottomNavItem(
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        screenRoute = AppDestinations.SETTINGS_ROUTE
    )
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onTitleChanged: (String) -> Unit,
    libraryViewModel: LibraryViewModel,
    kingdomViewModel: KingdomViewModel,
    settingsViewModel: SettingsViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Constants.START_DESTINATION,
        modifier = Modifier.padding(paddingValues)
    ) {
        // Library Route and its sub-states (handled within LibraryScreen or via NavHost if complex)
        composable(AppDestinations.LIBRARY_ROUTE) {
            // LibraryScreen now manages its internal UI state changes (SHOWING_EXPANSIONS, SHOWING_EXPANSION_CARDS etc.)
            // If one of these sub-states should be a distinct navigable destination,
            // you'd define separate routes for them and navigate using navController.
            // For now, let's assume LibraryScreen handles its internal states.
            LibraryScreen(
                navController = navController,
                onTitleChanged = onTitleChanged,
                viewModel = libraryViewModel
            )
        }

        // Kingdoms Route
        composable(AppDestinations.KINGDOMS_ROUTE) {
            KingdomsScreen(
                navController = navController,
                onTitleChanged = onTitleChanged,
                viewModel = kingdomViewModel
            )
        }

        // Settings Route
        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                navController = navController,
                onTitleChanged = onTitleChanged,
                viewModel = settingsViewModel
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

        // Add more composable() destinations for other screens if needed
        // e.g., an ExpansionDetailScreen, SearchResultsScreen as separate routes
    }

    // --- Handle complex sub-navigation that's not just a new screen ---
    // This is where you translate your previous libraryUiState logic into navigation if needed.
    // This is a bit advanced and might be better handled *within* LibraryScreen itself
    // or by making SHOWING_CARD_DETAIL a distinct navigable route.

    // Example: If SHOWING_CARD_DETAIL in Library should navigate to the common CardDetailScreen:
    /*LaunchedEffect(libraryUiState, libraryViewModel.selectedCard.collectAsStateWithLifecycle().value) {
        if (libraryUiState == LibraryUiState.SHOWING_CARD_DETAIL) {
            val card = libraryViewModel.selectedCard.value
            if (card != null) {
                // Check current route to avoid navigating if already there or in a loop
                if (navController.currentDestination?.route != AppDestinations.CARD_DETAIL_ROUTE.replace("{${AppDestinations.CARD_DETAIL_ARG_ID}}", card.name /* or card.id */)) {
                    navController.navigate("${AppDestinations.CARD_DETAIL_ROUTE_PREFIX}/${card.name /* or card.id */}")
                }
            }
        }
    }

    // Similar logic for KingdomViewModel if kingdomUiState.SHOWING_CARD_DETAIL should go to CardDetailScreen
    LaunchedEffect(kingdomUiState, kingdomViewModel.selectedCard.collectAsStateWithLifecycle().value) { // Assuming kingdomVM has selectedCard
        if (kingdomUiState == KingdomUiState.SHOWING_CARD_DETAIL) {
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
    viewModel: LibraryViewModel
) {

    Log.i("MainActivity", "Library Screen Content. UI State: ${viewModel.libraryUiState.collectAsState().value}")

    val title by viewModel.topBarTitle.collectAsState()
    LaunchedEffect(title) { onTitleChanged(title) }

    val libraryListState = rememberLazyListState()
    val cardListState = rememberLazyListState()

    val uiState by viewModel.libraryUiState.collectAsStateWithLifecycle()
    val expansionsWithEditions by viewModel.expansionsWithEditions.collectAsStateWithLifecycle()
    val selectedExpansion by viewModel.selectedExpansion.collectAsStateWithLifecycle()
    val selectedEdition by viewModel.selectedEdition.collectAsStateWithLifecycle()

    val cardsToShow by viewModel.cardsToShow.collectAsStateWithLifecycle()
    val selectedCard by viewModel.selectedCard.collectAsStateWithLifecycle()
    val sortType by viewModel.sortType.collectAsStateWithLifecycle()

    val searchText by viewModel.searchText.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.searchActive.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = searchText, key2 = isSearchActive) {
        if (isSearchActive) {
            viewModel.searchCards(searchText)
        }
    }

    BackHandler(enabled = uiState != LibraryUiState.SHOWING_EXPANSIONS) {
        Log.i("BackHandler", "Handle back navigation")
        viewModel.handleBackNavigation()
    }

    val applicationScope = rememberCoroutineScope()

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
        LibraryUiState.SHOWING_EXPANSION_CARDS -> {
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
        LibraryUiState.SHOWING_SEARCH_RESULTS -> {
            Log.i("MainView", "Showing search results (${cardsToShow.size})")
            SearchResultsCardList(
                cardList = cardsToShow,
                onCardClick = { viewModel.selectCard(it) },
                onToggleEnable = { viewModel.toggleCardEnabled(it) }
            )
        }

        // Show detail view of a single card
        LibraryUiState.SHOWING_CARD_DETAIL -> {
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
    viewModel: KingdomViewModel
) {
    LaunchedEffect(Unit) { onTitleChanged("Kingdoms") }

    val kingdomListState = rememberLazyListState()

    val uiState by viewModel.kingdomUiState.collectAsStateWithLifecycle()
    val kingdom by viewModel.kingdom.collectAsStateWithLifecycle()
    val playerCount by viewModel.playerCount.collectAsStateWithLifecycle()
    val isDismissEnabled by viewModel.isCardDismissalEnabled.collectAsState()
    val selectedCard by viewModel.selectedCard.collectAsStateWithLifecycle()

    Log.i("MainActivity", "Kingdom Screen Content. UI State: ${viewModel.kingdomUiState.collectAsState().value}")

    BackHandler(enabled = uiState != KingdomUiState.SHOWING_KINGDOM) {
        viewModel.handleBackNavigation()
    }

    when (uiState) {

        KingdomUiState.KINGDOM_LIST -> {
            // Generate Kingdom button
            // List of old kingdoms (db)
        }

        KingdomUiState.LOADING -> {
            //KingdomListSkeleton()
            Box(
                modifier = Modifier.clickable { viewModel.getRandomKingdom() }
            ) {
                Text("Reroll")
                Icon(imageVector = Icons.Outlined.Casino, contentDescription = "asd")
            }
        }

        // Show generated kingdom
        KingdomUiState.SHOWING_KINGDOM -> {
            Log.i(
                "MainView",
                "View card list (Random: ${kingdom.randomCards.size}, Dependent: ${kingdom.dependentCards.size}, Basic: ${kingdom.basicCards.size} cards, Landscape: ${kingdom.landscapeCards.size})"
            )
            KingdomList(
                kingdom = kingdom,
                onCardClick = { viewModel.selectCard(it) },
                selectedPlayers = playerCount,
                onPlayerCountChange = {
                    viewModel.updatePlayerCount(
                        kingdom,
                        it
                    )
                },
                listState = kingdomListState,
                isDismissEnabled = isDismissEnabled,
                onCardDismissed = { viewModel.onCardDismissed(it) },
                onRandomClick = { viewModel.getRandomKingdom() }
            )
        }

        KingdomUiState.SHOWING_CARD_DETAIL -> {
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
    viewModel: SettingsViewModel
) {

    Log.i("MainActivity", "Settings Screen Content. UI State: not implemented")

    LaunchedEffect(Unit) { onTitleChanged("Settings") }

    val settingsListState = rememberLazyListState()

    BackHandler(enabled = false) {
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsList(
        uiState.settings,
        listState = settingsListState
    )
}
