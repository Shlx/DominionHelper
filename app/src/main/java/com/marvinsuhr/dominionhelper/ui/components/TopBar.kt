package com.marvinsuhr.dominionhelper.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marvinsuhr.dominionhelper.CurrentScreen
import com.marvinsuhr.dominionhelper.model.AppSortType
import com.marvinsuhr.dominionhelper.ui.KingdomViewModel
import com.marvinsuhr.dominionhelper.ui.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TopBar(
    title: String,
    showBackButton: Boolean,
    onBackButtonClicked: () -> Unit,
    isSearchActive: Boolean,
    onSearchClicked: () -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    currentScreen: CurrentScreen, // TODO: Needed?
    onSortTypeSelected: (AppSortType) -> Unit,
    selectedSortType: AppSortType?,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showSearch: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        title = {
            // Conditionally show the search field
            if (isSearchActive) {
                TextField(
                    value = searchText,
                    onValueChange = { onSearchTextChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            "Search Cards",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Adjust alpha as needed
                            fontSize = 20.sp
                        )
                    },
                    textStyle = TextStyle(fontSize = 20.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, // Transparent background
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, // No underline
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences, // Capitalize the first letter
                        imeAction = ImeAction.Search
                    )
                )
            } else {
                Text(
                    text = title,
                    modifier = Modifier.sizeIn(maxWidth = 300.dp),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = { onBackButtonClicked() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (showSearch) {
                IconButton(onClick = {
                    onSearchClicked()
                }) {
                    Icon(
                        if (!isSearchActive) {
                            Icons.Filled.Search
                        } else {
                            Icons.Filled.Close
                        }, contentDescription = "Localized description"
                    )
                }
            }

            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Localized description"
                )
            }

            SortDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                selectedSortType = selectedSortType,
                onSortTypeSelected = onSortTypeSelected,
                currentScreen = currentScreen
            )
        },
        scrollBehavior = scrollBehavior
    )
    LaunchedEffect(key1 = isSearchActive) {
        if (isSearchActive) {
            Log.d("TopBar", "requestFocus")
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun SortDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedSortType: AppSortType?,
    onSortTypeSelected: (AppSortType) -> Unit,
    currentScreen: CurrentScreen
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        if  (selectedSortType != null) {

            if (currentScreen == CurrentScreen.Library) {
                LibraryViewModel.SortType.entries.forEach { sortOption ->
                    val appSortType = AppSortType.Library(sortOption)
                    SortDropdownMenuItem(
                        sortType = appSortType,
                        selectedSortType = selectedSortType,
                        onSortTypeSelected = onSortTypeSelected,
                        onDismissRequest = onDismissRequest
                    )
                }
            } else if (currentScreen == CurrentScreen.Kingdoms) {
                KingdomViewModel.SortType.entries.forEach { sortOption ->
                    val appSortType = AppSortType.Kingdom(sortOption)
                    SortDropdownMenuItem(
                        sortType = appSortType,
                        selectedSortType = selectedSortType,
                        onSortTypeSelected = onSortTypeSelected,
                        onDismissRequest = onDismissRequest
                    )
                }
            }
        }
    }
}

@Composable
fun SortDropdownMenuItem(
    sortType: AppSortType,
    selectedSortType: AppSortType,
    onSortTypeSelected: (AppSortType) -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(sortType.text) },
        onClick = {
            onSortTypeSelected(sortType)
            onDismissRequest()
        },
        modifier = Modifier.background(
            if (sortType == selectedSortType) MaterialTheme.colorScheme.primaryContainer
            else Color.Transparent
        ),
        trailingIcon = {
            if (selectedSortType == sortType) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "$sortType selected"
                )
            }
        }
    )
}