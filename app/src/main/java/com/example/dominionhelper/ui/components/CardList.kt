package com.example.dominionhelper.ui.components

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
import com.example.dominionhelper.model.Card
import com.example.dominionhelper.model.OwnedEdition
import com.example.dominionhelper.model.Set
import com.example.dominionhelper.model.Type
import com.example.dominionhelper.utils.Constants
import com.example.dominionhelper.utils.getDrawableId
import kotlin.math.cos
import kotlin.math.sin

// TODO: Check Box contentAlignment vs contents Modifier.align (first is better)

// Displays a list of cards
@Composable
fun CardList(
    modifier: Modifier,
    cardList: List<Card>,
    includeEditionSelection: Boolean = false,
    selectedEdition: OwnedEdition,
    onEditionSelected: (Int) -> Unit,
    onCardClick: (Card) -> Unit,
    onToggleEnable: (Card) -> Unit,
    listState: LazyListState = rememberLazyListState(),
) {
    Log.i("CardList", "${cardList.size} cards")

    Column(modifier = modifier.padding(horizontal = Constants.PADDING_SMALL)) {

        val topPadding = if (includeEditionSelection) 0.dp else Constants.PADDING_SMALL

        if (includeEditionSelection) {
            EditionSelectionButtons(onEditionSelected, selectedEdition)
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(top = topPadding),
            verticalArrangement = Arrangement.spacedBy(Constants.PADDING_SMALL)
        ) {
            items(cardList) { card ->
                CardView(
                    card,
                    onCardClick,
                    showIcon = false,
                    onToggleEnable = { onToggleEnable(card) })
            }
        }
    }
}

@Composable
fun EditionSelectionButtons(
    onEditionSelected: (Int) -> Unit,
    selectedEdition: OwnedEdition = OwnedEdition.FIRST
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Constants.PADDING_SMALL),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { onEditionSelected(1) },
            colors = if (selectedEdition == OwnedEdition.FIRST) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        ) {
            Text("1st Edition")
        }
        Button(
            onClick = { onEditionSelected(2) },
            colors = if (selectedEdition == OwnedEdition.SECOND) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        ) {
            Text("2nd Edition")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun KingdomList(
    modifier: Modifier,
    kingdom: Kingdom,
    onCardClick: (Card) -> Unit,
    selectedPlayers: Int,
    onPlayerCountChange: (Int) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    isDismissEnabled: Boolean,
    onCardDismissed: (Card) -> Unit
) {
    Log.i(
        "KingdomList",
        "randomCards: ${kingdom.randomCards.size}, basicCards: ${kingdom.basicCards.size}, dependentCards: ${kingdom.dependentCards.size}, startingCards: ${kingdom.startingCards.size}, landscapeCards: ${kingdom.landscapeCards.size}"
    )

    Column(modifier = modifier.padding(horizontal = Constants.PADDING_SMALL)) {

        PlayerSelectionButtons(
            selectedPlayers = selectedPlayers,
            onPlayerSelected = { onPlayerCountChange(it) }
        )

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(Constants.PADDING_SMALL)
        ) {

            // RANDOM CARDS
            items(
                items = kingdom.randomCards.keys.toList(),
                key = { card -> card.id }
            ) { card ->
                if (isDismissEnabled)
                    DismissableCard(card, onCardDismissed, onCardClick, Modifier.animateItem())
                else {
                    CardView(card, onCardClick, showIcon = true, kingdom.randomCards[card]!!)
                }
            }

            // LANDSCAPE CARDS
            items(
                items = kingdom.landscapeCards.keys.toList(),
                key = { card -> card.id }
            ) { card ->
                if (isDismissEnabled)
                    DismissableCard(card, onCardDismissed, onCardClick, Modifier.animateItem())
                else {
                    CardView(card, onCardClick, showIcon = true, kingdom.landscapeCards[card]!!)
                }
            }

            // DEPENDENT CARDS
            if (kingdom.hasDependentCards()) {
                item {
                    CardSpacer("Additional Cards")
                }
                items(kingdom.dependentCards.keys.toList()) { card ->
                    CardView(card, onCardClick, showIcon = true, kingdom.dependentCards[card]!!)
                }
            }

            // BASIC CARDS
            item {
                CardSpacer("Basic Cards")
            }
            items(kingdom.basicCards.keys.toList()) { card ->
                CardView(card, onCardClick, showIcon = true, kingdom.basicCards[card]!!)
            }

            // STARTING CARDS
            item {
                CardSpacer("Starting Cards")
            }
            items(kingdom.startingCards.keys.toList()) { card ->
                CardView(card, onCardClick, showIcon = true, kingdom.startingCards[card]!!)
            }
        }
    }
}

