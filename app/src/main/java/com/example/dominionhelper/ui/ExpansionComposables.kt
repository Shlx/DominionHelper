package com.example.dominionhelper.ui

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dominionhelper.data.Expansion
import com.example.dominionhelper.getDrawableId

// Displays the list of expansions
@Composable
fun ExpansionGrid(
    expansions: List<Expansion>,
    onExpansionClick: (Expansion) -> Unit,
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
                onClick = { onExpansionClick(expansion) })
        }
    }
}

// Displays a single expansion
@Composable
fun ExpansionView(
    expansion: Expansion,
    onClick: () -> Unit,
    expansionViewModel: ExpansionViewModel = hiltViewModel()
) {

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
                    .size(64.dp)
                    .align(Alignment.Center) // Center the image
            )
            // Expansion name, now at bottom left
            Text(
                text = expansion.name,
                fontSize = 20.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
            // Switch at bottom right
            Switch(
                checked = expansion.isOwned,
                onCheckedChange = { newIsChecked ->
                    expansionViewModel.updateIsOwned(expansion.id, newIsChecked)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(0.dp, 0.dp, 16.dp, 8.dp)
            )
        }
    }
}