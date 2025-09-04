package com.marvinsuhr.dominionhelper.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.marvinsuhr.dominionhelper.R
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.model.Kingdom
import com.marvinsuhr.dominionhelper.utils.Constants
import com.marvinsuhr.dominionhelper.utils.getDrawableId


@Composable
fun KingdomList2(
    kingdomList: List<Kingdom>,
    onGenerateKingdom: () -> Unit,
    onKingdomClicked: (Kingdom) -> Unit,
    onDeleteClick: (Kingdom) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        contentPadding = PaddingValues(Constants.PADDING_SMALL),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(Constants.PADDING_SMALL)
    ) {
        item {
            Box(
                modifier = Modifier.clickable { onGenerateKingdom() }
            ) {
                Column {
                    Text("Reroll")
                    Icon(
                        imageVector = Icons.Outlined.Casino,
                        contentDescription = "Generate new kingdom"
                    )
                }
            }
        }

        items(kingdomList) { kingdom ->
            KingdomCard(kingdom, onDeleteClick = { onDeleteClick(kingdom) },) { onKingdomClicked(kingdom) }
        }
    }
}

@Composable
fun KingdomCard(kingdom: Kingdom, onDeleteClick: () -> Unit, onKingdomClick: () -> Unit) {
    val cardsToDisplay = kingdom.randomCards.entries.take(10).toList()
    val numColumns = 5

    IconButton(onClick = { onDeleteClick() }) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Delete kingdom"
        )
    }

    if (cardsToDisplay.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = { onKingdomClick() }
        ) {
            Column(
                //modifier = Modifier.padding(4.dp) // Optional padding around the entire grid
            ) {
                // Group cards into chunks of 5 for each row
                cardsToDisplay.chunked(numColumns).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        //horizontalArrangement = Arrangement.spacedBy(4.dp) // Spacing between items in a row
                        // If you want items centered if there are fewer than 5 in the last row:
                        // horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                    ) {
                        // Display items in the current row
                        rowItems.forEach { (card, _) -> // Destructure Map.Entry
                            Box(
                                modifier = Modifier.weight(1f), // Each item takes equal space in the row
                                contentAlignment = Alignment.Center // Center CardImage within its allocated space
                            ) {
                                CardImage2(card = card)
                            }
                        }
                        // If you want to fill remaining cells in a row with empty placeholders
                        // when rowItems.size < numColumns (e.g., last row has only 3 items)
                        repeat((numColumns - rowItems.size).coerceAtLeast(0)) {
                            Box(modifier = Modifier.weight(1f)) { /* Empty placeholder */ }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardImage2(card: Card) {

    val context = LocalContext.current
    val drawableId = getDrawableId(context, card.imageName)

    Box(
        modifier = Modifier
            .padding(Constants.PADDING_SMALL)
            .clip(RoundedCornerShape(Constants.IMAGE_ROUNDED))
    ) {

        AsyncImage(
            model = drawableId,
            contentDescription = stringResource(
                id = R.string.card_image_content_description,
                card.name,
            ),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 2.5f
                    scaleY = 2.5f

                }
                .offset {
                    IntOffset(
                        x = 0,
                        y = 35
                    )

                }
        )
    }
}