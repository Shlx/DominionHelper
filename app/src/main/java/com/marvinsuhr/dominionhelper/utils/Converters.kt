package com.marvinsuhr.dominionhelper.utils

import androidx.room.TypeConverter
import com.marvinsuhr.dominionhelper.model.Category
import com.marvinsuhr.dominionhelper.model.Set
import com.marvinsuhr.dominionhelper.model.Type
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

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
    fun fromSetList(value: List<Set>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSetList(value: String): List<Set> {
        val set = object : TypeToken<List<Set>>() {}.type
        return gson.fromJson(value, set)
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