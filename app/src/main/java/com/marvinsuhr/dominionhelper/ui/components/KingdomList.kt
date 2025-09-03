package com.marvinsuhr.dominionhelper.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.marvinsuhr.dominionhelper.model.Kingdom
import com.marvinsuhr.dominionhelper.utils.Constants


@Composable
fun KingdomList(
    kingdomList: List<Kingdom>
) {
    LazyColumn(
        contentPadding = PaddingValues(Constants.PADDING_SMALL),
        verticalArrangement = Arrangement.spacedBy(Constants.PADDING_SMALL)
    ) {
        items(kingdomList) { kingdom ->
            KingdomCard(kingdom)
        }
    }
}

@Composable
fun KingdomCard(kingdom: Kingdom) {
    // TODO: Implement KingdomCard
}