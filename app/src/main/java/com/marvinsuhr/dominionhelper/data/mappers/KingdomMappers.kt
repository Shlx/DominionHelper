package com.marvinsuhr.dominionhelper.data.mappers

import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.data.entities.KingdomEntity
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.model.Kingdom

// Convert Kingdom to KingdomEntity
fun Kingdom.toEntity(): KingdomEntity {
    return KingdomEntity(
        id = this.id ?: 0,
        randomCardIds = this.randomCards.keys.map { it.id },
        landscapeCardIds = this.landscapeCards.keys.map { it.id }
    )
}

// Convert KingdomEntity to Kingdom
suspend fun KingdomEntity.toDomainModel(cardDao: CardDao): Kingdom {
    val randomCardObjects = cardDao.getCardsByIds(this.randomCardIds)
    val landscapeCardObjects = cardDao.getCardsByIds(this.landscapeCardIds)

    // Reconstruct the LinkedHashMaps, default to count = 1
    val randomCardsMap = LinkedHashMap<Card, Int>()
    randomCardObjects.forEach { randomCardsMap[it] = 1 }

    val landscapeCardsMap = LinkedHashMap<Card, Int>()
    landscapeCardObjects.forEach { landscapeCardsMap[it] = 1 }

    return Kingdom(
        randomCards = randomCardsMap,
        landscapeCards = landscapeCardsMap,
        id = this.id
    )
}
