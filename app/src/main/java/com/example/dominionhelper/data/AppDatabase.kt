package com.example.dominionhelper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dominionhelper.utils.Converters
import com.example.dominionhelper.model.Card
import com.example.dominionhelper.model.Expansion

@Database(
    entities = [
        Card::class,
        Expansion::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun expansionDao(): ExpansionDao
}