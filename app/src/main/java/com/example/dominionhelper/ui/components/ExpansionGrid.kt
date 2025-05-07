package com.example.dominionhelper.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dominionhelper.model.Expansion
import com.example.dominionhelper.model.ExpansionWithEditions
import com.example.dominionhelper.model.OwnedEdition
import com.example.dominionhelper.utils.getDrawableId

// Displays the list of expansions
@Composable
fun ExpansionGrid(
    expansions: List<ExpansionWithEditions>,
    onExpansionClick: (ExpansionWithEditions) -> Unit,
    onToggleClick: (ExpansionWithEditions) -> Unit,
    ownageText: (ExpansionWithEditions) -> String,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState()
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = gridState
    ) {
        items(expansions) { expansion ->
            ExpansionView(
                expansion = expansion,
                onClick = { onExpansionClick(expansion) },
                onToggleClick = { onToggleClick(expansion) },
                ownageText = ownageText(expansion)
            )
        }
    }
}

// Displays a single expansion
@Composable
fun ExpansionView(
    expansion: ExpansionWithEditions,
    onClick: () -> Unit,
    onToggleClick: () -> Unit,
    ownageText: String
) {

    val context = LocalContext.current
    val drawableId = getDrawableId(
        context,
        expansion.firstEdition?.imageName ?: expansion.secondEdition?.imageName ?: ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(150.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Expansion image
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "${expansion.name} Expansion Image",
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )

            // Expansion name
            Text(
                text = expansion.name,
                fontSize = 20.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )

            // Switch at bottom right
            /*Switch(
                checked = expansion.isOwned,
                onCheckedChange = { onCheckedChange(expansion, it) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(0.dp, 0.dp, 16.dp, 8.dp)
            )*/
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clickable(onClick = onToggleClick)
            ) {
                Text(ownageText)
            }
        }
    }
}

@Composable
fun ExpansionList(
    expansions: List<ExpansionWithEditions>,
    onExpansionClick: (ExpansionWithEditions) -> Unit,
    onToggleClick: (ExpansionWithEditions) -> Unit,
    ownageText: (ExpansionWithEditions) -> String,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState()
) {
    LazyColumn(
        //state = listState
        modifier = modifier
    ) {
        items(expansions) { expansion ->
            ExpansionItem(
                expansion = expansion,
                onClick = { onExpansionClick(expansion) },
                onToggleClick = { onToggleClick(expansion) },
                ownageText = ownageText(expansion)
            )
        }

        items(expansions) { expansion ->
            ExpansionItem2(
                expansion = expansion,
                onClick = { onExpansionClick(expansion) },
                onToggleClick = { onToggleClick(expansion) },
                ownageText = ownageText(expansion)
            )
        }

        items(expansions) { expansion ->
            ExpansionItem3(
                expansion = expansion,
                onClick = { onExpansionClick(expansion) },
                onToggleClick = { onToggleClick(expansion) },
                ownageText = ownageText(expansion)
            )
        }
    }
}

// Displays a single expansion
@Composable
fun ExpansionItem(
    expansion: ExpansionWithEditions,
    onClick: () -> Unit,
    onToggleClick: () -> Unit,
    ownageText: String
) {

    val context = LocalContext.current
    val drawableId = getDrawableId(
        context,
        expansion.firstEdition?.imageName ?: expansion.secondEdition?.imageName ?: ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp, 4.dp)
            .height(80.dp)
    ) {
        Row {

            // Expansion image
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "${expansion.name} Expansion Image",
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .align(Alignment.CenterVertically)
                    .padding(16.dp)
                //.weight(0.2f)
            )

            // Expansion name
            Column(
                modifier = Modifier
                    .align(Alignment.Top)
                    .weight(0.7f)
            ) {
                Text(
                    text = expansion.name,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(8.dp)
                        .clickable(onClick = onToggleClick)
                ) {
                    Text(
                        text = ownageText,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun ExpansionItem2(
    expansion: ExpansionWithEditions,
    onClick: () -> Unit,
    onToggleClick: () -> Unit,
    ownageText: String, // Still using ownageText to convey status
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val drawableId = getDrawableId(
        context,
        expansion.firstEdition?.imageName ?: expansion.secondEdition?.imageName ?: ""
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp, 4.dp)
            .height(80.dp) // Consider using a fixed size or adjusting dynamically
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Expansion image (Square space)
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "${expansion.name} Expansion Image",
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .align(Alignment.CenterVertically)
                    .padding(16.dp)
                //.weight(0.2f)
            )

            // Expansion name
            Text(
                text = expansion.name,
                fontSize = 18.sp, // Slightly smaller font
                fontWeight = FontWeight.Bold, // Make name bold
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1f) // Allow name to take up available space
                    .padding(start = 8.dp, end = 8.dp)
            )

            // Ownership status indicator (Clickable)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f) // Make the clickable area square
                    .clickable(onClick = onToggleClick)
                    .padding(8.dp), // Add padding inside the clickable area
                contentAlignment = Alignment.Center // Center content within the Box
            ) {
                // You can use icons and/or text here based on ownageText
                // Example: Using an icon and a small text indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = getOwnershipIcon(ownageText), // Custom function to get icon
                        contentDescription = "Ownership Status",
                        tint = getOwnershipColor(ownageText), // Custom function to get color
                        modifier = Modifier.size(24.dp) // Adjust icon size
                    )
                    Text(
                        text = ownageText,
                        fontSize = 10.sp, // Smaller text for status
                        textAlign = TextAlign.Center,
                        color = getOwnershipColor(ownageText) // Match text color with icon
                    )
                }
            }
        }
    }
}

