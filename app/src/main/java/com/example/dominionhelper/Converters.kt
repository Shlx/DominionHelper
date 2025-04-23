package com.example.dominionhelper

import androidx.room.TypeConverter
import com.example.dominionhelper.data.Set
import com.example.dominionhelper.data.Type
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

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
        return gson.toJson(value)
    }

    @TypeConverter
    fun toTypeList(value: String): List<Type> {
        val type = object : TypeToken<List<Type>>() {}.type
        return gson.fromJson(value, type)
    }

}