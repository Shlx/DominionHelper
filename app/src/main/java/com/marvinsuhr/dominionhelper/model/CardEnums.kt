package com.marvinsuhr.dominionhelper.model

import com.marvinsuhr.dominionhelper.R
import com.marvinsuhr.dominionhelper.ui.KingdomViewModel
import com.marvinsuhr.dominionhelper.ui.LibraryViewModel

enum class Set (val imageId: Int = R.drawable.ic_launcher_foreground) {
    BASE_1E(R.drawable.set_dominion_1e),
    BASE_2E(R.drawable.set_dominion_2e),
    INTRIGUE_1E(R.drawable.set_intrigue_1e),
    INTRIGUE_2E(R.drawable.set_intrigue_2e),
    SEASIDE_1E(R.drawable.set_seaside_1e),
    SEASIDE_2E(R.drawable.set_seaside_2e),
    ALCHEMY(R.drawable.set_alchemy),
    PROSPERITY_1E(R.drawable.set_prosperity_1e),
    PROSPERITY_2E(R.drawable.set_prosperity_2e),
    CORNUCOPIA_1E(R.drawable.set_cornucopia),
    CORNUCOPIA_GUILDS_2E(R.drawable.set_cornucopia_guilds_2e),
    HINTERLANDS_1E(R.drawable.set_hinterlands_1e),
    HINTERLANDS_2E(R.drawable.set_hinterlands_2e),
    DARK_AGES(R.drawable.set_dark_ages),
    GUILDS_1E(R.drawable.set_guilds),
    ADVENTURES(R.drawable.set_adventures),
    EMPIRES(R.drawable.set_empires),
    NOCTURNE(R.drawable.set_nocturne),
    RENAISSANCE(R.drawable.set_renaissance),
    MENAGERIE(R.drawable.set_menagerie),
    ALLIES(R.drawable.set_allies),
    PLUNDER(R.drawable.set_plunder),
    RISING_SUN(R.drawable.set_rising_sun),
    PROMO(R.drawable.set_promo),
    PLACEHOLDER() // TODO
}

