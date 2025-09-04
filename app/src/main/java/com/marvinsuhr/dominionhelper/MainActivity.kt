package com.marvinsuhr.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marvinsuhr.dominionhelper.ui.KingdomViewModel
import com.marvinsuhr.dominionhelper.ui.LibraryViewModel
import com.marvinsuhr.dominionhelper.ui.ScreenViewModel
import com.marvinsuhr.dominionhelper.ui.SettingsViewModel
import com.marvinsuhr.dominionhelper.ui.components.TopBar
import com.marvinsuhr.dominionhelper.ui.theme.DominionHelperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val libraryViewModel: LibraryViewModel by viewModels()
    private val kingdomViewModel: KingdomViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DominionHelperTheme {

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val currentScreen = CurrentScreen.fromRoute(currentRoute)

                val currentViewModel = when (currentScreen) {
                    CurrentScreen.Library -> libraryViewModel
                    CurrentScreen.Kingdoms -> kingdomViewModel
                    CurrentScreen.Settings -> settingsViewModel
                } as ScreenViewModel

                val snackbarHostState = remember { SnackbarHostState() }
                val topAppBarState = rememberTopAppBarState()
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

                var currentTopBarTitle by rememberSaveable { mutableStateOf("") }
                val onTitleChangedLambda = { newTitle: String ->
                    currentTopBarTitle = newTitle
                }

                val actualSelectedSortTypeForMenu by currentViewModel.currentAppSortType.collectAsStateWithLifecycle()
                val showBackButton by currentViewModel.showBackButton.collectAsStateWithLifecycle()
                val showTopAppBar by currentViewModel.showTopAppBar.collectAsStateWithLifecycle()

                val performBackNavigation = {
                    if (!currentViewModel.handleBackNavigation()) {
                        if (navController.previousBackStackEntry != null) {
                            navController.popBackStack()
                        } else {
                            finish()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (showTopAppBar) {
                            TopBar(
                                title = currentTopBarTitle,
                                showBackButton = showBackButton,
                                onBackButtonClicked = { performBackNavigation() },
                                isSearchActive = libraryViewModel.searchActive.collectAsStateWithLifecycle().value, // Assuming search is still library specific
                                onSearchClicked = { libraryViewModel.toggleSearch() },
                                searchText = libraryViewModel.searchText.collectAsStateWithLifecycle().value,
                                onSearchTextChange = { libraryViewModel.changeSearchText(it) },
                                currentScreen = currentScreen,
                                onSortTypeSelected = { currentViewModel.onSortTypeSelected(it) },
                                selectedSortType = actualSelectedSortTypeForMenu,
                                scrollBehavior = scrollBehavior,
                                showSearch = currentScreen == CurrentScreen.Library
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { item ->
                                val isSelected = item.screenRoute == currentRoute

                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {

                                        Log.i(
                                            "NavigationBarItem",
                                            "Selected ${item.label} (Previous: $currentRoute)"
                                        )

                                        // New item selected
                                        if (currentRoute != item.screenRoute) {
                                            navController.navigate(item.screenRoute) {

                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        } else { // Same item selected: scroll up
                                            currentViewModel.triggerScrollToTop()
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
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

                    AppNavigation(
                        navController = navController,
                        paddingValues = innerPadding,
                        onTitleChanged = onTitleChangedLambda,
                        snackbarHostState = snackbarHostState,
                        libraryViewModel = libraryViewModel,
                        kingdomViewModel = kingdomViewModel,
                        settingsViewModel = settingsViewModel,
                        performBackNavigation = performBackNavigation
                    )
                }
            }
        }
    }
}
