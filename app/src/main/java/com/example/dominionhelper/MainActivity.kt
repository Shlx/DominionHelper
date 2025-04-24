package com.example.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dominionhelper.ui.theme.DominionHelperTheme
import androidx.compose.material3.DrawerValue.*
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dominionhelper.ui.CardDetailPager
import com.example.dominionhelper.ui.CardList
import com.example.dominionhelper.ui.CardViewModel
import com.example.dominionhelper.ui.ExpansionGrid
import com.example.dominionhelper.ui.ExpansionViewModel
import com.example.dominionhelper.ui.RandomCardList
import com.example.dominionhelper.ui.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Random info:
// Prop drilling: passing lots of data down through mutliple levels (bad)
// by lazy: loading data only when needed (good)
// Flows: automatically updates UI elements when data changes
// mutableStateOf automatically updates UI elements reliant on the values when they change

// TODO: Use coil or glide to load images to avoid "image decoding logging dropped" warnings
// Applicationscope vs LifecycleScope vs CoroutineScope vs whatever
// Flows instead of lists?

// TODO
// Split piles
// Show behind top + nav bar?
// Research landscape rules (I think 2 are recommended)
// Add rules for randomization
// VP counter
// Find solution for 1st / 2nd edition
// Add loading times instead of switching instantly (you can see UI changing)
// Remove search from detail view?
// First launch: No data shown
// Close keyboard when scrolling on search results
// Landscape detail view
// Rethink the basic Card flag. I think it's only there for the UI fix?
// -> Nope I think it makes sense for the card randomization. These cards are never pulled without meeting conditions
// Rethink color gradient on mixed cards
// Use update { in ViewModels
// Search default text is cut off
// Cards that are not in the supply vs cards that cost 0 vs cards that cost nothing??
// Explanation for card categories

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val expansionViewModel: ExpansionViewModel by viewModels()
    private val cardViewModel: CardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DominionHelperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView(expansionViewModel, cardViewModel)
                }
            }
        }
    }
}