enum class Type(
    val sortPriority: Int = Int.MAX_VALUE,
    val displayText: String? = null
) {
    // Basic Types (sortPriority here is only needed for base game)
    TREASURE(sortPriority = 100, displayText = "Treasure"),
    VICTORY(sortPriority = 101, displayText = "Victory"),
    CURSE(sortPriority = 102, displayText = "Curse"),
    ACTION(displayText = "Action"),
    ATTACK(displayText = "Attack"),
    REACTION(displayText = "Reaction"),
    DURATION(displayText = "Duration"),
    NIGHT(displayText = "Night"),
    COMMAND(displayText = "Command"),

    // --- Non-Landscape Card Types ---
    PRIZE(sortPriority = 0, displayText = "Prize"),             // Cornucopia 1E
    KNIGHT(sortPriority = 1, displayText = "Knight"),           // Dark Ages
    LOOTER(sortPriority = 2, displayText = "Looter"),           // Dark Ages
    RUINS(sortPriority = 3, displayText = "Ruins"),             // Dark Ages
    SHELTER(sortPriority = 4, displayText = "Shelter"),         // Dark Ages
    RESERVE(sortPriority = 6, displayText = "Reserve"),         // Adventures
    TRAVELLER(sortPriority = 5, displayText = "Traveller"),     // Adventures
    CASTLE(sortPriority = 7, displayText = "Castle"),           // Empires
    GATHERING(sortPriority = 8, displayText = "Gathering"),     // Empires
    DOOM(sortPriority = 9, displayText = "Doom"),               // Nocturne
    FATE(sortPriority = 10, displayText = "Fate"),              // Nocturne
    HEIRLOOM(sortPriority = 11, displayText = "Heirloom"),      // Nocturne
    SPIRIT(sortPriority = 12, displayText = "Spirit"),          // Nocturne
    ZOMBIE(sortPriority = 13, displayText = "Zombie"),          // Nocturne
    AUGUR(sortPriority = 14, displayText = "Augur"),            // Allies
    CLASH(sortPriority = 15, displayText = "Clash"),            // Allies
    FORT(sortPriority = 16, displayText = "Fort"),              // Allies
    ODYSSEY(sortPriority = 17, displayText = "Odyssey"),        // Allies
    TOWNSFOLK(sortPriority = 18, displayText = "Townsfolk"),    // Allies
    WIZARD(sortPriority = 19, displayText = "Wizard"),          // Allies
    LIAISON(sortPriority = 20, displayText = "Liaison"),        // Allies
    LOOT(sortPriority = 21, displayText = "Loot"),              // Plunder
    REWARD(sortPriority = 22, displayText = "Reward"),          // Cornucopia & Guilds 2E -> Move up?
    OMEN(sortPriority = 23, displayText = "Omen"),              // Rising Sun
    SHADOW(sortPriority = 24, displayText = "Shadow"),          // Rising Sun

    // --- Landscape Card Types ---
    EVENT(sortPriority = 25, displayText = "Event"),            // Adventures
    LANDMARK(sortPriority = 26, displayText = "Landmark"),      // Empires
    BOON(sortPriority = 27, displayText = "Boon"),              // Nocturne
    HEX(sortPriority = 28, displayText = "Hex"),                // Nocturne
    STATE(sortPriority = 29, displayText = "State"),            // Nocturne
    ARTIFACT(sortPriority = 30, displayText = "Artifact"),      // Renaissance
    PROJECT(sortPriority = 31, displayText = "Project"),        // Renaissance
    WAY(sortPriority = 32, displayText = "Way"),                // Menagerie
    ALLY(sortPriority = 33, displayText = "Ally"),              // Allies
    TRAIT(sortPriority = 34, displayText = "Trait"),            // Plunder
    PROPHECY(sortPriority = 35, displayText = "Prophecy"),      // Rising Sun
}

enum class Category {
    CANTRIP,
    NONTERMINAL_DRAW,
    TERMINAL_DRAW,
    CURSER,
    NONTERMINAL, // Skipped: Cantrips, Villages, Non-terminal draw, peddlers, disappearing money
    TERMINAL, // Skipped: Way too many cards
    THRONEROOM_VARIANT,
    VILLAGE,
    TRASHER,
    ALT_VP,
    PLUSBUY,
    DECK_INSPECTOR,
    TRASH_FOR_BENEFIT,
    HANDSIZE_ATTACK,
    // ^done
    DIGGING,
    DISCARD, // For benefit
    SIFTERS, // ?
    COST_REDUCTION,
    DISAPPEARING_MONEY,
    PEDDLER,
    TERMINAL_SILVER, // ?
    VIRTUAL_COIN,
    VIRTUAL_BUY, // ?
    ATTACK_IMMUNITY,
    DECK_INSPECTION,
    DECK_ORDER_ATTACK,
    JUNKING_ATTACK,
    TRASHING_ATTACK,
    TURN_WORSENING_ATTACK, // ?
    DURATION_DRAW,
    COMMAND_VARIANT,
    GAINER,
    NON_ATTACK_INTERACTION,
    ONE_SHOT,
    REMODELER,
    SPLIT_PILE,
    TOP_DECKER,
    VANILLA, // ?
    EXTRA_TURN // ?
}

enum class CardDisplayCategory {
    SUPPLY, // Normal kingdom cards
    SPECIAL, // Additional cards dependent on other cards
    LANDSCAPE // Landscape cards
}

sealed class AppSortType(val text: String) {
    data class Kingdom(val sortType: KingdomViewModel.SortType) : AppSortType(sortType.text)
    data class Library(val sortType: LibraryViewModel.SortType) : AppSortType(sortType.text)
}