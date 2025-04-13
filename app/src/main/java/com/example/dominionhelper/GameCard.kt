// GameCard.kt
package com.example.dominionhelper

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Database entity for a game card
@Entity(tableName = "cards")
data class GameCard(
    @PrimaryKey val id: Int,
    val name: String,
    val expansionId: Int, // Expansion name as a foreign key
    val types: List<Type>, // You may need a TypeConverter for this
    val effects: List<Effect>, // And this
    val cost: Int,
    val imageResId: Int, // Name of the drawable
    val link: String, // Link to the card's wiki page
    /*@ColumnInfo(name = "is_favorite")*/ val isFavorite: Boolean = false,
    /*@ColumnInfo(name = "is_banned")*/ val isBanned: Boolean = false
) {
    enum class Expansion(val id: Int) {
        BASE(0), INTRIGUE(1)
    }

    enum class Type {
        ACTION, VICTORY
    }

    enum class Effect {
        CARD, ACTION, BUY, GOLD, TRASH
    }
}


// DAO for Game Cards
@Dao
interface GameCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(gameCards: List<GameCard>)

    @Query("SELECT * FROM cards")
    suspend fun getAll(): List<GameCard>

    @Query("SELECT * FROM cards")
    fun getAllFlow(): Flow<List<GameCard>>

    @Query("SELECT * FROM cards WHERE isfavorite = 1")
    suspend fun getFavorites(): List<GameCard>

    @Query("SELECT * FROM cards WHERE isbanned = 1")
    suspend fun getBanned(): List<GameCard>
    // Add more queries as needed (e.g., get by name, expansion, etc.)

    @Query("SELECT * FROM cards WHERE LOWER(name) LIKE LOWER(:letters)")
    fun getFilteredCards(letters: String): Flow<List<GameCard>>

    @Query("DELETE FROM cards")
    suspend fun delete()

}