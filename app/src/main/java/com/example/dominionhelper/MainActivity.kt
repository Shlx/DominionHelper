package com.example.dominionhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/*fun getSampleExpansions(): List<Expansion> {
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
                    imageResId = R.drawable.village,
                ),
                GameCard(
                    name = "Gardens",
                    expansion = GameCard.Expansion.BASE,
                    types = listOf(GameCard.Type.VICTORY),
                    effects = listOf(),
                    cost = 4,
                    imageResId = R.drawable.gardens,
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
                    imageResId = R.drawable.market,
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
                ),
                GameCard(
                    name = "Laboratory",
                    expansion = GameCard.Expansion.BASE,
                    types = listOf(GameCard.Type.ACTION),
                    effects = listOf(GameCard.Effect.CARD, GameCard.Effect.ACTION),
                    cost = 6,
                    imageResId = R.drawable.ic_launcher_foreground,
                )
            )
        )
    )
}*/

class MainActivity : ComponentActivity() {

    val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    private val gameCardDao by lazy { database.gameCardDao() }
    private val expansionDao by lazy { database.expansionDao() }

    var gameCards: List<GameCard> by mutableStateOf(emptyList())
    var expansions: List<Expansion> by mutableStateOf(emptyList())
    var selectedExpansion: Int? by mutableStateOf(null)
    var selectedCard: GameCard? by mutableStateOf(null)

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

// CardList is a composable function that creates a list of cards
@Composable
fun CardList(cardList: List<GameCard>, modifier: Modifier, onCardClick: (GameCard) -> Unit) {
    LazyColumn(
        modifier = modifier
    ) {
        items(cardList) { card ->
            CardView(card, onClick = { onCardClick(card) })
        }
    }
}

// CardView displays a single card, with an image and a name
@Composable
fun CardView(card: GameCard, onClick: () -> Unit) {
    val topCropPercentage = 0.10f // 10%
    val bottomCropPercentage = 0.50f // 50%
    val visibleHeightPercentage = 1f - topCropPercentage - bottomCropPercentage
    val imageWidth = 80.dp // Set the image width.
    val cardImageHeight = 100f//imageWidth / visibleHeightPercentage //calculate the card image height using the percentages.

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp, 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(50.dp),//cardImageHeight), // <--- Set the Row's height here
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box( //NEW: use Box to display the image
                modifier = Modifier
                    .weight(0.3f) // Take up 30% of the available width
            ) {
                Image(
                    painter = painterResource(id = card.imageResId),
                    contentDescription = card.name,
                    modifier = Modifier
                        .width(imageWidth)
                        .fillMaxHeight()
                        .clip(
                            object : Shape {
                                override fun createOutline(
                                    size: androidx.compose.ui.geometry.Size,
                                    layoutDirection: LayoutDirection,
                                    density: Density
                                ): Outline {
                                    val path = Path().apply {
                                        val topCropAmount = size.height * topCropPercentage
                                        val bottomCropAmount = size.height * bottomCropPercentage
                                        val rect = Rect(
                                            0f,
                                            topCropAmount,
                                            size.width,
                                            size.height - bottomCropAmount
                                        )
                                        addRect(rect)
                                    }
                                    return Outline.Generic(path)
                                }
                            }
                        ),
                    contentScale = ContentScale.Crop // Crop to maintain aspect ratio
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(0.7f) // Take up 70% of the available width
                    .height(50.dp)//cardImageHeight) // <--- Set the column's height here
            ) {
                Text(text = card.name,
                    modifier = Modifier.height(IntrinsicSize.Min)) // <--- set the text height here
            }
        }
    }
}