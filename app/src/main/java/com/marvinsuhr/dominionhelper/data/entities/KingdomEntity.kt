package com.marvinsuhr.dominionhelper.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kingdoms")
data class KingdomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val randomCardIds: List<Int>,
    val landscapeCardIds: List<Int>
)
