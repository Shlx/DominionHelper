package com.example.dominionhelper

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

// CardList is a composable function that creates a list of cards
@Composable
fun CardList(cardList: List<Card>, modifier: Modifier, onCardClick: (Card) -> Unit, expansionDao: ExpansionDao) {
    LazyColumn(
        modifier = modifier
    ) {
        items(cardList) { card ->
            CardView(card, onClick = { onCardClick(card) }, expansionDao = expansionDao)
        }
    }
}

// CardView displays a single card, with an image and a name
@Composable
fun CardView(card: Card, onClick: () -> Unit, expansionDao: ExpansionDao) {
    val context = LocalContext.current
    val drawableId = getDrawableId(context, card.imageName)

    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    var expansionImageId by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = card.set) {

        val applicationScope = CoroutineScope(SupervisorJob())
        applicationScope.launch {
            val expansion = expansionDao.getAll().firstOrNull { it.set == card.set }
            expansionImageId = expansion?.let { getDrawableId(context, it.imageName) } ?: R.drawable.ic_launcher_foreground
        }

    }



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                keyboardController?.hide()
                onClick()
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
                                    y = if (card.landscape) 13 else 31
                                )
                            }
                    )

                }

                // Name and Icon
                Box(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .padding(8.dp)
                ) {
                    Text(
                        text = card.name,
                        textAlign = TextAlign.Start,
                        fontSize = 20.sp
                    )

                    // Expansion Icon
                    Box(modifier = Modifier.align(Alignment.CenterEnd)
                        .padding(8.dp)) {
                        if (expansionImageId != 0) {
                            AsyncImage(
                                model = expansionImageId,
                                contentDescription = "${card.set} Expansion Image",
                                modifier = Modifier
                                    .size(48.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.ic_launcher_foreground),
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
}

@Composable
fun ColoredBar(barColors: List<Color>, modifier: Modifier = Modifier) {
    if (barColors.size > 2) {
        Log.e("ColoredBar", "barColors list must contain at most two colors.")
        barColors.dropLast(1)
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
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Find the initial index of the card
    val initialIndex = cardList.indexOf(initialCard)
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = {cardList.size})
    Column {
        Box {
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = "Card Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}