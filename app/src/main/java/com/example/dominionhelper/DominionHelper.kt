package com.example.dominionhelper

import android.app.Application
import android.util.Log
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.ExpansionDao
import com.example.dominionhelper.model.Card
import com.example.dominionhelper.model.Expansion
import com.example.dominionhelper.model.loadCardsFromAssets
import com.example.dominionhelper.model.loadExpansionsFromAssets
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DominionHelper : Application() {

    @Inject
    lateinit var cardDao: CardDao

    @Inject
    lateinit var expansionDao: ExpansionDao

    @Inject
    lateinit var applicationScope: CoroutineScope // Inject the application scope

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {

            if (cardDao.count() == 0 && expansionDao.count() == 0) {
                Log.i("Application", "Pre-populating database...")

                // Load and insert cards
                val cards: List<Card> = loadCardsFromAssets(applicationContext)
                cardDao.insertAll(cards)

                // Load and insert expansions
                val expansions: List<Expansion> = loadExpansionsFromAssets(applicationContext)
                expansionDao.insertAll(expansions)

                Log.i("Application", "Database pre-populated.")
            }
        }
    }
}