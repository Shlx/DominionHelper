package com.marvinsuhr.dominionhelper.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kingdoms")
data class KingdomEntity(
    @PrimaryKey val uuid: String,
    val creationTimeStamp: Long,
    val randomCardIds: List<Int>,
    val landscapeCardIds: List<Int>,
    val isFavorite: Boolean,
    val name: String
)
