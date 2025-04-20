package com.example.dominionhelper

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Expansion::class, GameCard::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expansionDao(): ExpansionDao
    abstract fun gameCardDao(): GameCardDao
}