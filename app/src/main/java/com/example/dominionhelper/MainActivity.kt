package com.example.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.dominionhelper.ui.theme.DominionHelperTheme
import androidx.compose.material3.DrawerValue.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dominionhelper.data.Card
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.Expansion
import com.example.dominionhelper.data.ExpansionDao
import com.example.dominionhelper.ui.CardDetailPager
import com.example.dominionhelper.ui.CardList
import com.example.dominionhelper.ui.CardViewModel
import com.example.dominionhelper.ui.ExpansionGrid
import com.example.dominionhelper.ui.ExpansionViewModel
import com.example.dominionhelper.ui.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

// Random info:
// Prop drilling: passing lots of data down through mutliple levels (bad)
// by lazy: loading data only when needed (good)
// Flows: automatically updates UI elements when data changes
// mutableStateOf automatically updates UI elements reliant on the values when they change

// Composables vs. ViewModels
// TODO: Use coil or glide to load images to avoid "image decoding logging dropped" warnings
// Applicationscope vs LifecycleScope vs CoroutineScope vs whatever
// Flows instead of lists?

// TODO
// Split piles
// Show behind top + nav bar?
// Add cost to cards
// Remove base cards from randomization
// Research landscape rules (I think 2 are recommended)
// FIX DRAWER
// Add rules for randomization
// VP counter
// Clear search on deactivating search
// Find solution for 1st / 2nd edition
// Add loading times instead of switching instantly (you can see UI changing)
// Remove search from detail view?
// First launch: No data shown
// ViewModels

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var applicationScope: CoroutineScope

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

    // State from ExpansionViewModel
    val expansions by expansionViewModel.expansions.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = Closed)
    val applicationScope = rememberCoroutineScope()

    // BackHandler:
    BackHandler(enabled = selectedExpansion != null || drawerState.isOpen || isSearchActive) {
        when {
            drawerState.isOpen -> applicationScope.launch {
                Log.i("Back Handler", "Close drawer")
                drawerState.close()
            }

            isSearchActive -> {
                Log.i("Back Handler", "Deactivate search")
                cardViewModel.toggleSearch()
                cardViewModel.changeSearchText("")
            }

            selectedCard != null -> {
                Log.i("Back Handler", "Deselect card -> Return to card list")
                cardViewModel.clearSelectedCard()
            }

            selectedExpansion != null -> {
                Log.i("Back Handler", "Deselect expansion -> Return to expansion list")
                expansionViewModel.clearSelectedExpansion()
            }
        }
    }

    var selectedOption by remember { mutableStateOf("") }
    val options = listOf("Option 1", "Option 2", "Option 3")

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
                    selectedExpansion = selectedExpansion
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
                selectedExpansion == null && searchText.length <= 1 && !showRandomCards -> {
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                selectedCard == null -> {
                    Log.i("MainActivity", "View card list (${cards.size} cards)")
                    CardList(
                        cardList = cards,
                        onCardClick = { card -> cardViewModel.selectCard(card) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                else -> {
                    Log.i("MainActivity", "View card detail (${selectedCard?.name})")
                    CardDetailPager(
                        cardList = cards,
                        initialCard = selectedCard!!,
                        onBackClick = { cardViewModel.clearSelectedCard() },
                        modifier = Modifier.padding(innerPadding)
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

