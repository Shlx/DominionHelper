package com.marvinsuhr.dominionhelper.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.marvinsuhr.dominionhelper.ui.SettingsViewModel
import com.marvinsuhr.dominionhelper.utils.calculatePadding
import com.marvinsuhr.dominionhelper.ui.components.SettingsList
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onTitleChanged: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    viewModel: SettingsViewModel,
    performBackNavigation: () -> Unit,
    innerPadding: PaddingValues
) {

    Log.i("MainActivity", "Settings Screen Content. UI State: not implemented")

    LaunchedEffect(Unit) { onTitleChanged("Settings") }

    val settingsListState = rememberLazyListState()

    /*LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            applicationScope.launch {
                snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
                viewModel.clearError()
            }
        }
    }*/

    LaunchedEffect(Unit) {
        viewModel.scrollToTopEvent.collect {
            settingsListState.animateScrollToItem(0)
        }
    }

    BackHandler {
        performBackNavigation()
    }

    val uiState by viewModel.uiState.collectAsState()
    SettingsList(
        uiState.settings,
        listState = settingsListState,
        paddingValues = calculatePadding(innerPadding)
    )
}
