package com.example.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// Prop drilling: passing lots of data down through mutliple levels (bad)
// by lazy: loading data only when needed (good)
// Composables vs. ViewModels
// Application class: global state holder and entry point, managing singletons, Timber / Crashlytics / Firebase
// Move dao etc there? -> yea
// Can I easily call Application functions from within activities? -> getApplication() (val myApplication = application as MyApplication)
// Even better: Use dependency injection to call DAOs TODO later on

class MainActivity : ComponentActivity() {

    // !! mutableStateOf automatically updates UI elements reliant on the values when they change
    var expansions: List<Expansion> by mutableStateOf(emptyList())
    var gameCards: List<GameCard> by mutableStateOf(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dominionHelper = application as DominionHelper
        val gameCardDao = dominionHelper.gameCardDao
        val expansionDao = dominionHelper.expansionDao
        val scope = dominionHelper.applicationScope

        scope.launch { // vs lifecyclescope?
            expansions = expansionDao.getAll() // TODO: Flow?
            gameCards = gameCardDao.getAll() // TODO: Get those when an expansion is clicked
        }

        setContent {
            DominionHelperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    var selectedExpansion by remember { mutableStateOf<Int?>(null) }
                    var selectedCard by remember { mutableStateOf<GameCard?>(null) }
                    var isSearchActive by remember { mutableStateOf(false) }
                    var searchText by remember { mutableStateOf("") }

                    val drawerState = rememberDrawerState(initialValue = Closed)
                    val scope = rememberCoroutineScope()

                    // BackHandler:
                    BackHandler(enabled = selectedExpansion != null || isSearchActive || selectedCard != null || drawerState.isOpen) {
                        if (drawerState.isOpen) {
                            scope.launch {
                                drawerState.close() // Close drawer
                            }
                        } else if (isSearchActive) {
                            isSearchActive = false // Close search
                        } else if (selectedCard != null) {
                            selectedCard = null // Deselect the card
                        } else {
                            selectedExpansion = null // Go back to expansions
                        }
                    }

                    var selectedOption by remember { mutableStateOf("") } // Initially select the first option

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            DrawerContent(selectedOption, { selectedOption = it }, drawerState)
                        },
                        gesturesEnabled = drawerState.isOpen
                    ) {

                        Scaffold(
                            topBar = {
                                TopBar(
                                    scope,
                                    drawerState,
                                    isSearchActive,
                                    { isSearchActive = !isSearchActive },
                                    searchText,
                                    { searchText = it },
                                    onRandomCardsClicked = {
                                        scope.launch {
                                            //gameCards = gameCards.shuffled().take(5)
                                            gameCards = gameCardDao.getRandomCards(3)
                                            Log.i("Random cards", ""+gameCards.size)
                                            // TODO: This shows a random amount of cards.
                                            // -> Callback function once the call is definitely complete
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->

                            LaunchedEffect(key1 = searchText, key2 = isSearchActive) {
                                if (isSearchActive && searchText.length >= 2) {
                                    gameCards = gameCardDao.getFilteredCards("%$searchText%")
                                } else {
                                    gameCards = gameCardDao.getAll()
                                }
                            }

                            // View list of expansions
                            if (selectedExpansion == null && searchText.length <= 1) {
                                Log.i("Grid", "view expansion list")
                                ExpansionGrid(
                                    expansions = expansions, onExpansionClick = { expansion ->
                                        selectedExpansion = expansion.id
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )

                                // View a list of cards
                            } else if (selectedCard == null) {
                                if (!isSearchActive) {
                                    gameCards = gameCards.filter { it.expansionId == selectedExpansion }
                                }
                                Log.i("Grid", "view card list")
                                CardList(
                                    cardList = gameCards,
                                    onCardClick = { card ->
                                        selectedCard = card
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )

                                // View a single card
                            } else {
                                Log.i("Grid", "view card detail")
                                CardDetail(
                                    card = selectedCard!!,
                                    onBackClick = { selectedCard = null },
                                    modifier = Modifier.padding(innerPadding)
                                )
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
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    drawerState: DrawerState
) {
    val scope = rememberCoroutineScope()
    val options = listOf("Option 1", "Option 2", "Option 3")

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        options.forEach { option ->
            NavigationDrawerItem(
                label = { Text(option) },
                selected = option == selectedOption,
                onClick = {
                    scope.launch { drawerState.close() } // Close drawer on click
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
    onRandomCardsClicked: () -> Unit
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
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
