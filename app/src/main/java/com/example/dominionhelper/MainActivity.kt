package com.example.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dominionhelper.data.Card
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.Expansion
import com.example.dominionhelper.data.ExpansionDao
import com.example.dominionhelper.ui.CardDetailPager
import com.example.dominionhelper.ui.CardList
import com.example.dominionhelper.ui.ExpansionGrid
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
    lateinit var cardDao: CardDao

    @Inject
    lateinit var expansionDao: ExpansionDao

    @Inject
    lateinit var applicationScope: CoroutineScope

    var expansions: List<Expansion> by mutableStateOf(emptyList())
    var cards: List<Card> by mutableStateOf(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applicationScope.launch {
            expansions = expansionDao.getAll()
            Log.d("MainActivity", "Loaded ${expansions.size} expansions")
            cards = cardDao.getAll()
            Log.d("MainActivity", "Loaded ${cards.size} cards")
        }

        setContent {
            DominionHelperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    var selectedExpansion by remember { mutableStateOf<Expansion?>(null) }
                    var selectedCard by remember { mutableStateOf<Card?>(null) }
                    var isSearchActive by remember { mutableStateOf(false) }
                    var searchText by remember { mutableStateOf("") }

                    val drawerState = rememberDrawerState(initialValue = Closed)
                    var isLoading by remember {mutableStateOf(false)}
                    var showRandomCards by remember {mutableStateOf(false)}

                    // BackHandler:
                    BackHandler(enabled = selectedExpansion != null || drawerState.isOpen || isSearchActive) {
                        if (drawerState.isOpen) {
                            applicationScope.launch {
                                Log.i("Back Handler", "Close drawer")
                                drawerState.close()
                            }

                        } else if (isSearchActive) {
                            Log.i("Back Handler", "Deactivate search")
                            isSearchActive = false

                        } else if (selectedCard != null) {
                            Log.i("Back Handler", "Deselect card -> Return to card list")
                            selectedCard = null

                        } else if (selectedExpansion != null) {
                            Log.i("Back Handler", "Deselect expansion -> Return to expansion list")
                            selectedExpansion = null
                        }
                    }

                    var selectedOption by remember { mutableStateOf("") } // Initially select the first option
                    val options = listOf("Option 1", "Option 2", "Option 3")

                    // Currently broken - java.lang.IllegalStateException: A MonotonicFrameClock is not available in this CoroutineContext. Callers should supply an appropriate MonotonicFrameClock using withContext.
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
                                    { isSearchActive = !isSearchActive },
                                    searchText,
                                    { searchText = it },
                                    onRandomCardsClicked = {
                                        // Get ApplicationContext scope here?
                                        applicationScope.launch {

                                            //isLoading = true // Probably not needed?
                                            // Error if no expansions selected
                                            cards = cardDao.getRandomCardsFromOwnedExpansions(10)
                                            Log.i("Random cards", "Generated ${cards.size} cards")
                                            //isLoading = false
                                            showRandomCards = true
                                        }
                                    },
                                    selectedExpansion = selectedExpansion
                                )
                            }
                        ) { innerPadding ->

                            // TODO: Option to order by cost / type / alphabetical
                            LaunchedEffect(key1 = searchText, key2 = isSearchActive) {
                                if (isSearchActive && searchText.length >= 2) {
                                    Log.i("LaunchedEffect", "Getting cards by search text ${searchText}")
                                    cards = cardDao.getFilteredCards("%$searchText%")
                                }/* else if (selectedExpansion != null) {
                                    Log.i("LaunchedEffect", "Getting cards from expansion ${selectedExpansion!!.name}")
                                    gameCards = cardDao.getCardsByExpansion(selectedExpansion!!.set)
                                }*/
                            }

                            // View list of expansions
                            // TODO: Geht hier rein on randomized cards
                            // -> Neue activity für card list?
                            if (selectedExpansion == null && searchText.length <= 1 && !showRandomCards) {
                                Log.i("MainActivity", "View expansion list")
                                ExpansionGrid(
                                    expansions = expansions,
                                    expansionDao = expansionDao,
                                    onExpansionClick = { expansion ->
                                        selectedExpansion = expansion
                                        applicationScope.launch {
                                            Log.i("MainActivity", "Getting cards from expansion ${selectedExpansion!!.name}")
                                            cards = cardDao.getCardsByExpansion(expansion.set)
                                        }
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )

                                // View a list of cards
                            } else if (selectedCard == null) {
                                Log.i("MainActivity", "View card list (${cards.size} cards)")
                                CardList(
                                    cardList = cards,
                                    onCardClick = { card ->
                                        selectedCard = card
                                    },
                                    expansionDao = expansionDao,
                                    modifier = Modifier.padding(innerPadding)
                                )

                                // View a single card
                            } else if (!isLoading){
                                Log.i("MainActivity", "View card detail (${selectedCard?.name})")
                                CardDetailPager(
                                    cardList = cards,
                                    initialCard = selectedCard!!,
                                    onBackClick = { selectedCard = null },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            } else {
                                CircularProgressIndicator()
                            }
                        }
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scope: CoroutineScope,
    drawerState: DrawerState,
    isSearchActive: Boolean,
    onSearchClicked: () -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onRandomCardsClicked: () -> Unit,
    selectedExpansion: Expansion? = null
) {
    TopAppBar(
        title = {
            // Conditionally show the search field
            if (isSearchActive) {
                // TODO placeholder text is cut off
                TextField(
                    value = searchText,
                    onValueChange = { onSearchTextChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp), // Add padding here
                    placeholder = {
                        CompositionLocalProvider(
                            LocalTextStyle provides LocalTextStyle.current.copy(
                                color = Color.Gray.copy(alpha = 0.5f),
                                lineHeight = 26.sp
                            )
                        ) {
                            Text("Search")
                        }
                    },
                    textStyle = TextStyle(lineHeight = 26.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.0f), // Completely transparent
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.0f), // Completely transparent
                        disabledContainerColor = Color.Black.copy(alpha = 0.0f) // Completely transparent
                    )
                )
            } else if (selectedExpansion != null) {
                Text(selectedExpansion.name) // Display expansion name
            } else {
                Text("Dominion Helper")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            // Conditionally show the back button or hamburger menu
            if (isSearchActive) {
                IconButton(onClick = { onSearchClicked() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            } else {
                IconButton(onClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Localized description"
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                onSearchClicked()
            }) {
                Icon(Icons.Filled.Search, contentDescription = "Localized description")
            }
            IconButton(onClick = {
                onRandomCardsClicked()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Localized description",
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { /* TODO Handle more options click */ }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Localized description"
                )
            }
        }
    )
}
