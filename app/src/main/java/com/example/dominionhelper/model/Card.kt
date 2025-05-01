package com.example.dominionhelper.model

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlin.text.uppercase
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken


@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val set: Set,
    val cost: Int,
    val supply: Boolean,
    val landscape: Boolean,
    val types: List<Type>,
    @SerializedName("image_name") val imageName: String,
    val basic: Boolean,
    val debt: Int,
    val categories: List<Category>,
    val potion: Boolean,
) {

    @Ignore
    var expansionImageId: Int = set.imageId

    fun getColorByTypes(): List<Color> {
        val colors = mutableListOf<Color>()
        if (types.contains(Type.TREASURE)) {
            colors.add(Color(0xFFF7DC7E))
        }
        if (types.contains(Type.DURATION)) {
            colors.add(Color(0xFFE78845))
        }
        if (types.contains(Type.REACTION)) {
            colors.add(Color(0xFF67AAD9))
        }
        if (types.contains(Type.RESERVE)) {
            colors.add(Color(0xFFD7BC86))
        }
        if (types.contains(Type.VICTORY)) {
            colors.add(Color(0xFFA2CB85))
        }
        if (types.contains(Type.CURSE)) {
            colors.add(Color(0xFFB18EBC))
        }
        if (types.contains(Type.RUINS)) {
            colors.add(Color(0xFF875F3C))
        }
        if (types.contains(Type.NIGHT)) {
            colors.add(Color(0xFF535353))
        }
        if (types.contains(Type.SHELTER)) {
            if (types.contains(Type.ACTION)) {
                colors.add(Color(0xFFF3EEE2))
            }
            colors.add(Color(0xFFEF876F))
        }

        // Default color
        if (colors.isEmpty()) {
            colors.add(Color(0xFFF3EEE2))
        }
        return colors
    }
}

// To data package
fun loadCardsFromAssets(context: Context): List<Card> {
    val jsonString: String
    try {
        val inputStream = context.assets.open("cards.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        jsonString = String(buffer, Charsets.UTF_8)
    } catch (e: IOException) {
        Log.e("loadCardsFromAssets", "Error reading from assets", e)
        return emptyList()
    }

    val gson = GsonBuilder()
        .registerTypeAdapter(Set::class.java, SetTypeAdapter())
        .registerTypeAdapter(Type::class.java, TypeTypeAdapter())
        .registerTypeAdapter(Category::class.java, CategoryTypeAdapter())
        .create()

    val cardListType = object : TypeToken<List<Card>>() {}.type
    val cardList: List<Card> = gson.fromJson(jsonString, cardListType)
    return cardList
}

class SetTypeAdapter : TypeAdapter<Set>() {

    override fun write(out: JsonWriter, value: Set?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name) // Write as the enum name (e.g., "BASE")
        }
    }

    override fun read(reader: JsonReader): Set? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val value = reader.nextString()
        return Set.valueOf(value.uppercase()) // Convert from string to Set enum
    }
}

class TypeTypeAdapter : TypeAdapter<Type>() {
    override fun write(out: JsonWriter, value: Type?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name) // Write as the enum name (e.g., "ACTION")
        }
    }

    override fun read(reader: JsonReader): Type? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val value = reader.nextString()
        return Type.valueOf(value.uppercase()) // Convert from string to Type enum
    }
}

class CategoryTypeAdapter : TypeAdapter<Category>() {
    override fun write(out: JsonWriter, value: Category?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.name)
        }
    }

    override fun read(reader: JsonReader): Category? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val value = reader.nextString()
        return Category.valueOf(value.uppercase()) // Convert from string to Category enum
    }
}