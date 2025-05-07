package com.example.dominionhelper.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dominionhelper.model.Card

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

    @Delete
    suspend fun delete(card: Card)

    @Update
    suspend fun update(card: Card)

    /*@Transaction
    @Query("SELECT * FROM cards WHERE id IN (SELECT cardId FROM card_category_cross_ref WHERE categoryId = :categoryId)")
    suspend fun getCardsByCategory(categoryId: Int): List<CardWithCategories>*/

    @Query("SELECT * FROM cards")
    suspend fun getAll(): List<Card>

    @Query("SELECT * FROM cards WHERE name LIKE :filter")
    suspend fun getFilteredCards(filter: String): List<Card>

    @Query("SELECT * FROM cards WHERE sets LIKE '%' || :id || '%'")
    suspend fun getCardsByExpansion(id: String): List<Card>

    @Query("SELECT * FROM cards ORDER BY RANDOM() LIMIT :amount")
    suspend fun getRandomCards(amount: Int): List<Card>

    @Query("""
        SELECT c.* FROM cards AS c
        INNER JOIN expansions AS e ON c.sets LIKE '%' || e.id || '%'
        WHERE e.isOwned
        AND c.landscape = 0
        AND c.basic = 0
        AND c.supply = 1 /*This makes sense right*/
        ORDER BY RANDOM()
        LIMIT :amount
    """)
    suspend fun getRandomCardsFromOwnedExpansions(amount: Int): List<Card>

    @Query("SELECT * FROM cards WHERE name = :name")
    suspend fun getCardByName(name: String): Card

    @Query("SELECT * FROM cards WHERE name IN (:names)")
    suspend fun getCardsByNameList(names: List<String>): List<Card>

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun count(): Int
}