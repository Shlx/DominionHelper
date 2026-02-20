package com.marvinsuhr.dominionhelper.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.marvinsuhr.dominionhelper.model.Expansion
import com.marvinsuhr.dominionhelper.model.ExpansionWithEditions
import com.marvinsuhr.dominionhelper.utils.Constants
import com.marvinsuhr.dominionhelper.utils.getDrawableId

// TODO: Check Box contentAlignment vs contents Modifier.align (first is better)

// Displays the list of expansions
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun ExpansionList(
    expansions: List<ExpansionWithEditions>,
    onExpansionClick: (ExpansionWithEditions) -> Unit, // Click on the whole parent item
    onEditionClick: (Expansion) -> Unit,
    ownershipText: (ExpansionWithEditions) -> String,
    onOwnershipToggle: (Expansion, Boolean) -> Unit, // Callback for ownership changes
    onToggleExpansion: (ExpansionWithEditions) -> Unit, // Callback to toggle the isExpanded flag for a given expansion name
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    searchText: String = "",
    onSearchTextChange: (String) -> Unit = {},
    onBlacklistedCardsClick: () -> Unit = {},
    disabledCardCount: Int = 0
) {

    LazyColumn(
        modifier = modifier,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(Constants.PADDING_SMALL),
        contentPadding = paddingValues
    ) {
        // Search bar as first item
        item {
            SearchBar(
                searchText = searchText,
                onSearchTextChange = onSearchTextChange
            )
        }

        items(
            items = expansions,
            key = { it.name }
        ) { expansion ->

            // Parent Expansion Item
            ExpansionListItem(
                expansion = expansion,
                onClick = { onExpansionClick(expansion) },
                ownershipText = ownershipText(expansion),
                // Handle the click on the ownership toggle for single editions
                onOwnershipToggle = {
                    // Only trigger toggle for single edition items
                    if (expansion.firstEdition != null && expansion.secondEdition == null) {
                        onOwnershipToggle(expansion.firstEdition, !expansion.firstEdition.isOwned)
                    } else if (expansion.secondEdition != null && expansion.firstEdition == null) {
                        onOwnershipToggle(expansion.secondEdition, !expansion.secondEdition.isOwned)
                    }
                },
                // Check if there are multiple editions
                hasMultipleEditions = expansion.firstEdition != null && expansion.secondEdition != null,
                isExpanded = expansion.isExpanded, // Use the flag from the data class
                onToggleExpansion = { onToggleExpansion(expansion) } // Call the toggle expansion callback
            )

            // Nested Edition Items (Visible when isExpanded is true and has multiple editions)
            AnimatedVisibility(
                visible = expansion.isExpanded && (expansion.firstEdition != null && expansion.secondEdition != null),
                enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
            ) {
                Column(modifier = Modifier.padding(start = 32.dp)) { // Indent nested items
                    expansion.firstEdition?.let { firstEdition ->
                        EditionListItem(
                            expansion = firstEdition,
                            onClick = { onEditionClick(expansion.firstEdition) },
                            onToggleClick = {
                                onOwnershipToggle(
                                    expansion.firstEdition,
                                    !expansion.firstEdition.isOwned
                                )
                            }, // Toggle ownership of this edition
                            Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }
                    expansion.secondEdition?.let { secondEdition ->
                        EditionListItem(
                            expansion = secondEdition,
                            onClick = { onEditionClick(expansion.secondEdition) },
                            onToggleClick = {
                                onOwnershipToggle(
                                    expansion.secondEdition,
                                    !expansion.secondEdition.isOwned
                                )
                            }, // Toggle ownership of this edition
                        )
                    }
                }
            }
        }

        // Blacklisted cards item at the bottom
        item {
            BlacklistedCardsListItem(
                disabledCardCount = disabledCardCount,
                onClick = onBlacklistedCardsClick
            )
        }
    }
}

// Display a parent expansion item
@Composable
fun ExpansionListItem(
    expansion: ExpansionWithEditions,
    onClick: () -> Unit, // Click on the whole item goes to detail
    ownershipText: String,
    onOwnershipToggle: () -> Unit, // Callback for single edition toggle click
    hasMultipleEditions: Boolean,
    isExpanded: Boolean, // Use the flag from the data class
    onToggleExpansion: () -> Unit // Callback for clicking the arrow
) {
    Card(
        modifier = Modifier
            .height(Constants.CARD_HEIGHT)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Expansion image
            ExpansionImage(expansion)

            // Expansion name and additional text
            ExpansionLabels(
                expansion, ownershipText, Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )

            // Ownership toggle or Expand/Collapse arrow
            ExpansionIcon(
                expansion,
                hasMultipleEditions,
                onToggleExpansion,
                onOwnershipToggle,
                isExpanded
            )
        }
    }
}

