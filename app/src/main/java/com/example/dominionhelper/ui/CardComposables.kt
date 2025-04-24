package com.example.dominionhelper.ui

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dominionhelper.R
import com.example.dominionhelper.data.Card
import com.example.dominionhelper.data.Category
import com.example.dominionhelper.data.Type
import com.example.dominionhelper.getDrawableId
import kotlin.math.cos
import kotlin.math.sin

// Displays a list of cards
@Composable
fun CardList(
    modifier: Modifier,
    cardList: List<Card>,
    onCardClick: (Card) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        println("CardList Parameter - ${cardList.size}")
        items(cardList) { card ->
            CardView(card, onCardClick)
        }
    }
}

@Composable
fun RandomCardList(
    modifier: Modifier,
    randomCards: List<Card> = emptyList(),
    basicCards: List<Card> = emptyList(),
    dependentCards: List<Card> = emptyList(),
    onCardClick: (Card) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    Log.i("RandomCardList", "randomCards: ${randomCards.size}, basicCards: ${basicCards.size}, dependentCards: ${dependentCards.size}")

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {

        // RANDOM CARDS
        items(randomCards) { card ->
            CardView(card, onCardClick)
        }

        // DEPENDENT CARDS
        if (dependentCards.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Additional Cards", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                }

            }
            items(dependentCards) { card ->
                CardView(card, onCardClick)
            }
        }

        // BASIC CARDS
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Basic Cards", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
                Spacer(modifier = Modifier.height(8.dp))
            }

        }
        items(basicCards) { card ->
            CardView(card, onCardClick)
        }

    }

}

// Displays a single card, with an image and a name
@Composable
fun CardView(
    card: Card,
    onCardClick: (Card) -> Unit,
) {
    val context = LocalContext.current
    val drawableId = getDrawableId(context, card.imageName)

    val focusManager: FocusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Lose focus (hide keyboard) on click
                focusManager.clearFocus()
                onCardClick(card)
            }
            .padding(8.dp, 4.dp)
            .height(80.dp) // TODO: Switch to constants
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vertical Bar
            ColoredBar(
                barColors = card.getColorByTypes(),
                modifier = Modifier
                    .fillMaxHeight(),
            )

            // Image and Name
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {

                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = stringResource(
                            id = R.string.card_image_content_description,
                            card.name
                        ),
                        // TODO: Treasure and Victory cards need slightly different values
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = if (card.landscape) 2.1f else 2.5f
                                scaleY = if (card.landscape) 2.1f else 2.5f
                            }
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = when {
                                        card.basic
                                                && !card.types.contains(Type.RUINS)
                                                && !card.types.contains(Type.SHELTER)
                                                && !card.types.contains(Type.HEIRLOOM) -> 26

                                        card.landscape -> 13
                                        else -> 31
                                    }
                                )
                            }
                    )

                }

                // Name and Icon
                Box(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .padding(8.dp, 12.dp)
                ) {
                    Column {
                        Text(
                            text = card.name,
                            textAlign = TextAlign.Start,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (card.types.contains(Type.PROPHECY)) {
                            CardTypeText("Prophecy")
                        } else if (card.types.contains(Type.LANDMARK)) {
                            CardTypeText("Landmark")
                        } else if (card.types.contains(Type.TRAIT)) {
                            CardTypeText("Trait")
                        } else if (card.types.contains(Type.ALLY)) {
                            CardTypeText("Ally")
                        } else if (card.types.contains(Type.WAY)) {
                            CardTypeText("Way")
                        } else if (card.types.contains(Type.ARTIFACT)) {
                            CardTypeText("Artifact")
                        } else if (card.types.contains(Type.STATE)) {
                            CardTypeText("State")
                        } else if (card.types.contains(Type.HEX)) {
                            CardTypeText("Hex")
                        } else if (card.types.contains(Type.BOON)) {
                            CardTypeText("Boon")
                        } else if (card.types.contains(Type.LOOT)) {
                            CardTypeText("Loot")
                        } else {
                            Row {
                                if (card.cost > 0) {
                                    NumberCircle(number = card.cost)
                                }
                                if (card.debt > 0) {
                                    if (card.cost > 0) {
                                        Spacer(modifier = Modifier.padding(4.dp))
                                    }
                                    NumberHexagon(number = card.debt)
                                }
                            }
                        }
                    }

                    // Expansion Icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(card.expansionImageId),
                            contentDescription = "Unknown Image",
                            modifier = Modifier
                                .size(48.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColoredBar(barColors: List<Color>, modifier: Modifier = Modifier) {
    if (barColors.size > 2) {
        Log.e("ColoredBar", "barColors list must contain at most two colors.")
        barColors.dropLast(barColors.size - 2)
    }

    val color1 = barColors.firstOrNull() ?: Color.Transparent
    val color2 = barColors.getOrNull(1) ?: color1

    val animatedColor1 by animateColorAsState(
        targetValue = color1,
        animationSpec = tween(durationMillis = 1000), label = "color1"
    )

    val animatedColor2 by animateColorAsState(
        targetValue = color2,
        animationSpec = tween(durationMillis = 1000), label = "color2"
    )

    val brush = if (barColors.size == 1) {
        Brush.verticalGradient(listOf(animatedColor1, animatedColor1))
    } else {
        Brush.verticalGradient(listOf(animatedColor1, animatedColor2))
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
            .background(brush)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardDetailPager(
    cardList: List<Card>,
    initialCard: Card,
    modifier: Modifier = Modifier
) {
    // Find the initial index of the card
    val initialIndex = cardList.indexOf(initialCard)
    val pagerState =
        rememberPagerState(initialPage = initialIndex, pageCount = { cardList.size })
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = modifier
                .fillMaxSize(),
        ) { page ->
            // Display the card for the current page
            // TODO: Always prints the next card as well
            Log.i("CardDetailPager", "Displaying ${cardList[page].name}")
            CardDetail(card = cardList[page])
        }
    }
}

@Composable
fun CardDetail(card: Card) {
    val context = LocalContext.current
    val drawableId = getDrawableId(context, card.imageName)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Align content to the top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "Card Image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth // Changed to FillWidth to maintain aspect ratio
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(card.categories) { category ->
                CategoryText(category = category)
            }
        }
    }
}

@Composable
fun CategoryText(category: Category) {
    Text(
        text = category.name,
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
fun NumberCircle(number: Int, modifier: Modifier = Modifier) {
    val circleColor = MaterialTheme.colorScheme.primaryContainer
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(24.dp)
        ) {
            drawCircle(
                color = circleColor,
                radius = size.minDimension / 2,
                center = Offset(size.width / 2, size.height / 2)
            )

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = textColor
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 12.sp.toPx()
                    isFakeBoldText = true
                }

                val textBounds = android.graphics.Rect()
                paint.getTextBounds(number.toString(), 0, number.toString().length, textBounds)

                canvas.nativeCanvas.drawText(
                    number.toString(),
                    size.width / 2,
                    (size.height / 2) - (textBounds.top + textBounds.bottom) / 2,
                    paint
                )
            }
        }
    }
}

