package com.marvinsuhr.dominionhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.marvinsuhr.dominionhelper.ui.theme.DominionHelperTheme
import com.marvinsuhr.dominionhelper.ui.KingdomViewModel
import com.marvinsuhr.dominionhelper.ui.LibraryViewModel
import com.marvinsuhr.dominionhelper.ui.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DominionHelperTheme {

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val currentScreen = CurrentScreen.fromRoute(currentRoute)

                val snackbarHostState = remember { SnackbarHostState() }
                val topAppBarState = rememberTopAppBarState()
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

                var currentTopBarTitle by rememberSaveable { mutableStateOf("") }
                val onTitleChangedLambda = { newTitle: String ->
                    currentTopBarTitle = newTitle
                }

                // Get ViewModels for the current screen using the navBackStackEntry
                // This ensures we get the same instances as in the navigation composables
                val currentLibraryViewModel: LibraryViewModel? = navBackStackEntry?.let {
                    when (currentScreen) {
                        CurrentScreen.Library -> hiltViewModel(it)
                        else -> null
                    }
                }
                val currentKingdomViewModel: KingdomViewModel? = navBackStackEntry?.let {
                    when (currentScreen) {
                        CurrentScreen.Kingdoms -> hiltViewModel(it)
                        else -> null
                    }
                }
                val currentSettingsViewModel: SettingsViewModel? = navBackStackEntry?.let {
                    when (currentScreen) {
                        CurrentScreen.Settings -> hiltViewModel(it)
                        else -> null
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    /*topBar = {
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
                    },*/
                    floatingActionButton = {
                        if (currentScreen == CurrentScreen.Kingdoms) {
                            FloatingActionButton(
                                onClick = { currentKingdomViewModel?.getRandomKingdom() },
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "FAB to generate a new kingdom"
                                )
                            }
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
                                            // Use the pre-fetched ViewModels to trigger scroll to top
                                            when (currentScreen) {
                                                CurrentScreen.Library -> currentLibraryViewModel?.triggerScrollToTop()
                                                CurrentScreen.Kingdoms -> currentKingdomViewModel?.triggerScrollToTop()
                                                CurrentScreen.Settings -> currentSettingsViewModel?.triggerScrollToTop()
                                            }
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
                        onTitleChanged = onTitleChangedLambda,
                        snackbarHostState = snackbarHostState,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}
