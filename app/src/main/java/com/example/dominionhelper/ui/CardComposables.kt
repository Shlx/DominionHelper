package com.example.dominionhelper.ui

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.dominionhelper.Kingdom
import com.example.dominionhelper.R
import com.example.dominionhelper.data.Card
import com.example.dominionhelper.data.Category
import com.example.dominionhelper.data.Set
import com.example.dominionhelper.data.Type
import com.example.dominionhelper.findIndexOfReference
import com.example.dominionhelper.getDrawableId

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
fun KingdomList(
    modifier: Modifier,
    kingdom: Kingdom,
    onCardClick: (Card) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    Log.i("RandomCardList", "randomCards: ${kingdom.randomCards.size}, basicCards: ${kingdom.basicCards.size}, dependentCards: ${kingdom.dependentCards.size}, startingCards: ${kingdom.startingCards.size}")

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {

        // RANDOM CARDS
        items(kingdom.randomCards) { card ->
            CardView(card, onCardClick)
        }

        // DEPENDENT CARDS
        if (kingdom.hasDependentCards()) {
            item {
                CardSpacer("Additional Cards")

            }
            items(kingdom.dependentCards) { card ->
                CardView(card, onCardClick)
            }
        }

        // BASIC CARDS
        item {
            CardSpacer("Basic Cards")

        }
        items(kingdom.basicCards) { card ->
            CardView(card, onCardClick)
        }

        // STARTING CARDS
        item {
            CardSpacer("Starting Cards")
        }
        items(kingdom.startingCards.keys.toList()) { card ->
            CardView(card, onCardClick, kingdom.startingCards[card]!!)
        }
    }
    }

@Composable
fun CardSpacer(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = text, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
        Spacer(modifier = Modifier.height(8.dp))
    }
}

// Displays a single card, with an image and a name
@Composable
fun CardView(
    card: Card,
    onCardClick: (Card) -> Unit,
    amount: Int = 1,
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
                                if (card.set == Set.PLACEHOLDER) {
                                    scaleX = 1.25f
                                    scaleY = 1.25f
                                } else {
                                    scaleX = if (card.landscape) 2.1f else 2.5f
                                    scaleY = if (card.landscape) 2.1f else 2.5f
                                }
                            }
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = when {
                                        card.name == "Potion" -> 0
                                        card.basic
                                                && !card.types.contains(Type.RUINS)
                                                && !card.types.contains(Type.SHELTER)
                                                && !card.types.contains(Type.HEIRLOOM) -> 26

                                        card.set == Set.PLACEHOLDER -> 0
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
                            text = card.name + if (amount > 1) " x$amount" else "",
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
                                    if (card.cost > 0 || card.potion) {
                                        Spacer(modifier = Modifier.padding(4.dp))
                                    }
                                    NumberHexagon(number = card.debt)
                                }
                                if (card.potion) {
                                    if (card.cost > 0) {
                                        Spacer(modifier = Modifier.padding(4.dp))
                                    }
                                    Image(
                                        painter = painterResource(id = R.drawable.set_alchemy),
                                        contentDescription = "Tinted Image",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondaryContainer),
                                        modifier = Modifier.size(22.dp)
                                    )
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
    modifier: Modifier = Modifier,
    cardList: List<Card>,
    initialCard: Card
) {
    val initialIndex = findIndexOfReference(cardList, initialCard)
    val pagerState =
        rememberPagerState(initialPage = initialIndex, pageCount = { cardList.size })
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = modifier
                .fillMaxSize(),
        ) { page ->

            // Display the card for the current page
            Log.i("CardDetailPager", "Displaying ${cardList[page].name}, Index $initialIndex")
            println("CardDetailPager $cardList.size")
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
fun CardTypeText(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Start,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.secondary,
        fontStyle = Italic
    )
}