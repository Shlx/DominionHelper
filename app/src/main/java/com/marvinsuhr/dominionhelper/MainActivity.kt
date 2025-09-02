package com.marvinsuhr.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.marvinsuhr.dominionhelper.ui.theme.DominionHelperTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marvinsuhr.dominionhelper.ui.LibraryViewModel
import com.marvinsuhr.dominionhelper.ui.KingdomViewModel
import com.marvinsuhr.dominionhelper.ui.LibraryUiState
import com.marvinsuhr.dominionhelper.ui.SettingsViewModel
import com.marvinsuhr.dominionhelper.ui.KingdomUiState
import com.marvinsuhr.dominionhelper.ui.SortType
import com.marvinsuhr.dominionhelper.ui.components.TopBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val libraryViewModel: LibraryViewModel by viewModels()
    private val kingdomViewModel: KingdomViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        //WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DominionHelperTheme {
                val navController = rememberNavController()

                val snackbarHostState = remember { SnackbarHostState() }
                val topAppBarState = rememberTopAppBarState()
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                var currentTopBarTitle by rememberSaveable { mutableStateOf("Library") }
                val onTitleChangedLambda = { newTitle: String ->
                    currentTopBarTitle = newTitle
                }

                val libraryUiState by libraryViewModel.libraryUiState.collectAsStateWithLifecycle()
                val kingdomUiState by kingdomViewModel.kingdomUiState.collectAsStateWithLifecycle()
                val isSearchActive by libraryViewModel.searchActive.collectAsStateWithLifecycle()

                val applicationScope = rememberCoroutineScope()

                val showBackButton = remember(currentRoute, libraryUiState, kingdomUiState, isSearchActive) {
                    when (currentRoute) {
                        AppDestinations.LIBRARY_ROUTE -> {
                            libraryUiState == LibraryUiState.SHOWING_CARD_DETAIL ||
                                    libraryUiState == LibraryUiState.SHOWING_EXPANSION_CARDS ||
                                    (libraryUiState == LibraryUiState.SHOWING_SEARCH_RESULTS)// && isSearchActive)
                        }
                        AppDestinations.KINGDOMS_ROUTE -> {
                            kingdomUiState == KingdomUiState.SHOWING_CARD_DETAIL
                        }
                        AppDestinations.SETTINGS_ROUTE -> false
                        else -> true
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopBar(
                            title = currentTopBarTitle,
                            showBackButton = showBackButton,
                            onBackButtonClicked = {
                                // TODO VM.handle back
                                navController.popBackStack()
                            },
                            isSearchActive = libraryViewModel.searchActive.collectAsStateWithLifecycle().value, // Assuming search is still library specific
                            onSearchClicked = { libraryViewModel.toggleSearch() },
                            searchText = libraryViewModel.searchText.collectAsStateWithLifecycle().value,
                            onSearchTextChange = { libraryViewModel.changeSearchText(it) },
                            onSortTypeSelected = { sortType ->
                                when (currentRoute) {
                                    AppDestinations.LIBRARY_ROUTE -> libraryViewModel.updateSortType(sortType)
                                    AppDestinations.KINGDOMS_ROUTE -> kingdomViewModel.updateSortType(sortType)
                                }
                            },
                            selectedSortType = when (currentRoute) {
                                AppDestinations.LIBRARY_ROUTE -> libraryViewModel.sortType.collectAsStateWithLifecycle().value
                                AppDestinations.KINGDOMS_ROUTE -> kingdomViewModel.sortType.collectAsStateWithLifecycle().value
                                else -> SortType.ALPHABETICAL // Default
                            },
                            scrollBehavior = scrollBehavior,
                            showSearch = currentRoute == AppDestinations.LIBRARY_ROUTE
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                val isSelected = item.screenRoute == currentRoute // Simpler selection logic

                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {

                                        Log.i(
                                            "NavigationBarItem",
                                            "Selected ${item.label} (Previous: $currentRoute)"
                                        )

                                        if (currentRoute != item.screenRoute) {
                                            navController.navigate(item.screenRoute) {

                                                // Pop up to the start destination of the graph to
                                                // avoid building up a large stack of destinations
                                                // on the back stack as users select items
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                // Avoid multiple copies of the same destination when
                                                // reselecting the same item
                                                launchSingleTop = true
                                                // Restore state when reselecting a previously selected item
                                                restoreState = true
                                            }
                                        } else {
                                            // Handle re-selection of the current item (e.g., scroll to top)
                                            when (item.screenRoute) {
                                                AppDestinations.LIBRARY_ROUTE -> {
                                                    // libraryViewModel.scrollToTop() or similar
                                                    // This part needs specific implementation for scroll states
                                                    applicationScope.launch {
                                                        // TODO ScrollToTop
                                                        // kingdomListState.animateScrollToItem(0)
                                                        // Example: Reset or scroll to top for library list states
                                                        // This depends on how your libraryListState and cardListState are managed
                                                        // and if they are accessible here or if ViewModel handles it.
                                                    }
                                                }
                                                AppDestinations.KINGDOMS_ROUTE -> {
                                                    // kingdomViewModel.scrollToTop()
                                                }
                                            }
                                        }
                                    },
                                    icon = { Icon(if (isSelected) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                                    label = {
                                        Text(
                                            text = item.label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Pass navController and padding to your AppNavigation composable
                    AppNavigation(
                        navController = navController,
                        paddingValues = innerPadding,
                        onTitleChanged = onTitleChangedLambda,
                        snackbarHostState = snackbarHostState,
                        libraryViewModel = libraryViewModel,
                        kingdomViewModel = kingdomViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}

/*@Composable
fun CardDetailScreen( // Example for a screen that takes arguments
    navController: NavHostController,
    cardId: String?, // Argument passed via navigation
    onTitleChanged: (String) -> Unit
    // Potentially a CardDetailViewModel = hiltViewModel()
) {
    // You'd likely fetch card details using cardId in a ViewModel
    val title = "Card Details for: ${cardId ?: "Unknown"}"
    LaunchedEffect(title) { onTitleChanged(title) }
    Text("Displaying details for card ID: $cardId")
    BackHandler { navController.popBackStack() } // Example of handling back
}*/
