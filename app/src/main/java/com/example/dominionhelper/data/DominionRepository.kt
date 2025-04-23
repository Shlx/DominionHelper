package com.example.dominionhelper.data

interface DominionRepository {
    suspend fun getAllExpansions(): List<Expansion>
    suspend fun getCardsByExpansion(expansionSet: Set): List<Card>
    suspend fun getRandomCardsFromOwnedExpansions(cardCount: Int): List<Card>
    suspend fun getFilteredCards(searchText: String): List<Card>
}