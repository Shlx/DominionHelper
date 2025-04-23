package com.example.dominionhelper.data

import com.example.dominionhelper.Card
import com.example.dominionhelper.Expansion
import com.example.dominionhelper.Set
import javax.inject.Inject

class DominionRepositoryImpl @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao
) : DominionRepository {

    override suspend fun getAllExpansions(): List<Expansion> {
        return expansionDao.getAll()
    }

    override suspend fun getCardsByExpansion(expansionSet: Set): List<Card> {
        return cardDao.getCardsByExpansion(expansionSet)
    }

    override suspend fun getRandomCardsFromOwnedExpansions(cardCount: Int): List<Card> {
        return cardDao.getRandomCardsFromOwnedExpansions(cardCount)
    }

    override suspend fun getFilteredCards(searchText: String): List<Card> {
        return cardDao.getFilteredCards(searchText)
    }
}