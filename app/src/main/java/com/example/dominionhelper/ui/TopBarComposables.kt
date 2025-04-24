package com.example.dominionhelper.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dominionhelper.R
import com.example.dominionhelper.data.Expansion
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
    selectedExpansion: Expansion? = null
) {
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
            } else if (selectedExpansion != null) {
                Text(selectedExpansion.name) // Display expansion name
            } else {
                Text("Dominion Helper")
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
            IconButton(onClick = {
                onSearchClicked()
            }) {
                Icon(Icons.Filled.Search, contentDescription = "Localized description")
            }
            IconButton(onClick = {
                onRandomCardsClicked()
            }) {
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