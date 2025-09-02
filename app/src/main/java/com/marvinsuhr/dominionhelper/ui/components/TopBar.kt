package com.marvinsuhr.dominionhelper.ui.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.unit.sp
import com.marvinsuhr.dominionhelper.ui.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    showBackButton: Boolean,
    onBackButtonClicked: () -> Unit,
    isSearchActive: Boolean,
    onSearchClicked: () -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSortTypeSelected: (SortType) -> Unit,
    selectedSortType: SortType,
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
                Text(title)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,//MaterialTheme.colorScheme.primaryContainer,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            // Conditionally show the back button
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
                    Icon( if (!isSearchActive) {
                        Icons.Filled.Search
                    } else {
                        Icons.Filled.Close
                    }, contentDescription = "Localized description")
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
                onSortTypeSelected = onSortTypeSelected
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
    selectedSortType: SortType,
    onSortTypeSelected: (SortType) -> Unit
) {

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        SortType.entries.forEach { sortOption ->
            SortDropdownMenuItem(
                sortType = sortOption,
                selectedSortType = selectedSortType,
                onSortTypeSelected = onSortTypeSelected,
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@Composable
fun SortDropdownMenuItem(
    sortType: SortType,
    selectedSortType: SortType,
    onSortTypeSelected: (SortType) -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(sortType.text) },
        onClick = {
            onSortTypeSelected(sortType)
            onDismissRequest()
        },
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