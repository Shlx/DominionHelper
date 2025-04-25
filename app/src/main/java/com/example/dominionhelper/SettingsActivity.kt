package com.example.dominionhelper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dominionhelper.ui.SettingsList
import com.example.dominionhelper.ui.SettingsViewModel
import com.example.dominionhelper.ui.theme.DominionHelperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DominionHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SettingsView(settingsViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Do something special when a new Intent is received
        // while this activity is already running
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    settingsViewModel: SettingsViewModel
) {

    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val applicationScope = rememberCoroutineScope()
    val topBarTitle by remember { mutableStateOf("Settings") }

    // Handle back gesture according to state
    //BackHandler(enabled = true) {}

    // TODO: This will lead to other parts of the app (I hope)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(applicationScope, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(topBarTitle) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            applicationScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                )
            }
        ) { innerPadding ->
            SettingsList(uiState.settings, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun DrawerContent(
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    val screens = listOf("Home", "Settings", "Option 3")
    val context = LocalContext.current

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        screens.forEach { option ->
            NavigationDrawerItem(
                label = { Text(option) },
                selected = option == "Settings",
                onClick = {
                    scope.launch {
                        drawerState.close()
                        //onOptionSelected(option)
                    }
                    when (option) {
                        "Home" -> {
                            navigateToActivity(context, MainActivity::class.java)
                        }

                        "Settings" -> {
                            navigateToActivity(context, SettingsActivity::class.java)
                        }

                        "Option 3" -> {
                            //navigateToActivity(context, AboutActivity::class.java)
                        }

                        else -> {}
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}
