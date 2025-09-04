package com.marvinsuhr.dominionhelper.data.repositories

import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.data.KingdomDao
import com.marvinsuhr.dominionhelper.data.mappers.toDomainModel
import com.marvinsuhr.dominionhelper.data.mappers.toEntity
import com.marvinsuhr.dominionhelper.model.Kingdom
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KingdomRepository @Inject constructor(
    private val kingdomDao: KingdomDao,
    private val cardDao: CardDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Retrieves all kingdoms from the database as a Flow of domain models.
     * Each KingdomEntity is mapped to a Kingdom domain model.
     * Note: This mapping involves fetching card details for each kingdom,
     * which could be performance-intensive for large lists.
     */
    fun getAllKingdoms(): Flow<List<Kingdom>> {
        return kingdomDao.getAllKingdomsFlow().map { kingdomEntities ->
            // Map each KingdomEntity to the Kingdom domain model
            // This requires cardDao for fetching associated cards.
            kingdomEntities.mapNotNull { entity ->
                // Ensure toDomainModel can handle potential nulls if a card ID is invalid,
                // or filter out kingdoms that can't be fully constructed.
                entity.toDomainModel(cardDao)
            }
        }
    }

    /**
     * Retrieves a specific kingdom by its database ID.
     * @param kingdomId The Int ID of the kingdom in the database.
     * @return The Kingdom domain model if found, null otherwise.
     */
    suspend fun getKingdomById(kingdomId: Int): Kingdom? {
        // Perform DB operations on the injected dispatcher (e.g., Dispatchers.IO)
        return withContext(defaultDispatcher) {
            val kingdomEntity = kingdomDao.getKingdomById(kingdomId)
            kingdomEntity?.toDomainModel(cardDao)
        }
    }

    /**
     * Saves a kingdom to the database.
     * The Kingdom domain model is first converted to a KingdomEntity.
     * @param kingdom The Kingdom domain model to save.
     * @return The row ID of the newly inserted kingdom, or -1 if an error occurred.
     */
    suspend fun saveKingdom(kingdom: Kingdom): Long {
        return withContext(defaultDispatcher) {
            val kingdomEntity = kingdom.toEntity() // Assumes toEntity() correctly sets id=0 for auto-generation
            kingdomDao.insertKingdom(kingdomEntity) // insertKingdom should return the Long rowId
        }
    }

    /**
     * Deletes a kingdom from the database by its ID.
     * @param kingdomId The Int ID of the kingdom to delete.
     */
    suspend fun deleteKingdomById(kingdomId: Int) {
        withContext(defaultDispatcher) {
            kingdomDao.deleteKingdomById(kingdomId)
        }
    }
}