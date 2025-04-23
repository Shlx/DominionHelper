package com.example.dominionhelper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dominionhelper.Card
import com.example.dominionhelper.CardCategory
import com.example.dominionhelper.CardCategoryCrossRef
import com.example.dominionhelper.Converters
import com.example.dominionhelper.Expansion

@Database(
    entities = [
        Card::class,
        Expansion::class,
        CardCategory::class,
        CardCategoryCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun expansionDao(): ExpansionDao
}