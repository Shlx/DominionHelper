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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dominionhelper.ui.CardDetailPager
import com.example.dominionhelper.ui.CardList
import com.example.dominionhelper.ui.CardViewModel
import com.example.dominionhelper.ui.ExpansionGrid
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

// TODO: Use coil or glide or fresco to load images to avoid "image decoding logging dropped" warnings
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
// Rethink the basic Card flag. I think it's only there for the UI fix?
// -> Nope I think it makes sense for the card randomization. These cards are never pulled without meeting conditions
// Rethink color gradient on mixed cards
// Use update { in ViewModels
// Search default text is cut off
// Cards that are not in the supply vs cards that cost 0 vs cards that cost nothing??
// Explanation for card categories
// Search while viewing expansions -> only results from expansion?
// When pressing random in card view, the ui updates too fast
// Add sorting for expansions?
// Save sort type between sessions
// Remove sort by expansion in expansion view?
// Add 6* / 4+ costs (How? cost as a string in json?)
// Add ability to choose player number in generated kingdom
// Landscape cards are low res
// Warning when navigating back from generated kingdom

// You can make infinite instances of Home and Settings. Need to reload existing ones

// I think list state is shared between search / expansion and random cards (doesn't reset)
// -> Seems fine between expansion and random cards, expansion to search needs to reset
// Going back from expansion list resets even though it shouldn't

// Check image sizes
// Turn placeholders to webp

// Heirlooms should remove a copper each
// Chose 2 from Events, Landmarks, Projects, Ways, Traits

// Ability to switch expansion between first and second edition

// Problem if there is a card of the same name twice within the card lists

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
fun MainView(
    cardViewModel: CardViewModel
) {
    val expansions by cardViewModel.expansions.collectAsStateWithLifecycle()
    val selectedExpansion by cardViewModel.selectedExpansion.collectAsStateWithLifecycle()

    val cardsToShow by cardViewModel.cardsToShow.collectAsStateWithLifecycle()
    val expansionCards by cardViewModel.expansionCards.collectAsStateWithLifecycle()
    val randomCards by cardViewModel.randomCards.collectAsStateWithLifecycle()
    val basicCards by cardViewModel.basicCards.collectAsStateWithLifecycle()
    val dependentCards by cardViewModel.dependentCards.collectAsStateWithLifecycle()
    val startingCards by cardViewModel.startingCards.collectAsStateWithLifecycle()
    val selectedCard by cardViewModel.selectedCard.collectAsStateWithLifecycle()

    val isSearchActive by cardViewModel.searchActive.collectAsStateWithLifecycle()
    val searchText by cardViewModel.searchText.collectAsStateWithLifecycle()
    val sortType by cardViewModel.sortType.collectAsStateWithLifecycle()

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
            "Random Cards"
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

    var selectedOption by remember { mutableStateOf("") }
    //val options = listOf("Option 1", "Option 2", "Option 3")

    // TODO: This will lead to other parts of the app (I hope)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(applicationScope, selectedOption, { selectedOption = it }, drawerState)
        }/*,
        gesturesEnabled = drawerState.isOpen*/ // Leaving this lets the user drag the drawer open
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
                        cardViewModel.getRandomCards()
                        cardViewModel.clearSelectedExpansion()
                    },
                    onSortTypeSelected = { cardViewModel.updateSortType(it) },
                    selectedSortType = sortType,
                    topBarTitle = topBarTitle,
                    hideSearch = randomCards.isNotEmpty()
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
                        // This feels weird but maybe it's ok?
                        cardList = expansionCards + randomCards + dependentCards + basicCards + startingCards.keys.toList(),
                        initialCard = selectedCard!!,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                // Show a list of cards
                cardsToShow -> {
                    Log.i(
                        "MainView",
                        "View card list (Expansion: ${expansionCards.size}, Random: ${randomCards.size}, Dependent: ${dependentCards.size}, Basic: ${basicCards.size} cards)"
                    )

                    // Show expansion or search result
                    // Having no separation here is kind of weird I think
                    if (expansionCards.isNotEmpty()) {
                        CardList(
                            cardList = expansionCards,
                            onCardClick = { cardViewModel.selectCard(it) },
                            modifier = Modifier.padding(innerPadding),
                            listState = listState
                        )

                        // Show generated random cards
                    } else {
                        RandomCardList(
                            randomCards = randomCards,
                            basicCards = basicCards,
                            dependentCards = dependentCards,
                            startingCards = startingCards,
                            onCardClick = { cardViewModel.selectCard(it) },
                            modifier = Modifier.padding(innerPadding),
                            listState = listState
                        )
                    }
                }

                // Show all expansions in a grid
                else -> {
                    Log.i("MainView", "View expansion list")
                    ExpansionGrid(
                        expansions = expansions,
                        onExpansionClick = { expansion ->
                            cardViewModel.loadCardsByExpansion(expansion)
                            cardViewModel.selectExpansion(expansion)
                        },
                        onCheckedChange = { expansion, checked ->
                            cardViewModel.updateIsOwned(expansion, checked)
                        },
                        modifier = Modifier.padding(innerPadding),
                        gridState = gridState
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
    val screens = listOf("Home", "Settings", "Option 3")
    val context = LocalContext.current
    //val screens = DrawerScreen.values()

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        screens.forEach { option ->
            NavigationDrawerItem(
                label = { Text(option) },
                selected = option == "Home",
                onClick = {
                    scope.launch {
                        drawerState.close()
                        onOptionSelected(option)
                    }
                when (option) {
                    "Home" -> {}//Do nothing
                    "Settings" -> {
                        navigateToActivity(context, SettingsActivity::class.java)
                    }
                    "Option 3" -> {
                        //navigateToActivity(context, AboutActivity::class.java)
                    } else -> {}
                }
        },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
