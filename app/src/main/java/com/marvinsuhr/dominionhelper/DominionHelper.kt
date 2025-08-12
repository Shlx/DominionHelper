package com.marvinsuhr.dominionhelper

import android.app.Application
import android.util.Log
import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.data.ExpansionDao
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.model.Expansion
import com.marvinsuhr.dominionhelper.model.loadCardsFromAssets
import com.marvinsuhr.dominionhelper.model.loadExpansionsFromAssets
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