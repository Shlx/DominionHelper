package com.example.dominionhelper.data

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.dominionhelper.R
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
    val categories: List<Category>
) {

    @Ignore
    var expansionImageId: Int = set.imageId

    fun getColorByTypes(): List<Color> {
        val list = mutableListOf<Color>()
        if (types.contains(Type.TREASURE)) {
            list.add(Color(0xFFF7DC7E))
        }
        if (types.contains(Type.DURATION)) {
            list.add(Color(0xFFE78845))
        }
        if (types.contains(Type.REACTION)) {
            list.add(Color(0xFF67AAD9))
        }
        if (types.contains(Type.RESERVE)) {
            list.add(Color(0xFFD7BC86))
        }
        if (types.contains(Type.VICTORY)) {
            list.add(Color(0xFFA2CB85))
        }
        if (types.contains(Type.CURSE)) {
            list.add(Color(0xFFB18EBC))
        }
        if (types.contains(Type.RUINS)) {
            list.add(Color(0xFF875F3C))
        }
        if (types.contains(Type.NIGHT)) {
            list.add(Color(0xFF535353))
        }
        if (types.contains(Type.SHELTER)) {
            if (types.contains(Type.ACTION)) {
                list.add(Color(0xFFF3EEE2))
            }
            list.add(Color(0xFFEF876F))
        }

        // Default color
        if (list.isEmpty()) {
            list.add(Color(0xFFF3EEE2))
        }
        return list
    }
}

enum class Set (val imageId: Int = R.drawable.ic_launcher_foreground) {
    BASE(R.drawable.set_unknown),
    BASE_1E(R.drawable.set_dominion_1e),
    BASE_2E(R.drawable.set_dominion_2e),
    INTRIGUE(R.drawable.set_unknown),
    INTRIGUE_1E(R.drawable.set_intrigue_1e),
    INTRIGUE_2E(R.drawable.set_intrigue_2e),
    SEASIDE(R.drawable.set_unknown),
    SEASIDE_1E(R.drawable.set_seaside_1e),
    SEASIDE_2E(R.drawable.set_seaside_1e),
    ALCHEMY(R.drawable.set_alchemy),
    PROSPERITY(R.drawable.set_prosperity),
    PROSPERITY_1E(R.drawable.set_prosperity),
    PROSPERITY_2E(R.drawable.set_prosperity),
    CORNUCOPIA(R.drawable.set_cornucopia),
    HINTERLANDS(R.drawable.set_hinterlands),
    HINTERLANDS_1E(R.drawable.set_hinterlands),
    HINTERLANDS_2E(R.drawable.set_hinterlands),
    DARK_AGES(R.drawable.set_dark_ages),
    GUILDS(R.drawable.set_guilds),
    ADVENTURES(R.drawable.set_adventures),
    EMPIRES(R.drawable.set_empires),
    NOCTURNE(R.drawable.set_nocturne),
    RENAISSANCE(R.drawable.set_renaissance),
    MENAGERIE(R.drawable.set_menagerie),
    ALLIES(R.drawable.set_allies),
    PLUNDER(R.drawable.set_plunder),
    RISING_SUN(R.drawable.set_rising_sun),
    PROMO(R.drawable.set_promo),
    CORNUCOPIA_GUILDS(R.drawable.set_unknown),
    CORNUCOPIA_GUILDS_1E(R.drawable.set_unknown),
    CORNUCOPIA_GUILDS_2E(R.drawable.set_unknown)
}

enum class Type {
    ACTION,
    ATTACK,
    REACTION,
    CURSE,
    DURATION,
    TREASURE,
    VICTORY,
    NIGHT,
    PRIZE,
    RUINS,
    TRAVELLER,
    GATHERING,
    LIAISON,
    DOOM,
    HEIRLOOM,
    CASTLE,
    SHELTER,
    SPIRIT,
    FATE,
    REWARD,
    COMMAND,
    LOOTER,
    KNIGHT,
    RESERVE,
    EVENT,
    LANDMARK,
    ZOMBIE,
    BOON,
    HEX,
    STATE,
    PROJECT,
    PLUNDER,
    TRAIT,
    SHADOW,
    LOOT,
    ALLY,
    TOWNSFOLK,
    ODYSSEY,
    CLASH,
    FORT,
    WIZARD,
    AUGUR,
    WAY,
    ARTIFACT,
    OMEN,
    PROPHECY
}

enum class Category {
    CANTRIP,
    NONTERMINAL_DRAW,
    TERMINAL_DRAW
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