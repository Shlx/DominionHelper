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
            "dominion_helper_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    //Pre populate the database here
                    prePopulateDatabase()
                }
            })
            .build()
    }

    // DAOs
    val gameCardDao: GameCardDao by lazy { database.gameCardDao() }
    val expansionDao: ExpansionDao by lazy { database.expansionDao() }

    // TODO: pre-packaged database
    private fun prePopulateDatabase() {

        applicationScope.launch {

            // Create Base expansion cards
            val baseCards = listOf(
                GameCard(
                    0,
                    "Village",
                    GameCard.Expansion.BASE.id,
                    listOf(GameCard.Type.ACTION),
                    listOf(GameCard.Effect.ACTION, GameCard.Effect.CARD),
                    3,
                    R.drawable.village,
                    "a",
                    false,
                    false
                ),
                GameCard(
                    1,
                    "Gardens",
                    GameCard.Expansion.BASE.id,
                    listOf(GameCard.Type.VICTORY),
                    emptyList(),
                    4,
                    R.drawable.gardens,
                    "a",
                    false,
                    false
                ),
                GameCard(
                    2,
                    "Market",
                    GameCard.Expansion.BASE.id,
                    listOf(GameCard.Type.ACTION),
                    listOf(
                        GameCard.Effect.ACTION,
                        GameCard.Effect.CARD,
                        GameCard.Effect.BUY,
                        GameCard.Effect.GOLD
                    ),
                    5,
                    R.drawable.market,
                    "a",
                    false,
                    false
                ),
                GameCard(
                    3,
                    "Smithy",
                    GameCard.Expansion.BASE.id,
                    listOf(GameCard.Type.ACTION),
                    listOf(GameCard.Effect.CARD),
                    4,
                    R.drawable.smithy,
                    "a",
                    false,
                    false
                )

            )

            // Create Intrigue expansion cards
            val intrigueCards = listOf(
                GameCard(
                    4,
                    "Courtyard",
                    GameCard.Expansion.INTRIGUE.id,
                    listOf(GameCard.Type.ACTION),
                    listOf(GameCard.Effect.CARD),
                    2,
                    R.drawable.village,
                    "a",
                    false,
                    false
                ),
                GameCard(
                    5,
                    "Nobles",
                    GameCard.Expansion.INTRIGUE.id,
                    listOf(GameCard.Type.ACTION, GameCard.Type.VICTORY),
                    listOf(GameCard.Effect.ACTION, GameCard.Effect.CARD),
                    6,
                    R.drawable.village,
                    "a",
                    false,
                    false
                ),
                GameCard(
                    6,
                    "Pawn",
                    GameCard.Expansion.INTRIGUE.id,
                    listOf(GameCard.Type.ACTION),
                    listOf(
                        GameCard.Effect.ACTION,
                        GameCard.Effect.CARD,
                        GameCard.Effect.BUY,
                        GameCard.Effect.GOLD
                    ),
                    2,
                    R.drawable.village,
                    "a",
                    false,
                    false
                )
            )
            gameCardDao.insertAll(baseCards + intrigueCards)

            // Create expansions
            val baseExpansion = Expansion(
                0,
                "Base",
                imageResId = R.drawable.dominion,
                isFavorite = false,
                isOwned = false
            )

            val intrigueExpansion = Expansion(
                1,
                "Intrigue",
                imageResId = R.drawable.intrigue,
                isFavorite = false,
                isOwned = false
            )
            expansionDao.insertAll(listOf(baseExpansion, intrigueExpansion))
        }
    }
}