@Composable
fun NumberHexagon(number: Int, modifier: Modifier = Modifier) {
    val hexagonColor = MaterialTheme.colorScheme.secondaryContainer
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(25.dp)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2

            // Draw the hexagon
            drawIntoCanvas { canvas ->
                val hexagonPath = android.graphics.Path()
                val angle = 2.0 * Math.PI / 6 // 6 sides

                // Start at the first vertex
                hexagonPath.moveTo(
                    centerX + radius * cos(0.0).toFloat(),
                    centerY + radius * sin(0.0).toFloat()
                )

                // Draw lines to each subsequent vertex
                for (i in 1..6) {
                    hexagonPath.lineTo(
                        centerX + radius * cos(angle * i).toFloat(),
                        centerY + radius * sin(angle * i).toFloat()
                    )
                }

                // Close the path
                hexagonPath.close()
                val paint = android.graphics.Paint()
                paint.color = hexagonColor.toArgb()
                paint.style = android.graphics.Paint.Style.FILL
                canvas.nativeCanvas.drawPath(hexagonPath, paint)

                // Draw the text
                val textPaint = android.graphics.Paint().apply {
                    color = textColor
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 12.sp.toPx()
                    isFakeBoldText = true
                }

                val textBounds = android.graphics.Rect()
                textPaint.getTextBounds(
                    number.toString(),
                    0,
                    number.toString().length,
                    textBounds
                )

                canvas.nativeCanvas.drawText(
                    number.toString(),
                    centerX,
                    centerY - (textBounds.top + textBounds.bottom) / 2,
                    textPaint
                )
            }
        }
    }
}

@Composable
fun CardTypeText(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Start,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.secondary,
        fontStyle = Italic
    )
}