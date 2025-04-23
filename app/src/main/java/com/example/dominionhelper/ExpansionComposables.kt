package com.example.dominionhelper

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dominionhelper.data.ExpansionDao
import kotlinx.coroutines.launch

// ExpansionGrid displays the list of expansions
@Composable
fun ExpansionGrid(
    expansions: List<Expansion>,
    expansionDao: ExpansionDao,
    onExpansionClick: (Expansion) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.padding(8.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(expansions) { expansion ->
            ExpansionView(
                expansion = expansion,
                expansionDao = expansionDao,
                onClick = { onExpansionClick(expansion) })
        }
    }
}

// ExpansionView displays a single expansion
@Composable
fun ExpansionView(expansion: Expansion, expansionDao: ExpansionDao, onClick: () -> Unit) {
    var isOwned by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Get the expansion from the database and update the state
    LaunchedEffect(key1 = expansion.id) {
        val expansionEntity = expansionDao.getExpansionById(expansion.id)
        isOwned = expansionEntity?.isOwned ?: false
    }

    val context = LocalContext.current
    val drawableId = getDrawableId(context, expansion.imageName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(150.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = "${expansion.name} Expansion Image",
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center) // Center the image
            )
            // Expansion name, now at bottom left
            Text(
                text = expansion.name,
                fontSize = 24.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
            // Switch at bottom right
            Switch(
                checked = isOwned, // Read from db
                onCheckedChange = { newIsChecked ->
                    coroutineScope.launch {
                        // Update the database
                        expansionDao.updateIsOwned(expansion.id, newIsChecked)
                        // Update the state
                        isOwned = newIsChecked
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(0.dp, 0.dp, 16.dp, 8.dp)
            )
        }
    }
}