package com.marvinsuhr.dominionhelper.model

import com.marvinsuhr.dominionhelper.R

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
    CORNUCOPIA(R.drawable.set_cornucopia),
    CORNUCOPIA_GUILDS_2E(R.drawable.set_cornucopia_guilds_2e),
    HINTERLANDS_1E(R.drawable.set_hinterlands_1e),
    HINTERLANDS_2E(R.drawable.set_hinterlands_2e),
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
    PLACEHOLDER() // TODO
}

enum class Type(val displayText: String? = null) { // Add nullable displayText property
    ACTION,
    ATTACK,
    REACTION,
    CURSE,
    DURATION,
    TREASURE,
    VICTORY,
    NIGHT,
    PRIZE("Prize"),
    RUINS("Ruins"),
    TRAVELLER("Traveller"),
    GATHERING("Gathering"),
    LIAISON("Liaison"),
    DOOM("Doom"),
    HEIRLOOM("Heirloom"),
    CASTLE("Castle"),
    SHELTER("Shelter"),
    SPIRIT("Spirit"),
    FATE("Fate"),
    REWARD("Reward"),
    COMMAND,
    LOOTER("Looter"),
    KNIGHT("Knight"),
    RESERVE("Reserve"),
    EVENT("Event"),
    LANDMARK("Landmark"),
    ZOMBIE,
    BOON("Boon"),
    HEX("Hex"),
    STATE("State"),
    PROJECT("Project"),
    PLUNDER("Plunder"),
    TRAIT("Trait"),
    SHADOW("Shadow"),
    LOOT("Loot"),
    ALLY("Ally"),
    TOWNSFOLK("Townsfolk"),
    ODYSSEY("Odyssey"),
    CLASH("Clash"),
    FORT("Fort"),
    WIZARD("Wizard"),
    AUGUR("Augur"),
    WAY("Way"),
    ARTIFACT("Artifact"),
    OMEN("Omen"),
    PROPHECY("Prophecy")
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