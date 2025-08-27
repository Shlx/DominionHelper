package com.marvinsuhr.dominionhelper.ui.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.distinctUntilChanged
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.utils.findIndexOfReference
import com.marvinsuhr.dominionhelper.utils.getDrawableId
import kotlinx.coroutines.flow.distinctUntilChanged

// Show a pager scrolling through a list of cards
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardDetailPager(
    modifier: Modifier = Modifier,
    cardList: List<Card>,
    initialCard: Card,
    onClick: () -> Unit,
    onPageChanged: (Card) -> Unit
) {

    if (cardList.isEmpty()) {
        Log.w("CardDetailPager", "Card list is empty, cannot initialize Pager.")
        return
    }

    /*
    val initialIndex = remember(cardList, initialCard) {
        findIndexOfReference(cardList, initialCard).coerceIn(0, cardList.size - 1)
    }
     */

    val initialIndex = findIndexOfReference(cardList, initialCard)
    val pagerState =
        rememberPagerState(initialPage = initialIndex, pageCount = { cardList.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page >= 0 && page < cardList.size) {
                    val currentCard = cardList[page]
                    Log.i("CardDetailPager", "Page changed to: ${currentCard.name}")
                    onPageChanged(currentCard)
                }
            }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = modifier
                .fillMaxSize(),
        ) { page ->

            // It's possible for 'page' to be temporarily out of bounds during fast scrolls
            // or state restoration. Ensure it's valid.
            if (page >= 0 && page < cardList.size) {
                val cardForPage = cardList[page]
                Log.i("CardDetailPager", "Displaying ${cardForPage.name}, Index $page")
                CardDetail(card = cardForPage, onClick = onClick)
            } else {
                Log.w("CardDetailPager", "Page index $page is out of bounds for cardList size ${cardList.size}")
                // Optionally, display a placeholder or empty content
            }
        }
    }
}

// Show a detail view of a single card
@Composable
fun CardDetail(card: Card, onClick: () -> Unit) {
    val drawableId = getDrawableId(LocalContext.current, card.imageName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                Log.d("CardDetail", "CardDetail Column clicked!") // For debugging
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "Card Image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(card.categories) { category ->
                Text(
                    text = category.name,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