@Composable
fun MainView(expansionViewModel: ExpansionViewModel,
             cardViewModel: CardViewModel) {

    val selectedExpansion by expansionViewModel.selectedExpansion.collectAsStateWithLifecycle()
    val selectedCard by cardViewModel.selectedCard.collectAsStateWithLifecycle()
    val isSearchActive by cardViewModel.searchActive.collectAsStateWithLifecycle()
    val searchText by cardViewModel.searchText.collectAsStateWithLifecycle()
    val showRandomCards by cardViewModel.showRandomCards.collectAsStateWithLifecycle()
    val cards by cardViewModel.cards.collectAsStateWithLifecycle()
    val sortType by cardViewModel.sortType.collectAsStateWithLifecycle()

    val randomCards by cardViewModel.randomCards.collectAsStateWithLifecycle()
    val basicCards by cardViewModel.basicCards.collectAsStateWithLifecycle()
    val dependentCards by cardViewModel.dependentCards.collectAsStateWithLifecycle()

    val cardsToShow by cardViewModel.cardsToShow.collectAsStateWithLifecycle()

    // State from ExpansionViewModel
    val expansions by expansionViewModel.expansions.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = Closed)
    val applicationScope = rememberCoroutineScope()

    var topBarTitle by remember { mutableStateOf("Dominion Helper") }
    val listState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    val gridState = rememberSaveable(saver = LazyGridState.Saver) {
        LazyGridState()
    }

    LaunchedEffect(key1 = showRandomCards, key2 = selectedExpansion) {
        topBarTitle = if (showRandomCards) {
            "Random Cards"
        } else if (selectedExpansion != null) {
            selectedExpansion!!.name
        } else {
            "Dominion Helper"
        }
    }

    // BackHandler:
    BackHandler(enabled = selectedExpansion != null || drawerState.isOpen || isSearchActive || showRandomCards) {
        when {
            drawerState.isOpen -> applicationScope.launch {
                Log.i("BackHandler", "Close drawer")
                drawerState.close()
            }

            selectedCard != null -> {
                Log.i("BackHandler", "Deselect card -> Return to card list")
                cardViewModel.clearSelectedCard()
            }

            cardsToShow -> {
                // Go back to expansion view
                Log.i("BackHandler", "Leave random cards -> Return to expansion list")
                cardViewModel.clearRandomCards()
                cardViewModel.clearCards()
                expansionViewModel.clearSelectedExpansion()
                applicationScope.launch {
                    listState.scrollToItem(0)
                }
            }

            isSearchActive -> {
                Log.i("BackHandler", "Deactivate search")
                cardViewModel.toggleSearch()
                cardViewModel.changeSearchText("")
                cardViewModel.clearCards()
            }

            selectedExpansion != null -> {
                Log.i("BackHandler", "Deselect expansion -> Return to expansion list")
                expansionViewModel.clearSelectedExpansion()
                cardViewModel.clearCards()
            }
        }
    }

    var selectedOption by remember { mutableStateOf("") }
    //val options = listOf("Option 1", "Option 2", "Option 3")

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(applicationScope, selectedOption, { selectedOption = it }, drawerState)
        },
        gesturesEnabled = drawerState.isOpen
    ) {

        Scaffold(
            topBar = {
                TopBar(
                    applicationScope,
                    drawerState,
                    isSearchActive,
                    { cardViewModel.toggleSearch() },
                    searchText,
                    { cardViewModel.changeSearchText(it) },
                    onRandomCardsClicked = {
                        cardViewModel.setRandomCards()
                    },
                    onSortTypeSelected = { sortType ->
                        cardViewModel.updateSortType(sortType)
                    },
                    selectedSortType = sortType,
                    topBarTitle = topBarTitle
                )
            }
        ) { innerPadding ->

            // TODO: Option to order by cost / type / alphabetical
            LaunchedEffect(key1 = searchText, key2 = isSearchActive) {
                if (isSearchActive && searchText.length >= 2) {
                    Log.i("LaunchedEffect", "Getting cards by search text ${searchText}")
                    cardViewModel.searchCards(searchText)
                }
            }

            when {

                // Detail view
                selectedCard != null -> {
                    Log.i("MainActivity", "View card detail (${selectedCard?.name})")
                    CardDetailPager(
                        cardList = if (showRandomCards) randomCards + dependentCards + basicCards else cards,
                        initialCard = selectedCard!!,
                        onBackClick = { cardViewModel.clearSelectedCard() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                // List of cards in selected expansion
                // Includes random cards and search cards?
                cardsToShow -> {
                    Log.i("MainActivity", "View card list (${cards.size} cards)")

                    if (!showRandomCards) {
                        CardList(
                            cardList = cards,
                            onCardClick = { card -> cardViewModel.selectCard(card) },
                            modifier = Modifier.padding(innerPadding),
                            listState = listState
                        )
                    } else {
                        Log.i("MainActivity", "View random cards")
                        RandomCardList(
                            randomCards = randomCards,
                            basicCards = basicCards,
                            dependentCards = dependentCards,
                            onCardClick = { cardViewModel.selectCard(it) },
                            modifier = Modifier.padding(innerPadding),
                            listState = listState
                        )
                        }
                }

                // All expansions in grid
                else -> {
                    Log.i("MainActivity", "View expansion list")
                    ExpansionGrid(
                        expansions = expansions,
                        onExpansionClick = { expansion ->
                            Log.i(
                                "MainActivity",
                                "Getting cards from expansion ${expansion.name}"
                            )
                            cardViewModel.loadCardsByExpansion(expansion.set)
                            expansionViewModel.selectExpansion(expansion)
                        },
                        modifier = Modifier.padding(innerPadding),
                        gridState
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    scope: CoroutineScope,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    drawerState: DrawerState
) {
    val options = listOf("Option 1", "Option 2", "Option 3")

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        options.forEach { option ->
            NavigationDrawerItem(
                label = { Text(option) },
                selected = option == selectedOption,
                onClick = {
                    scope.launch {
                        drawerState.close()
                    }
                    onOptionSelected(option)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

