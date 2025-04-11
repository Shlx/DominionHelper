package com.example.dominionhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.launch
import androidx.compose.animation.core.copy
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dominionhelper.ui.theme.DominionHelperTheme
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

fun getSampleExpansions(): List<Expansion> {
    return listOf(
        Expansion(
            name = "Base",
            number = 1,
            imageResId = R.drawable.ic_launcher_foreground, // Replace with actual image
            gameCards = listOf(
                GameCard(
                    name = "Village",
                    expansion = GameCard.Expansion.BASE,
                    types = listOf(GameCard.Type.ACTION),
                    effects = listOf(GameCard.Effect.CARD, GameCard.Effect.ACTION),
                    cost = 3,
                    imageResId = R.drawable.ic_launcher_foreground,
                    onClick = {}
                ),
                GameCard(
                    name = "Smithy",
                    expansion = GameCard.Expansion.BASE,
                    types = listOf(GameCard.Type.ACTION),
                    effects = listOf(GameCard.Effect.CARD),
                    cost = 4,
                    imageResId = R.drawable.ic_launcher_foreground,
                    onClick = {}
                ),
                GameCard(
                    name = "Market",
                    expansion = GameCard.Expansion.BASE,
                    types = listOf(GameCard.Type.ACTION),
                    effects = listOf(
                        GameCard.Effect.CARD,
                        GameCard.Effect.ACTION,
                        GameCard.Effect.BUY,
                        GameCard.Effect.GOLD
                    ),
                    cost = 5,
                    imageResId = R.drawable.ic_launcher_foreground,
                    onClick = {}
                )
            )
        ),
        Expansion(
            name = "Intrigue",
            number = 2,
            imageResId = R.drawable.ic_launcher_foreground, // Replace with actual image
            gameCards = listOf(
                GameCard(
                    name = "Market",
                    expansion = GameCard.Expansion.BASE,
                    types = listOf(GameCard.Type.ACTION),
                    effects = listOf(
                        GameCard.Effect.CARD,
                        GameCard.Effect.ACTION,
                        GameCard.Effect.BUY,
                        GameCard.Effect.GOLD
                    ),
                    cost = 5,
                    imageResId = R.drawable.ic_launcher_foreground,
                    onClick = {}
                ),
                GameCard(
                    name = "Laboratory",
                    expansion = GameCard.Expansion.BASE,
                    types = listOf(GameCard.Type.ACTION),
                    effects = listOf(GameCard.Effect.CARD, GameCard.Effect.ACTION),
                    cost = 5,
                    imageResId = R.drawable.ic_launcher_foreground,
                    onClick = {}
                )
            )
        )
    )
}


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DominionHelperTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

// State handling and view selection
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val expansions = getSampleExpansions()
    var selectedExpansion by remember { mutableStateOf<Expansion?>(null) }
    var isSearchActive by remember { mutableStateOf(false) } // NEW: Search state
    var searchText by remember { mutableStateOf("") } // NEW: Search text

    val drawerState = rememberDrawerState(initialValue = Closed)
    val scope = rememberCoroutineScope()

    // BackHandler to manage back navigation
    BackHandler(enabled = selectedExpansion != null || isSearchActive) {
        if (isSearchActive) {
            isSearchActive = false // Close search
        } else {
            selectedExpansion = null // Go back to expansions
        }
    }

    // NEW: List of options
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
                        // Conditionally show the search field or nothing
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
                        }) { // Modified: Toggle search
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
            if (selectedExpansion == null) {
                ExpansionGrid(
                    expansions = expansions, onExpansionClick = { expansion ->
                        selectedExpansion = expansion
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                CardList(
                    cardList = selectedExpansion!!.gameCards,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

// ExpansionGrid displays the list of expansions
@Composable
fun ExpansionGrid(
    expansions: List<Expansion>,
    onExpansionClick: (Expansion) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(expansions) { expansion ->
            ExpansionView(expansion = expansion, onClick = { onExpansionClick(expansion) })
        }
    }
}

// ExpansionView displays a single expansion
@Composable
fun ExpansionView(expansion: Expansion, onClick: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) } // NEW: Checkbox state

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(150.dp)
    ) {
        Box( // NEW: Box for positioning
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = expansion.imageResId),
                contentDescription = "${expansion.name} Expansion Image",
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center) // Center the image
            )
            // Expansion name, now at bottom left
            Text(
                text = expansion.name,
                fontSize = 24.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
            // Switch at bottom right
            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(0.dp, 0.dp, 16.dp, 8.dp)
            )
        }
    }
}

// CardView displays a single card, with an image and a name
@Composable
fun CardView(card: GameCard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp, 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = card.imageResId),
                contentDescription = "${card.name} Image",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = card.name)
            }
        }
    }
}

// CardList is a composable function that creates a list of cards
@Composable
fun CardList(cardList: List<GameCard>, modifier: Modifier) {
    LazyColumn {
        items(cardList) { card ->
            CardView(card)
        }
    }
}