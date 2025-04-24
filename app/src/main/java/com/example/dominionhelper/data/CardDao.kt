package com.example.dominionhelper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CardDao {

    companion object {
        val BASIC_CARD_NAMES = listOf(
            "Copper",
            "Silver",
            "Gold",
            "Estate",
            "Duchy",
            "Province"
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<Card>)

    /*@Insert
    suspend fun insertAllCategories(categories: List<CardCategory>)

    @Insert
    suspend fun insertCardCategoryCrossRef(crossRef: CardCategoryCrossRef)*/

    @Delete
    suspend fun delete(card: Card)

    @Update
    suspend fun update(card: Card)

    /*@Transaction
    @Query("SELECT * FROM cards")
    suspend fun getAllCardsWithCategories(): List<CardWithCategories>

    @Query("SELECT * FROM card_categories")
    suspend fun getAllCategories(): List<CardCategory>

    @Transaction
    @Query("SELECT * FROM cards WHERE id IN (SELECT cardId FROM card_category_cross_ref WHERE categoryId = :categoryId)")
    suspend fun getCardsByCategory(categoryId: Int): List<CardWithCategories>*/

    @Query("SELECT * FROM cards")
    suspend fun getAll(): List<Card>

    @Query("SELECT * FROM cards WHERE name LIKE :filter")
    suspend fun getFilteredCards(filter: String): List<Card>

    @Query("SELECT * FROM cards WHERE `set` = :expansion")
    suspend fun getCardsByExpansion(expansion: Set): List<Card>

    @Query("SELECT * FROM cards ORDER BY RANDOM() LIMIT :amount")
    suspend fun getRandomCards(amount: Int): List<Card>

    @Query("""
        SELECT c.* FROM cards AS c
        INNER JOIN expansions AS e ON c.`set` = e.`set`
        WHERE e.isOwned = 1
        AND c.landscape = 0
        AND c.basic = 0
        ORDER BY RANDOM()
        LIMIT :amount
    """)
    suspend fun getRandomCardsFromOwnedExpansions(amount: Int): List<Card>

    @Query("SELECT * FROM cards WHERE name IN (:names)")
    suspend fun getBasicCards(names: List<String> = BASIC_CARD_NAMES): List<Card>

    @Query("SELECT * FROM cards ORDER BY RANDOM() LIMIT 1")
    suspend fun getDependentCards(): List<Card>

}