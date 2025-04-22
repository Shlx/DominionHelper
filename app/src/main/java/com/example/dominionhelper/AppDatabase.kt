package com.example.dominionhelper

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Card::class, Expansion::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun expansionDao(): ExpansionDao
}