// Helper functions to determine icon and color based on ownership text
@Composable
fun getOwnershipIcon(ownageText: String): ImageVector {
    return when (ownageText.lowercase()) {
        "Owned" -> Icons.Filled.CheckCircle // Example: Checkmark for owned
        "First Edition Owned" -> Icons.Outlined.CheckCircle // Example: Outlined circle for first edition
        "Second Edition Owned" -> Icons.Filled.CheckCircle // Example: Filled circle for second edition
        "Both Editions Owned" -> Icons.Filled.CheckCircle // Example: Checkmark for both (or a different icon)
        else -> Icons.Outlined.CheckCircle // Default icon for unowned or unknown status
    }
}

@Composable
fun getOwnershipColor(ownageText: String): Color {
    return when (ownageText.lowercase()) {
        "Owned", "Both Editions Owned" -> Color.Green // Example: Green for owned/both
        "First Edition Owned", "Second Edition Owned" -> Color.Blue // Example: Blue for specific editions
        else -> Color.Gray // Default color for unowned
    }
}

@Composable
fun ExpansionItem3(
    expansion: ExpansionWithEditions,
    onClick: () -> Unit,
    onToggleClick: () -> Unit,
    ownageText: String,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val drawableId = getDrawableId(
        context,
        expansion.firstEdition?.imageName ?: expansion.secondEdition?.imageName ?: ""
    )

    var selectedEditions1 by remember {
        mutableStateOf("Unowned")
    }

    var selectedEditions2 by remember {
        mutableStateOf(
            setOf<String>()
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp, 4.dp)
            .height(80.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Expansion image (Square space)
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "${expansion.name} Expansion Image",
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .align(Alignment.CenterVertically)
                    .padding(16.dp)
                //.weight(0.2f)
            )

            // Expansion name
            Text(
                text = expansion.name,
                fontSize = 18.sp, // Slightly smaller font
                fontWeight = FontWeight.Bold, // Make name bold
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1f) // Allow name to take up available space
                    .padding(start = 8.dp, end = 8.dp)
            )

            if (expansion.firstEdition == null || expansion.secondEdition == null) {
                // Single-select segmented button for ownership
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .wrapContentWidth() // Allow the row to take minimal width
                        .padding(end = 8.dp) // Padding on the right
                ) {

                    val options = listOf("Owned", "Unowned")
                    options.forEachIndexed { index, edition ->

                        SegmentedButton(
                            selected = edition == selectedEditions1,
                            onClick = { selectedEditions1 = edition },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),

                            label = { Text(edition) }
                        )

                    }
                }
            } else {

                MultiChoiceSegmentedButtonRow {

                    val options = listOf("First", "Second")
                    options.forEachIndexed { index, edition ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            checked = selectedEditions2.contains(edition),
                            onCheckedChange = { isChecked ->
                                // Update the local state based on the checked state
                                selectedEditions2 = if (isChecked) {
                                    selectedEditions2 + edition // Add edition if checked
                                } else {
                                    selectedEditions2 - edition // Remove edition if unchecked
                                }
                            },
                            label = { Text(options[index]) }
                        )

                    }
                }
            }
        }
    }
}


fun mapToSingleOwnershipStatus(expansion: ExpansionWithEditions): OwnedEdition {
    return when {
        expansion.firstEdition?.isOwned == true && expansion.secondEdition?.isOwned == true -> OwnedEdition.BOTH
        expansion.firstEdition?.isOwned == true -> OwnedEdition.FIRST
        expansion.secondEdition?.isOwned == true -> OwnedEdition.SECOND
        else -> OwnedEdition.NONE
    }
}

////////////
////////////
////////////
////////////
////////////



