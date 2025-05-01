package com.example.dominionhelper.utils

import androidx.room.TypeConverter
import com.example.dominionhelper.model.Category
import com.example.dominionhelper.model.Set
import com.example.dominionhelper.model.Type
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

    @TypeConverter
    fun fromCategoryList(value: List<Category>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toCategoryList(value: String): List<Category> {
        val type = object : TypeToken<List<Category>>() {}.type
        return gson.fromJson(value, type)
    }

}