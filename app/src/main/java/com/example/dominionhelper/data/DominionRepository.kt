package com.example.dominionhelper.data

import com.example.dominionhelper.Card
import com.example.dominionhelper.Expansion
import com.example.dominionhelper.Set

interface DominionRepository {
    suspend fun getAllExpansions(): List<Expansion>
    suspend fun getCardsByExpansion(expansionSet: Set): List<Card>
    suspend fun getRandomCardsFromOwnedExpansions(cardCount: Int): List<Card>
    suspend fun getFilteredCards(searchText: String): List<Card>
}