package com.marvinsuhr.dominionhelper.utils

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marvinsuhr.dominionhelper.CurrentScreen
import com.marvinsuhr.dominionhelper.ui.DarkAgesMode
import com.marvinsuhr.dominionhelper.ui.ProsperityMode
import com.marvinsuhr.dominionhelper.ui.RandomMode
import com.marvinsuhr.dominionhelper.ui.VetoMode

object Constants {

    // UI TODO - SORT
    val PADDING_MINI = 4.dp
    val PADDING_SMALL = 8.dp
    val PADDING_MEDIUM = 16.dp
    val ICON_SIZE = 35.dp
    // 72 or 88 would be preferable. 80 is used so that the dimensions of card artworks fit well
    val CARD_HEIGHT = 80.dp
    val COLOR_BAR_WIDTH = 8.dp
    val IMAGE_ROUNDED = 16.dp
    val CARD_IMAGE_WIDTH = 85.dp
    val CARD_NAME_FONT_SIZE = 20.sp
    val TEXT_SMALL = 16.sp
    val CARD_PRICE_SIZE = 24.dp
    val CARD_DEBT_SIZE = 26.dp

    // PREFERENCES
    const val USER_PREFERENCES_NAME = "settings_pref"

    // -> UserPrefsRepo?
    val DEFAULT_RANDOM_MODE = RandomMode.FULL_RANDOM
    const val DEFAULT_RANDOM_EXPANSION_AMOUNT = 2
    val DEFAULT_VETO_MODE = VetoMode.REROLL_SAME
    const val DEFAULT_NUMBER_OF_CARDS_TO_GENERATE = 10
    const val DEFAULT_LANDSCAPE_CATEGORIES = 2
    const val DEFAULT_LANDSCAPE_DIFFERENT_CATEGORIES = true
    val DEFAULT_DARK_AGES_STARTER_CARDS = DarkAgesMode.TEN_PERCENT_PER_CARD
    val DEFAULT_PROSPERITY_BASIC_CARDS = ProsperityMode.TEN_PERCENT_PER_CARD

    const val DEFAULT_IS_DARK_MODE = false

    val START_DESTINATION = CurrentScreen.Kingdoms

}