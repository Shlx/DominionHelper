package com.example.dominionhelper.di

import android.app.Application
import androidx.room.Room
import com.example.dominionhelper.KingdomGenerator
import com.example.dominionhelper.R
import com.example.dominionhelper.data.AppDatabase
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.ExpansionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
        app: Application
    ): AppDatabase {
        val databaseName = app.getString(R.string.database_name)

        return Room.databaseBuilder(
            app.applicationContext,
            AppDatabase::class.java,
            databaseName
        ).build()
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
}