@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun ExpansionList2(
    expansions: List<ExpansionWithEditions>,
    onExpansionClick: (ExpansionWithEditions) -> Unit, // Click on the whole parent item
    onOwnershipToggle: (ExpansionWithEditions) -> Unit, // Callback for ownership changes
    onToggleExpansion: (ExpansionWithEditions) -> Unit, // Callback to toggle the isExpanded flag for a given expansion name
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = expansions,
            key = { it.name }
        ) { expansion ->

            Column {
                // Parent Expansion Item
                ExpansionListItem2(
                    expansion = expansion,
                    onClick = { onExpansionClick(expansion) },

                    // Handle the click on the ownership toggle for single editions
                    onOwnershipToggle = {
                        // Only trigger toggle for single edition items
                        if (expansion.firstEdition != null && expansion.secondEdition == null) {
                            onOwnershipToggle(expansion)
                        } else if (expansion.secondEdition != null && expansion.firstEdition == null) {
                            onOwnershipToggle(expansion)
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
                                onToggleClick = { onOwnershipToggle(expansion) }, // Toggle ownership of this edition
                            )
                        }
                        expansion.secondEdition?.let { secondEdition ->
                            EditionListItem(
                                expansion = secondEdition,
                                onToggleClick = { onOwnershipToggle(expansion) }, // Toggle ownership of this edition
                            )
                        }
                    }
                }
            }
        }
    }
}

// Composable for the Parent Expansion Item
@Composable
fun ExpansionListItem2(
    expansion: ExpansionWithEditions,
    onClick: () -> Unit, // Click on the whole item goes to detail
    onOwnershipToggle: () -> Unit, // Callback for single edition toggle click
    hasMultipleEditions: Boolean,
    isExpanded: Boolean, // Use the flag from the data class
    onToggleExpansion: () -> Unit // Callback for clicking the arrow
) {
    val context = LocalContext.current
    val drawableId = getDrawableId(
        context,
        expansion.firstEdition?.imageName ?: expansion.secondEdition?.imageName ?: ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 4.dp)
            .height(80.dp) // Consistent height for parent items
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }, // Make the entire row clickable for detail view
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Expansion image
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "${expansion.name} Expansion Image",
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .padding(8.dp)
            )

            // Expansion name and additional text
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = expansion.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                )
                // Add placeholder for additional text below name
                Text(
                    text = "Release date unknown", // Use actual data if available
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.6f)
                )
            }

            // Right-hand side: Ownership toggle or Expand/Collapse arrow
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f) // Keep the right area square
                    .clickable {
                        if (hasMultipleEditions) {
                            onToggleExpansion() // Click arrow to expand/collapse
                        } else {
                            onOwnershipToggle() // Click icon to toggle ownership (single edition)
                        }
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasMultipleEditions) {
                    // Show arrow if multiple editions exist
                    Log.i("ExpansionListItem2", "isExpanded: $isExpanded, hasMultipleEditions: $hasMultipleEditions")
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.size(24.dp) // Adjust icon size
                    )
                } else {
                    // Show ownership icon for single editions
                    Icon(
                        imageVector = if (expansion.firstEdition?.isOwned == true || expansion.secondEdition?.isOwned == true) {
                            Icons.Filled.CheckCircle // Checkmark if owned
                        } else {
                            Icons.Rounded.FavoriteBorder // Empty circle if unowned
                        },
                        contentDescription = if (expansion.firstEdition?.isOwned == true || expansion.secondEdition?.isOwned == true) "Owned" else "Unowned",
                        modifier = Modifier.size(24.dp) // Adjust icon size
                    )
                }
            }
        }
    }
}

@Composable
fun EditionListItem(
    expansion: Expansion,
    onToggleClick: () -> Unit, // Callback to toggle ownership of this edition
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Use the image name from the OwnedEdition for the nested item
    val drawableId = getDrawableId(context, expansion.imageName ?: "")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp) // Adjust padding for nested items
            .height(64.dp) // Slightly smaller height for nested items
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edition image (Smaller square space)
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "${expansion.name} Edition Image",
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .padding(4.dp) // Smaller padding for nested image
            )

            // Edition name and additional text
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = expansion.name, // Display the specific edition name
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
                    .clickable(onClick = onToggleClick) // Click to toggle ownership of THIS edition
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Show ownership icon for this specific edition
                Icon(
                    imageVector = if (expansion.isOwned) {
                        Icons.Filled.CheckCircle // Checkmark if owned
                    } else {
                        Icons.Outlined.Favorite // Empty circle if unowned
                    },
                    contentDescription = if (expansion.isOwned) "Owned" else "Unowned",
                    modifier = Modifier.size(20.dp) // Slightly smaller icon for nested items
                )
            }
        }
    }
}