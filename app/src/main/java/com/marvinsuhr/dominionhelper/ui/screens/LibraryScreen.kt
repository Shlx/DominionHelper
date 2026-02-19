package com.marvinsuhr.dominionhelper.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.marvinsuhr.dominionhelper.ui.LibraryUiState
import com.marvinsuhr.dominionhelper.utils.calculatePadding
import com.marvinsuhr.dominionhelper.ui.LibraryViewModel
import com.marvinsuhr.dominionhelper.ui.components.CardDetailPager
import com.marvinsuhr.dominionhelper.ui.components.ExpansionList
import com.marvinsuhr.dominionhelper.ui.components.LibraryCardList
import com.marvinsuhr.dominionhelper.ui.components.SearchResultsCardList
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    onTitleChanged: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: LibraryViewModel,
    navController: NavHostController,
    innerPadding: PaddingValues
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

    val uiState by viewModel.uiState.collectAsState()
    val expansionsWithEditions by viewModel.expansionsWithEditions.collectAsState()
    val selectedExpansion by viewModel.selectedExpansion.collectAsState()
    val selectedEdition by viewModel.selectedEdition.collectAsState()

    val cardsToShow by viewModel.cardsToShow.collectAsState()
    val selectedCard by viewModel.selectedCard.collectAsState()
    val sortType by viewModel.sortType.collectAsState()

    val searchText by viewModel.searchText.collectAsState()
    val isSearchActive by viewModel.searchActive.collectAsState()

    val errorMessage by viewModel.errorMessage.collectAsState()
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
        // First, let the ViewModel handle back navigation (e.g., from card list to expansion list)
        if (!viewModel.handleBackNavigation()) {
            // If ViewModel didn't handle it, navigate at the app level
            if (navController.previousBackStackEntry != null) {
                navController.popBackStack()
            }
        }
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
                listState = libraryListState,
                paddingValues = calculatePadding(innerPadding)
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
                listState = cardListState,
                paddingValues = calculatePadding(innerPadding)
            )
        }

        // Show search results
        LibraryUiState.SEARCH_RESULTS -> {
            Log.i("MainView", "Showing search results (${cardsToShow.size})")
            SearchResultsCardList(
                cardList = cardsToShow,
                onCardClick = { viewModel.selectCard(it) },
                onToggleEnable = { viewModel.toggleCardEnabled(it) },
                listState = searchListState,
                paddingValues = calculatePadding(innerPadding)
            )
        }

        // Show detail view of a single card
        LibraryUiState.CARD_DETAIL -> {
            Log.i("MainView", "View card detail (${selectedCard?.name})")
            CardDetailPager(
                cardList = cardsToShow,
                initialCard = selectedCard!!,
                onClick = { viewModel.clearSelectedCard() },
                onPageChanged = { viewModel.selectCard(it) },
                paddingValues = calculatePadding(innerPadding)
            )
        }
    }
}
