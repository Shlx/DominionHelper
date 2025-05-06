package com.example.dominionhelper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dominionhelper.model.Expansion
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpansionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expansions: List<Expansion>)

    @Query("SELECT * FROM expansions")
    fun getAll(): Flow<List<Expansion>>

    // Turning this into a suspend fun crashes??
    @Query("SELECT * FROM expansions WHERE isOwned = 1")
    fun getOwned(): Flow<List<Expansion>>

    @Query("SELECT * FROM expansions WHERE id = :id")
    suspend fun getExpansionById(id: Int): Expansion?

    @Query("UPDATE expansions SET isOwned = :isOwned WHERE name = :expansionName AND edition = 1")
    suspend fun updateFirstEditionOwned(expansionName: String, isOwned: Boolean)

    @Query("UPDATE expansions SET isOwned = :isOwned WHERE name = :expansionName AND edition = 2")
    suspend fun updateSecondEditionOwned(expansionName: String, isOwned: Boolean)

    @Query("SELECT isOwned FROM expansions WHERE name = :expansionName AND edition = 1")
    suspend fun isFirstEditionOwned(expansionName: String): Boolean

    @Query("SELECT isOwned FROM expansions WHERE name = :expansionName AND edition = 2")
    suspend fun isSecondEditionOwned(expansionName: String): Boolean
}