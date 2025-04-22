package com.example.dominionhelper

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlin.text.uppercase
import com.google.gson.stream.JsonWriter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken


@Entity(tableName = "card")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val set: Set,
    val cost: Int,
    val supply: Boolean,
    val landscape: Boolean,
    val types: List<Type>,
    @SerializedName("image_name") val imageName: String
)

enum class Set {
    BASE,
    BASE_1E,
    BASE_2E,
    INTRIGUE,
    INTRIGUE_1E,
    INTRIGUE_2E,
    SEASIDE,
    SEASIDE_1E,
    SEASIDE_2E,
    ALCHEMY,
    PROSPERITY,
    PROSPERITY_1E,
    PROSPERITY_2E,
    CORNUCOPIA,
    HINTERLANDS,
    HINTERLANDS_1E,
    HINTERLANDS_2E,
    DARK_AGES,
    GUILDS,
    ADVENTURES,
    EMPIRES,
    NOCTURNE,
    RENAISSANCE,
    MENAGERIE,
    ALLIES,
    PLUNDER,
    RISING_SUN,
    PROMO,
    CORNUCOPIA_GUILDS,
    CORNUCOPIA_GUILDS_1E,
    CORNUCOPIA_GUILDS_2E
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
        return Type.valueOf(value.uppercase()) // Convert from string to Set enum
    }
}

@Dao
interface CardDao {

    @Query("SELECT * FROM card")
    suspend fun getAll(): List<Card>

    @Query("SELECT * FROM card WHERE name LIKE :filter")
    suspend fun getFilteredCards(filter: String): List<Card>

    @Query("SELECT * FROM card ORDER BY RANDOM() LIMIT :amount")
    suspend fun getRandomCards(amount: Int): List<Card>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<Card>)
}