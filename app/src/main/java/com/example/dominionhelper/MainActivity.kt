package com.example.dominionhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dominionhelper.ui.theme.DominionHelperTheme
import androidx.compose.material3.Card

fun getSampleCardList(): List<GameCard> {
    return listOf(
        GameCard(
            name = "Village",
            expansion = GameCard.Expansion.BASE,
            types = listOf(GameCard.Type.ACTION),
            effects = listOf(GameCard.Effect.CARD, GameCard.Effect.ACTION),
            cost = 3,
            imageResId = R.drawable.ic_launcher_foreground,
            onClick = {}
        ),
        GameCard(
            name = "Smithy",
            expansion = GameCard.Expansion.BASE,
            types = listOf(GameCard.Type.ACTION),
            effects = listOf(GameCard.Effect.CARD),
            cost = 4,
            imageResId = R.drawable.ic_launcher_foreground,
            onClick = {}
        ),
        GameCard(
            name = "Market",
            expansion = GameCard.Expansion.BASE,
            types = listOf(GameCard.Type.ACTION),
            effects = listOf(
                GameCard.Effect.CARD,
                GameCard.Effect.ACTION,
                GameCard.Effect.BUY,
                GameCard.Effect.GOLD
            ),
            cost = 5,
            imageResId = R.drawable.ic_launcher_foreground,
            onClick = {}
        ),
        GameCard(
            name = "Laboratory",
            expansion = GameCard.Expansion.BASE,
            types = listOf(GameCard.Type.ACTION),
            effects = listOf(GameCard.Effect.CARD, GameCard.Effect.ACTION),
            cost = 5,
            imageResId = R.drawable.ic_launcher_foreground,
            onClick = {}
        )
    )
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            DominionHelperTheme {
                DominionHelperTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CardList(getSampleCardList())
                    }
                }
            }
        }
    }
}

// CardView displays a single card, with an image and a name
@Composable
fun CardView(card: GameCard) {
    Card(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = card.imageResId),
                contentDescription = "${card.name} Image",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = card.name)
            }
        }
    }
}
// CardList is a composable function that creates a list of cards
@Composable
fun CardList(cardList: List<GameCard>) {
    LazyColumn {
        items(cardList) { card ->
            CardView(card)
        }
    }
}