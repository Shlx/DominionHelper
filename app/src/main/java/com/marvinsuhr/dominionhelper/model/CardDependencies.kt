package com.marvinsuhr.dominionhelper.model

class CardDependencies {

    // Data class to represent a dependency rule
    data class DependencyRule(
        val condition: (Card) -> Boolean,
        val dependentCardNames: List<String>
    )

    val dependencyRules = listOf(

        // TODO Schwierig: Ferryman, Young Witch, Black Market, Riverboat, Approaching Army, Divine Wind, Inherited, Way of the Mouse
        // -> Data driven? Store dependencies in db?

        // If there is a Curser present, add Curse card
        DependencyRule(
            condition = { it.categories.contains(Category.CURSER) },
            dependentCardNames = listOf(CardNames.CURSE)
        ),

        // If there is an Alchemy card present, add Potion
        DependencyRule(
            condition = { it.sets.contains(Set.ALCHEMY) },
            dependentCardNames = listOf(CardNames.POTION)
        ),

        // If there is a Fate card present, add all Boons
        DependencyRule(
            condition = { it.types.contains(Type.FATE) },
            dependentCardNames = listOf(CardNames.BOON_PILE, CardNames.WILL_O_WISP)
        ),

        // If there is a Doom card present, add all Hexes and corresponding States
        DependencyRule(
            condition = { it.types.contains(Type.DOOM) },
            dependentCardNames = listOf(
                CardNames.HEX_PILE,
                CardNames.CURSE,
                CardNames.DELUDED,
                CardNames.ENVIOUS,
                CardNames.MISERABLE,
                CardNames.TWICE_MISERABLE // -> State Pile
            )
        ),

        // If there is a card present that rewards loot, add all Loots
        DependencyRule(
            condition = { card ->
                listOf(
                    CardNames.JEWELLED_EGG,
                    CardNames.PERIL,
                    CardNames.SEARCH,
                    CardNames.FORAY,
                    CardNames.PICKAXE,
                    CardNames.WEALTHY_VILLAGE,
                    CardNames.CUTTHROAT,
                    CardNames.LOOTING,
                    CardNames.SACK_OF_LOOT,
                    CardNames.INVASION,
                    CardNames.PROSPER,
                    CardNames.CURSED
                ).contains(card.name)
            },
            dependentCardNames = listOf(CardNames.LOOT_PILE)
        ),

        // If there is a Looter card present, add Ruins cards
        DependencyRule(
            condition = { it.types.contains(Type.LOOTER) },
            dependentCardNames = listOf(CardNames.RUINS_PILE)
        ),

        // Tournament -> add Prizes
        DependencyRule(
            condition = { it.name == CardNames.TOURNAMENT },
            dependentCardNames = listOf(CardNames.PRIZE_PILE)
        ),
        // Joust -> Add Rewards
        DependencyRule(
            condition = { it.name == CardNames.JOUST },
            dependentCardNames = listOf(CardNames.REWARD_PILE)
        ),

        // If there is a Bandit Camp, Marauder or Pillage card present, add Spoils cards
        DependencyRule(
            condition = { card ->
                listOf(
                    CardNames.BANDIT_CAMP, CardNames.MARAUDER, CardNames.PILLAGE
                ).contains(card.name)
            },
            dependentCardNames = listOf(CardNames.SPOILS)
        ),

        // ARTIFACTS
        // If there is Border Guard present, add Lantern and Horn
        DependencyRule(
            condition = { it.name == CardNames.BORDER_GUARD },
            dependentCardNames = listOf(CardNames.LANTERN, CardNames.HORN)
        ),
        // If there is Flag Bearer present, add Flag
        DependencyRule(
            condition = { it.name == CardNames.FLAG_BEARER },
            dependentCardNames = listOf(CardNames.FLAG)
        ),
        // If there is Swashbuckler present, add Treasure Chest
        DependencyRule(
            condition = { it.name == CardNames.SWASHBUCKLER },
            dependentCardNames = listOf(CardNames.TREASURE_CHEST)
        ),
        // If there is Treasurer present, add Key
        DependencyRule(
            condition = { it.name == CardNames.TREASURER },
            dependentCardNames = listOf(CardNames.KEY)
        ),

        // Travellers
        DependencyRule(
            condition = { it.name == CardNames.PAGE },
            dependentCardNames = listOf(
                CardNames.TREASURE_HUNTER,
                CardNames.WARRIOR,
                CardNames.HERO,
                CardNames.CHAMPION
            )
        ),
        DependencyRule(
            condition = { it.name == CardNames.PEASANT },
            dependentCardNames = listOf(
                CardNames.SOLDIER,
                CardNames.FUGITIVE,
                CardNames.DISCIPLE,
                CardNames.TEACHER
            )
        ),

        // Spirits
        DependencyRule(
            condition = { it.name == CardNames.EXORCIST },
            dependentCardNames = listOf(
                CardNames.WILL_O_WISP,
                CardNames.IMP,
                CardNames.GHOST
            )
        ),

        // Horse
        DependencyRule(
            condition = { card ->
                listOf(
                    CardNames.SLEIGH,
                    CardNames.SUPPLIES,
                    CardNames.SCRAP,
                    CardNames.CAVALRY,
                    CardNames.GROOM,
                    CardNames.HOSTELRY,
                    CardNames.LIVERY,
                    CardNames.PADDOCK,
                    CardNames.RIDE,
                    CardNames.BARGAIN,
                    CardNames.DEMAND,
                    CardNames.STAMPEDE
                ).contains(card.name)
            },
            dependentCardNames = listOf(CardNames.HORSE)
        ),

        // Specific card interactions
        DependencyRule(
            condition = { it.name == CardNames.FOOL },
            dependentCardNames = listOf(CardNames.LOST_IN_THE_WOODS)
        ),
        DependencyRule(
            condition = { it.name == CardNames.NECROMANCER },
            dependentCardNames = listOf(
                CardNames.ZOMBIE_APPRENTICE,
                CardNames.ZOMBIE_MASON,
                CardNames.ZOMBIE_SPY
            )
        ),
        DependencyRule(
            condition = { it.name == CardNames.VAMPIRE },
            dependentCardNames = listOf(CardNames.BAT)
        ),
        DependencyRule(
            condition = { it.name == CardNames.SECRET_CAVE || it.name == CardNames.LEPRECHAUN },
            dependentCardNames = listOf(CardNames.WISH)
        ),
        DependencyRule(
            condition = { it.name == CardNames.HERMIT },
            dependentCardNames = listOf(CardNames.MADMAN)
        ),
        DependencyRule(
            condition = { it.name == CardNames.URCHIN },
            dependentCardNames = listOf(CardNames.MERCENARY)
        ),
        DependencyRule(
            condition = { it.name == CardNames.DEVILS_WORKSHOP || it.name == CardNames.TORMENTOR },
            dependentCardNames = listOf(CardNames.IMP)
        ),

        // If there is a trasher present, add Trash mat
        DependencyRule(
            condition = {
                it.categories.contains(Category.TRASHER) || it.categories.contains(Category.TRASH_FOR_BENEFIT)
            },
            dependentCardNames = listOf(CardNames.TRASH_MAT)
        )
    )
}