@Composable
fun ExpansionImage(expansion: ExpansionWithEditions) {

    val context = LocalContext.current
    val drawableId = getDrawableId(
        context,
        expansion.firstEdition?.imageName ?: expansion.secondEdition?.imageName ?: ""
    )

    AsyncImage(
        model = drawableId,
        contentDescription = "${expansion.name} Expansion Image",
        modifier = Modifier
            .aspectRatio(1f)
            .padding(Constants.PADDING_MEDIUM),
        colorFilter = ColorFilter.tint(LocalContentColor.current)
    )
}

@Composable
fun ExpansionLabels(
    expansion: ExpansionWithEditions,
    ownershipText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(Constants.PADDING_SMALL)
    ) {
        Text(
            text = expansion.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = Constants.CARD_NAME_FONT_SIZE,
        )
        // TODO
        Text(
            text = ownershipText + " - " + expansion.firstEdition?.size?.text + " Expansion",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = Constants.TEXT_SMALL,
            color = LocalContentColor.current.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ExpansionIcon(
    expansion: ExpansionWithEditions,
    hasMultipleEditions: Boolean,
    onToggleExpansion: () -> Unit,
    onOwnershipToggle: () -> Unit,
    isExpanded: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clickable(
                /*interactionSource = null,
                indication = null,*/
                // TODO I don't like this
                onClick = {
                    if (hasMultipleEditions) {
                        Log.i("ExpansionListItem", "Clicking arrow")
                        onToggleExpansion() // Click arrow to expand/collapse
                    } else {
                        Log.i("ExpansionListItem", "Clicking ownership icon")
                        onOwnershipToggle() // Click icon to toggle ownership (single edition)
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (hasMultipleEditions) {
            // Show arrow if multiple editions exist
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(Constants.ICON_SIZE) // Adjust icon size
            )
        } else {
            // Show ownership icon for single editions
            Icon(
                imageVector = if (expansion.firstEdition?.isOwned == true || expansion.secondEdition?.isOwned == true) {
                    Icons.Filled.CheckCircle // Checkmark if owned
                } else {
                    Icons.Outlined.Circle // Empty circle if unowned
                },
                contentDescription = if (expansion.firstEdition?.isOwned == true || expansion.secondEdition?.isOwned == true) "Owned" else "Unowned",
                modifier = Modifier.size(Constants.ICON_SIZE)
            )
        }
    }
}

// Display a child item for a single edition
@Composable
fun EditionListItem(
    expansion: Expansion,
    onClick: () -> Unit,
    onToggleClick: () -> Unit, // Callback to toggle ownership of this edition
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val drawableId = getDrawableId(context, expansion.imageName)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp) // Slightly smaller height for nested items
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                // TODO: We're clicking an EDITION here, but the callback only knows the EXPANSION
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edition image (Smaller square space)
            AsyncImage(
                model = drawableId,
                contentDescription = "${expansion.name} Edition Image",
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .padding(12.dp), // Smaller padding for nested image
                colorFilter = ColorFilter.tint(LocalContentColor.current)
            )

            // Edition name and additional text
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = if (expansion.edition == 1) "First Edition" else "Second Edition", // Display the specific edition name
                    fontSize = 16.sp, // Smaller font size
                    fontWeight = FontWeight.Medium, // Use medium weight
                    textAlign = TextAlign.Start,
                )
                // Add placeholder for additional text below name (e.g., Release Date if available on edition)
                Text(
                    text = "Release date unknown", // Use actual data if available
                    fontSize = 10.sp, // Even smaller text
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            }

            // Right-hand side: Ownership toggle for this edition
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f) // Keep the right area square
                    .clickable(
                        //interactionSource = null,
                        //indication = null,
                        onClick = onToggleClick
                    ) // Click to toggle ownership of THIS edition
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Show ownership icon for this specific edition
                Icon(
                    imageVector = if (expansion.isOwned) {
                        Icons.Filled.CheckCircle // Checkmark if owned
                    } else {
                        Icons.Outlined.Circle // Empty circle if unowned
                    },
                    contentDescription = if (expansion.isOwned) "Owned" else "Unowned",
                    modifier = Modifier.size(25.dp) // Slightly smaller icon for nested items
                )
            }
        }
    }
}

@Composable
fun BlacklistedCardsListItem(
    disabledCardCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(Constants.CARD_HEIGHT)
            .then(
                if (disabledCardCount > 0) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Constants.PADDING_MEDIUM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = "Blacklisted Cards",
                tint = if (disabledCardCount > 0) {
                    LocalContentColor.current
                } else {
                    LocalContentColor.current.copy(alpha = 0.38f)
                },
                modifier = Modifier.size(Constants.ICON_SIZE)
            )

            // Text
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Constants.PADDING_SMALL)
            ) {
                Text(
                    text = "Blacklisted Cards",
                    fontSize = Constants.CARD_NAME_FONT_SIZE,
                    color = if (disabledCardCount > 0) {
                        LocalContentColor.current
                    } else {
                        LocalContentColor.current.copy(alpha = 0.38f)
                    }
                )
                Text(
                    text = "You have disabled $disabledCardCount cards",
                    fontSize = Constants.TEXT_SMALL,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            }
        }
    }
}
