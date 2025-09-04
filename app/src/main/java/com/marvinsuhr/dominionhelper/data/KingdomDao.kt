package com.marvinsuhr.dominionhelper.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.marvinsuhr.dominionhelper.data.entities.KingdomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KingdomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKingdom(kingdom: KingdomEntity): Long

    @Query("SELECT * FROM kingdoms WHERE id = :kingdomId")
    suspend fun getKingdomById(kingdomId: Int): KingdomEntity?

    @Query("SELECT * FROM kingdoms ORDER BY id DESC")
    fun getAllKingdomsFlow(): Flow<List<KingdomEntity>>

    @Query("SELECT * FROM kingdoms ORDER BY id DESC")
    suspend fun getAllKingdoms(): List<KingdomEntity>

    @Query("DELETE FROM kingdoms WHERE id = :kingdomId")
    suspend fun deleteKingdomById(kingdomId: Int)
}