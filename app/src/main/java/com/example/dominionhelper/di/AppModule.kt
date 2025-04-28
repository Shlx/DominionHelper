package com.example.dominionhelper.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dominionhelper.KingdomGenerator
import com.example.dominionhelper.data.Card
import com.example.dominionhelper.data.Expansion
import com.example.dominionhelper.R
import com.example.dominionhelper.data.AppDatabase
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.ExpansionDao
import com.example.dominionhelper.data.DominionRepository
import com.example.dominionhelper.data.DominionRepositoryImpl
import com.example.dominionhelper.data.loadCardsFromAssets
import com.example.dominionhelper.data.loadExpansionsFromAssets
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        app: Application,
        coroutineScope: CoroutineScope
    ): AppDatabase {
        val databaseName = app.getString(R.string.database_name)

        return Room.databaseBuilder(
            app.applicationContext,
            AppDatabase::class.java,
            databaseName
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    coroutineScope.launch {
                        prePopulateDatabase(
                            app,
                            provideCardDao(provideAppDatabase(app, coroutineScope)),
                            provideExpansionDao(provideAppDatabase(app, coroutineScope))
                        )
                    }
                }
            })
            .build()
    }

    @Provides
    fun provideCardDao(appDatabase: AppDatabase): CardDao {
        return appDatabase.cardDao()
    }

    @Provides
    fun provideExpansionDao(appDatabase: AppDatabase): ExpansionDao {
        return appDatabase.expansionDao()
    }

    @Provides
    @Singleton
    fun provideKingdomGenerator(cardDao: CardDao): KingdomGenerator {
        return KingdomGenerator(cardDao)
    }

    @Provides
    fun provideDominionRepository(
        cardDao: CardDao,
        expansionDao: ExpansionDao
    ): DominionRepository {
        return DominionRepositoryImpl(cardDao, expansionDao)
    }

    private suspend fun prePopulateDatabase(
        app: Application,
        cardDao: CardDao,
        expansionDao: ExpansionDao
    ) {

        // Load and insert cards
        val cards: List<Card> = loadCardsFromAssets(app.applicationContext)
        cardDao.insertAll(cards)

        // Load and insert expansions
        val expansions: List<Expansion> = loadExpansionsFromAssets(app.applicationContext)
        expansionDao.insertAll(expansions)
    }
}