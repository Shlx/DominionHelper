package com.example.dominionhelper.model

import android.content.Context
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.IOException

data class ExpansionWithEditions(
    val name: String, // The name of the Expansion (e.g., "Base", "Intrigue")
    val firstEdition: Expansion? = null,
    val secondEdition: Expansion? = null,
    val image: String,
    val isExpanded: Boolean = false
)

enum class OwnedEdition() {
    NONE,
    FIRST,
    SECOND,
    BOTH
}

@Entity(tableName = "expansions")
data class Expansion(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val edition: Int,
    @SerializedName("image_name") val imageName: String,
    val isOwned: Boolean,
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
