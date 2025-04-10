package com.example.dominionhelper

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable

data class GameCard (
    val name: String,
    val expansion: Expansion,
    val types: List<Type>,
    val effects: List<Effect>,
    val cost: Int,
    @DrawableRes val imageResId: Int,
    val onClick: @Composable () -> Unit) {

    enum class Expansion {
        BASE,
        INTRIGUE,
        SEASIDE
    }

    enum class Type {
        ACTION,
        ATTACK,
        REACTION,
        DURATION,
        VICTORY
    }

    enum class Effect {
        ACTION,
        CARD,
        GOLD,
        BUY,
        POINTS
    }

}