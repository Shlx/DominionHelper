// Expansion.kt
package com.example.dominionhelper

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Database entity for an Expansion
@Entity(tableName = "expansions")
data class Expansion(
    @PrimaryKey val id: Int,
    val name: String,
    //val gameCards: List<GameCard>,
    val imageResId: Int, // Name of the drawable
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "is_owned") val isOwned: Boolean = false
)

// DAO for Expansions
@Dao
interface ExpansionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expansions: List<Expansion>)

    @Query("SELECT * FROM expansions")
    suspend fun getAll(): List<Expansion>

    @Query("SELECT * FROM expansions")
    fun getAllFlow(): Flow<List<Expansion>>

    @Query("SELECT * FROM expansions WHERE is_favorite = 1")
    suspend fun getFavorites(): List<Expansion>

    @Query("DELETE FROM expansions")
    suspend fun delete(): Unit

    // Add more queries as needed (e.g., get by name)
}