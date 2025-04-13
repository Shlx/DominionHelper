package com.example.dominionhelper

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// CardList is a composable function that creates a list of cards
@Composable
fun CardList(cardList: List<GameCard>, modifier: Modifier, onCardClick: (GameCard) -> Unit) {
    LazyColumn(
        modifier = modifier
    ) {
        items(cardList) { card ->
            CardView(card, onClick = { onCardClick(card) })
        }
    }
}

// CardView displays a single card, with an image and a name
@Composable
fun CardView(card: GameCard, onClick: () -> Unit) {
    val topCropPercentage = 0.10f // 10%
    val bottomCropPercentage = 0.50f // 50%
    val visibleHeightPercentage = 1f - topCropPercentage - bottomCropPercentage
    val imageWidth = 80.dp // Set the image width.
    val cardImageHeight = 100f//imageWidth / visibleHeightPercentage //calculate the card image height using the percentages.

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp, 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(50.dp),//cardImageHeight), // <--- Set the Row's height here
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box( //NEW: use Box to display the image
                modifier = Modifier
                    .weight(0.3f) // Take up 30% of the available width
            ) {
                Image(
                    painter = painterResource(id = card.imageResId),
                    contentDescription = card.name,
                    modifier = Modifier
                        .width(imageWidth)
                        .fillMaxHeight()
                        .clip(
                            object : Shape {
                                override fun createOutline(
                                    size: androidx.compose.ui.geometry.Size,
                                    layoutDirection: LayoutDirection,
                                    density: Density
                                ): Outline {
                                    val path = Path().apply {
                                        val topCropAmount = size.height * topCropPercentage
                                        val bottomCropAmount = size.height * bottomCropPercentage
                                        val rect = Rect(
                                            0f,
                                            topCropAmount,
                                            size.width,
                                            size.height - bottomCropAmount
                                        )
                                        addRect(rect)
                                    }
                                    return Outline.Generic(path)
                                }
                            }
                        ),
                    contentScale = ContentScale.Crop // Crop to maintain aspect ratio
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(0.7f) // Take up 70% of the available width
                    .height(50.dp)//cardImageHeight) // <--- Set the column's height here
            ) {
                Text(text = card.name,
                    modifier = Modifier.height(IntrinsicSize.Min)) // <--- set the text height here
            }
        }
    }
}