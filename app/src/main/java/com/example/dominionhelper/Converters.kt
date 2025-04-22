// Converters.kt
package com.example.dominionhelper

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromSet(value: Set): String {
        return value.name
    }

    @TypeConverter
    fun toSet(value: String): Set {
        return Set.valueOf(value)
    }

    @TypeConverter
    fun fromTypeList(value: List<Type>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTypeList(value: String): List<Type> {
        val gson = Gson()
        val type = object : TypeToken<List<Type>>() {}.type
        return gson.fromJson(value, type)
    }

}