@Composable
fun PlayerSelectionButtons(selectedPlayers: Int, onPlayerSelected: (Int) -> Unit) {
    val playerCounts = listOf(2, 3, 4)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Constants.PADDING_SMALL),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        playerCounts.forEach { count ->
            Button(
                onClick = { onPlayerSelected(count) },
                colors = if (selectedPlayers == count) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            ) {
                Text("$count Players")
            }
        }
    }
}

@Composable
fun DismissableCard(
    card: Card,
    onCardDismissed: (Card) -> Unit,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            val dismissed = dismissValue != SwipeToDismissBoxValue.Settled
            if (dismissed) {
                onCardDismissed(card)
            }
            dismissed
        },
        positionalThreshold = { it * 0.25f } // Distance to be dismissed
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {

            // Change scale of icon depending on position
            val scale by animateDpAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75.dp else 1.dp,
                label = "icon scale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                // Icon  behind the swipe
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Dismiss Icon",
                    modifier = Modifier.scale(scale.value),
                    tint = Color.White
                )
            }
        }
    ) {
        CardView(card, onCardClick)
    }
}


@Composable
fun CardSpacer(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(Constants.PADDING_SMALL))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
        Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM))
        Text(text = text, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(0.5f))
        Spacer(modifier = Modifier.height(Constants.PADDING_SMALL))
    }
}

// Displays a single card, with an image and a name
// TODO parameter order
@Composable
fun CardView(
    card: Card,
    onCardClick: (Card) -> Unit,
    showIcon: Boolean = true,
    amount: Int = 1,
    onToggleEnable: () -> Unit = {}
) {
    val focusManager: FocusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .height(Constants.CARD_HEIGHT)
            .clickable {
                focusManager.clearFocus() // Lose focus (hide keyboard) on click
                onCardClick(card)
            }
    ) {
        Row {
            // Vertical colored bar
            ColoredBar(card.getColorByTypes())

            // Cropped card image
            CardImage(card)

            // Card name and price
            CardLabels(card, amount, modifier = Modifier.weight(1f))

            // Expansion or enable / disable icon
            CardIcon(card, showIcon, onToggleEnable)
        }
    }
}

@Composable
fun ColoredBar(barColors: List<Color>) {
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
        modifier = Modifier
            .fillMaxHeight()
            .width(Constants.COLOR_BAR_WIDTH)
            .background(brush)
    )
}

@Composable
fun CardImage(card: Card) {

    val context = LocalContext.current
    val drawableId = getDrawableId(context, card.imageName)

    Box(
        modifier = Modifier
            .padding(Constants.PADDING_SMALL)
            .clip(RoundedCornerShape(Constants.IMAGE_ROUNDED))
            .width(Constants.CARD_IMAGE_WIDTH)
    ) {

        Image(
            painter = painterResource(id = drawableId),
            contentDescription = stringResource(
                id = R.string.card_image_content_description,
                card.name
            ),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (card.sets.contains(Set.PLACEHOLDER)) {
                        if (card.name == "Trash Mat") {
                            scaleX = 1.75f
                            scaleY = 1.75f
                        } else {
                            scaleX = 1.25f
                            scaleY = 1.25f
                        }
                    } else {
                        scaleX = if (card.landscape) 2.1f else 2.5f
                        scaleY = if (card.landscape) 2.1f else 2.5f
                    }
                }
                .offset {
                    IntOffset(
                        x = 0,
                        y = when {
                            card.name == "Potion" || card.sets.contains(Set.PLACEHOLDER) -> 0
                            card.landscape || card.name == "Curse" -> 13
                            card.basic
                                    && !card.types.contains(Type.RUINS)
                                    && !card.types.contains(Type.SHELTER)
                                    && !card.types.contains(Type.HEIRLOOM) -> 26
                            else -> 31
                        }
                    )
                }
        )
    }
}

