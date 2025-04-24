package com.example.dominionhelper.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dominionhelper.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scope: CoroutineScope,
    drawerState: DrawerState,
    isSearchActive: Boolean,
    onSearchClicked: () -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onRandomCardsClicked: () -> Unit,
    onSortTypeSelected: (SortType) -> Unit,
    selectedSortType: SortType,
    topBarTitle: String,
    hideSearch: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        title = {
            // Conditionally show the search field
            if (isSearchActive) {
                // TODO placeholder text is cut off
                TextField(
                    value = searchText,
                    onValueChange = { onSearchTextChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp) // Add padding here
                        .focusRequester(focusRequester),
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
            } else {
                Text(topBarTitle)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        navigationIcon = {
            // Conditionally show the back button or hamburger menu
            if (isSearchActive) {
                IconButton(onClick = { onSearchClicked() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            if (!hideSearch) {
                IconButton(onClick = {
                    onSearchClicked()
                }) {
                    Icon(Icons.Filled.Search, contentDescription = "Localized description")
                }
            }
            IconButton(onClick = {
                onRandomCardsClicked()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.dice3),
                    contentDescription = "Localized description",
                    modifier = Modifier.size(24.dp)
                )
            }

            var expanded by remember { mutableStateOf(false) }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Localized description"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sort Alphabetically") },
                    onClick = {
                        onSortTypeSelected(SortType.ALPHABETICAL)
                        expanded = false
                    },
                    trailingIcon = {
                        if (selectedSortType == SortType.ALPHABETICAL) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Sort Alphabetically Selected"
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sort by Cost") },
                    onClick = {
                        onSortTypeSelected(SortType.COST)
                        expanded = false
                    },
                    trailingIcon = {
                        if (selectedSortType == SortType.COST) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Sort by Cost Selected"
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sort By Expansion") },
                    onClick = {
                        onSortTypeSelected(SortType.EXPANSION)
                        expanded = false
                    },
                    trailingIcon = {
                        if (selectedSortType == SortType.EXPANSION) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Sort by Expansion Selected"
                            )
                        }
                    }
                )
            }
        }
    )
    LaunchedEffect(key1 = isSearchActive) {
        if (isSearchActive) {
            Log.d("TopBar", "requestFocus")
            focusRequester.requestFocus()
        }
    }
}