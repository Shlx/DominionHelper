// Converters.kt
package com.example.dominionhelper

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromType(value: List<GameCard.Type>): String {
        val gson = Gson()
        val type = object : TypeToken<List<GameCard.Type>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toType(value: String): List<GameCard.Type> {
        val gson = Gson()
        val type = object : TypeToken<List<GameCard.Type>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromEffect(value: List<GameCard.Effect>): String {
        val gson = Gson()
        val type = object : TypeToken<List<GameCard.Effect>>() {}.type
        return gson.toJson(value, type)
    }
    @TypeConverter
    fun toEffect(value: String): List<GameCard.Effect> {
        val gson = Gson()
        val type = object : TypeToken<List<GameCard.Effect>>() {}.type
        return gson.fromJson(value, type)
    }
}