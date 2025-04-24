package com.example.dominionhelper.data

import kotlinx.coroutines.flow.Flow

interface DominionRepository {
    suspend fun getAllExpansions(): List<Expansion>
    fun getCardsByExpansion(expansionSet: Set): Flow<List<Card>>
    suspend fun getRandomCardsFromOwnedExpansions(cardCount: Int): List<Card>
    suspend fun getFilteredCards(searchText: String): List<Card>
}