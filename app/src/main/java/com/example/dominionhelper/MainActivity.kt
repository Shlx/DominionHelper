package com.example.dominionhelper

import android.os.Bundle
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

class MainActivity : ComponentActivity() {

    val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    private val gameCardDao by lazy { database.gameCardDao() }
    private val expansionDao by lazy { database.expansionDao() }

    var expansions: List<Expansion> by mutableStateOf(emptyList())
    var gameCards: List<GameCard> by mutableStateOf(emptyList())

    var selectedExpansion: Int? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applicationScope.launch {
            expansions = expansionDao.getAll()
            gameCards = gameCardDao.getAll()
        }

        setContent {
            DominionHelperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(expansions, gameCards, selectedExpansion, { expansion ->
                        selectedExpansion = expansion.id
                    })
                }
            }
        }
    }
}

// State handling and view selection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    expansions: List<Expansion>,
    gameCards: List<GameCard>,
    selectedExpansion: Int?,
    onExpansionClick: (Expansion) -> Unit
) {

    var selectedExpansion by remember { mutableStateOf<Int?>(null) }
    var selectedCard by remember { mutableStateOf<GameCard?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = Closed)
    val scope = rememberCoroutineScope()

    // BackHandler:
    BackHandler(enabled = selectedExpansion != null || isSearchActive || selectedCard != null) {
        if (isSearchActive) {
            isSearchActive = false // Close search
        } else if (selectedCard != null) {
            selectedCard = null // Deselect the card
        } else {
            selectedExpansion = null // Go back to expansions
        }
    }

    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedOption by remember { mutableStateOf(options[0]) } // Initially select the first option

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                options.forEach { option ->
                    NavigationDrawerItem(
                        label = { Text(option) },
                        selected = option == selectedOption,
                        onClick = {
                            scope.launch { drawerState.close() } // Close drawer on click
                            selectedOption = option
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        },
        gesturesEnabled = drawerState.isOpen
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // Conditionally show the search field
                        if (isSearchActive) {
                            // TODO placeholder text is cut off
                            TextField(
                                value = searchText,
                                onValueChange = { searchText = it },
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
                            IconButton(onClick = { isSearchActive = false }) {
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
                            isSearchActive = !isSearchActive
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Localized description")
                        }
                        IconButton(onClick = { /* TODO Handle dice click */ }) {
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
        ) { innerPadding ->

            // View list of expansions
            if (selectedExpansion == null) {
                ExpansionGrid(
                    expansions = expansions, onExpansionClick = { expansion ->
                        selectedExpansion = expansion.id
                    },
                    modifier = Modifier.padding(innerPadding)
                )

            // View a list of cards
            } else if (selectedCard == null) {
                CardList(
                    cardList = gameCards.filter { it.expansionId == selectedExpansion }, onCardClick = { card ->
                        selectedCard = card
                    },
                    modifier = Modifier.padding(innerPadding)
                )

            // View a single card
            } else {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)) {
                    Image(
                        painter = painterResource(id = selectedCard!!.imageResId),
                        contentDescription = "Card Image",
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(onClick = { selectedCard = null }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            }
        }
    }
}