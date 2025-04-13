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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ExpansionGrid displays the list of expansions
@Composable
fun ExpansionGrid(
    expansions: List<Expansion>,
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
            ExpansionView(expansion = expansion, onClick = { onExpansionClick(expansion) })
        }
    }
}

// ExpansionView displays a single expansion
@Composable
fun ExpansionView(expansion: Expansion, onClick: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) } // NEW: Checkbox state

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(150.dp)
    ) {
        Box( // NEW: Box for positioning
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = expansion.imageResId),
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
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(0.dp, 0.dp, 16.dp, 8.dp)
            )
        }
    }
}