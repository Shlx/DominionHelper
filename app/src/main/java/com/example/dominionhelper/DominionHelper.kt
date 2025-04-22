package com.example.dominionhelper

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DominionHelper : Application() {

    // Coroutine scope tied to the application's lifecycle
    val applicationScope = CoroutineScope(SupervisorJob())

    // Database
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "dominion_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    prePopulateDatabase()
                }
            })
            .build()
    }

    // DAOs
    val cardDao: CardDao by lazy { database.cardDao() }
    val expansionDao: ExpansionDao by lazy { database.expansionDao() }

    private fun prePopulateDatabase() {

        applicationScope.launch {
            // Load and insert cards
            val cards = loadCardsFromAssets(applicationContext)
            cardDao.insertAll(cards)

            // Load and insert expansions
            val expansions = loadExpansionsFromAssets(applicationContext)
            expansionDao.insertAll(expansions)
        }
    }
}