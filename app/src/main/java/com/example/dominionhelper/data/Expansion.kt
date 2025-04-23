package com.example.dominionhelper.data

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.IOException

@Entity(tableName = "expansions")
data class Expansion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val set: Set,
    val name: String,
    @SerializedName("image_name") val imageName: String,
    val isOwned: Boolean = false
)

// To data package
fun loadExpansionsFromAssets(context: Context): List<Expansion> {
    val jsonString: String
    try {
        val inputStream = context.assets.open("sets.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        jsonString = String(buffer, Charsets.UTF_8)
    } catch (e: IOException) {
        Log.e("loadExpansionsFromAssets", "Error reading from assets", e)
        return emptyList()
    }

    val gson = GsonBuilder()
        .registerTypeAdapter(Set::class.java, SetTypeAdapter())
        .create()

    val expansionListType = object : TypeToken<List<Expansion>>() {}.type
    val expansionList: List<Expansion> = gson.fromJson(jsonString, expansionListType)
    return expansionList
}