@Composable
fun CardLabels(card: Card, amount: Int, modifier: Modifier) {
        Column(
            modifier = modifier
                .fillMaxHeight()
                .padding(horizontal = 4.dp, vertical = 12.dp)
        ) {
            Text(
                text = card.name + if (amount > 1) " x$amount" else "",
                fontSize = Constants.CARD_NAME_FONT_SIZE
            )
            Spacer(modifier = Modifier.weight(1f))

            // TODO: Dirty
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
                        NumberCircle(card.cost)
                    }
                    if (card.debt > 0) {
                        if (card.cost > 0 || card.potion) {
                            Spacer(modifier = Modifier.padding(4.dp))
                        }
                        NumberHexagon(card.debt)
                    }
                    if (card.potion) {
                        if (card.cost > 0) {
                            Spacer(modifier = Modifier.padding(4.dp))
                        }
                        Image(
                            painter = painterResource(id = R.drawable.set_alchemy),
                            contentDescription = "Tinted Image",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.size(22.dp).offset(y = 1.dp)
                        )
                    }
                }
            }
    }
}

// Display the type of landscape cards
@Composable
fun CardTypeText(text: String) {
    Text(
        text = text,
        fontSize = Constants.TEXT_SMALL,
        color = MaterialTheme.colorScheme.secondary,
        fontStyle = Italic
    )
}

// Display a number in a circle (Used for card costs)
@Composable
fun NumberCircle(number: Int) {
    val circleColor = MaterialTheme.colorScheme.primaryContainer
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Box(
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(Constants.CARD_PRICE_SIZE)
        ) {
            drawCircle(
                color = circleColor,
                radius = size.minDimension / 2,
                center = Offset(size.width / 2, size.height / 2)
            )

            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = textColor
                    textAlign = Paint.Align.CENTER
                    textSize = 12.sp.toPx()
                    isFakeBoldText = true
                }

                val textBounds = Rect()
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

// Display a number in a hexagon (Used for card debt)
@Composable
fun NumberHexagon(number: Int) {
    val hexagonColor = MaterialTheme.colorScheme.secondaryContainer
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier // Apply the passed-in modifier here
            .offset(y = (-1).dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(Constants.CARD_DEBT_SIZE)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2

            // Draw the hexagon
            drawIntoCanvas { canvas ->
                val hexagonPath = Path()
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
                val paint = Paint()
                paint.color = hexagonColor.toArgb()
                paint.style = Paint.Style.FILL
                canvas.nativeCanvas.drawPath(hexagonPath, paint)

                // Draw the text
                val textPaint = Paint().apply {
                    color = textColor
                    textAlign = Paint.Align.CENTER
                    textSize = 12.sp.toPx()
                    isFakeBoldText = true
                }

                val textBounds = Rect()
                textPaint.getTextBounds(
                    number.toString(),
                    0,
                    number.toString().length,
                    textBounds
                )

                canvas.nativeCanvas.drawText(
                    number.toString(),
                    centerX - 1f,
                    centerY - (textBounds.top + textBounds.bottom) / 2,
                    textPaint
                )
            }
        }
    }
}

@Composable
fun CardIcon(card: Card, showIcon: Boolean, onToggleEnable: () -> Unit) {

    val isToggleIconVisible = !showIcon && !card.basic

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .then (if (isToggleIconVisible) Modifier.clickable { onToggleEnable() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (showIcon) {

            // Expansion Icon
            Image(
                painter = painterResource(card.expansionImageId),
                contentDescription = "Unknown Image",
                modifier = Modifier
                    .size(Constants.ICON_SIZE)
            )
        } else if (!card.basic) {

            // En- / Disable button
            Icon(
                imageVector = if (card.isEnabled) {
                    Icons.Filled.CheckCircle // Checkmark if owned / allowed
                } else {
                    Icons.Outlined.RemoveCircleOutline
                    //Icons.RemoveCircle // Circle with minus if unowned / banned
                },
                contentDescription = if (card.isEnabled) "Allowed" else "Banned",
                modifier = Modifier
                    .size(Constants.ICON_SIZE)
            )
        }
    }
}
