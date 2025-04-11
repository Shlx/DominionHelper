package com.example.dominionhelper

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Expansion::class, GameCard::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expansionDao(): ExpansionDao
    abstract fun gameCardDao(): GameCardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(AppDatabaseCallback(scope)) // Add the callback here
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.gameCardDao(),database.expansionDao())
                }
            }
        }

        suspend fun populateDatabase(gameCardDao: GameCardDao, expansionDao: ExpansionDao) {
            // Clear any existing data (optional, but often useful for development)
            gameCardDao.getAll().forEach{gameCardDao.delete()}
            expansionDao.getAll().forEach{expansionDao.delete()}

            // Create Base expansion cards
            val baseCards = listOf(
                GameCard(0, "Village", GameCard.Expansion.BASE.id, listOf(GameCard.Type.ACTION), listOf(GameCard.Effect.ACTION, GameCard.Effect.CARD), 3,R.drawable.village, false,false),
                GameCard(1, "Gardens", GameCard.Expansion.BASE.id, listOf(GameCard.Type.VICTORY), emptyList(), 4,R.drawable.gardens, false,false),
                GameCard(2, "Market", GameCard.Expansion.BASE.id, listOf(GameCard.Type.ACTION), listOf(GameCard.Effect.ACTION,GameCard.Effect.CARD,GameCard.Effect.BUY,GameCard.Effect.GOLD), 5,R.drawable.market, false,false),
                GameCard(3, "Smithy", GameCard.Expansion.BASE.id, listOf(GameCard.Type.ACTION), listOf(GameCard.Effect.CARD), 4,R.drawable.smithy, false,false)

            )

            // Create Intrigue expansion cards
            val intrigueCards = listOf(
                GameCard(4, "Courtyard", GameCard.Expansion.INTRIGUE.id, listOf(GameCard.Type.ACTION), listOf(GameCard.Effect.CARD), 2, R.drawable.village, false,false),
                GameCard(5, "Nobles", GameCard.Expansion.INTRIGUE.id, listOf(GameCard.Type.ACTION,GameCard.Type.VICTORY), listOf(GameCard.Effect.ACTION,GameCard.Effect.CARD), 6,R.drawable.village, false,false),
                GameCard(6, "Pawn", GameCard.Expansion.INTRIGUE.id, listOf(GameCard.Type.ACTION), listOf(GameCard.Effect.ACTION,GameCard.Effect.CARD,GameCard.Effect.BUY,GameCard.Effect.GOLD), 2,R.drawable.village, false,false)
            )
            gameCardDao.insertAll(baseCards+intrigueCards)

            // Create expansions
            val baseExpansion = Expansion(0, "Base", imageResId = R.drawable.dominion, isFavorite = false, isOwned = false)
            val intrigueExpansion = Expansion(1, "Intrigue", imageResId = R.drawable.intrigue, isFavorite = false, isOwned = false)
            expansionDao.insertAll(listOf(baseExpansion, intrigueExpansion))
        }
    }
}