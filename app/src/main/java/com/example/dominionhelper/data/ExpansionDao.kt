package com.example.dominionhelper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpansionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expansions: List<Expansion>)

    @Query("SELECT * FROM expansions")
    fun getAll(): List<Expansion>

    @Query("SELECT * FROM expansions WHERE isOwned = 1")
    fun getOwned(): Flow<List<Expansion>>

    @Query("SELECT * FROM expansions WHERE id = :id")
    suspend fun getExpansionById(id: Int): Expansion?

    @Query("UPDATE expansions SET isOwned = :isOwned WHERE id = :id")
    suspend fun updateIsOwned(id: Int, isOwned: Boolean)
}