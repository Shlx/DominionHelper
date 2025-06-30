package com.example.dominionhelper

import android.util.Log
import com.example.dominionhelper.model.Card
import com.example.dominionhelper.data.CardDao
import com.example.dominionhelper.data.CardDao.Companion.BASIC_CARD_NAMES
import com.example.dominionhelper.data.ExpansionDao
import com.example.dominionhelper.model.Category
import com.example.dominionhelper.model.Set
import com.example.dominionhelper.model.Type
import com.example.dominionhelper.utils.isPercentChance
import javax.inject.Inject

data class Kingdom(
    val randomCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    val basicCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    val dependentCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    val startingCards: LinkedHashMap<Card, Int> = linkedMapOf()
) {

    fun hasDependentCards(): Boolean {
        return dependentCards.isNotEmpty()
    }

    fun isNotEmpty(): Boolean {
        return randomCards.isNotEmpty() || basicCards.isNotEmpty() || dependentCards.isNotEmpty() || startingCards.isNotEmpty()
    }
}

class KingdomGenerator @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao
) {

    suspend fun generateKingdom(): Kingdom {

        val randomExpansions = expansionDao.getFixedAmountOfOwnedExpansions(2)
        val randomCards = mutableListOf<Card>()

        for (expansion in randomExpansions) {
            randomCards.addAll(cardDao.getRandomCardsFromExpansion(expansion.id, 5))
        }

        // TODO Handle DAO null return in separate class?
        val basicCards = cardDao.getCardsByNameList(BASIC_CARD_NAMES)

        // TODO add 2 landscape cards first. They might affect the rest of the cards (for example Trait: Cursed)

        // TODO: Check if all names exist in DB
        val dependentCardsToLoad = getDependentCards(randomCards)
        val dependentCards = cardDao.getCardsByNameList(dependentCardsToLoad)

        // The amount of these cards is dependent on player count, so we set these as 1 for now
        val randomCardMap = listToMap(randomCards)
        val basicCardMap = listToMap(basicCards)
        val dependentCardMap = listToMap(dependentCards)

        val startingCardsToLoad =
            getStartingCards(randomCards)

        val startingCards = linkedMapOf<Card, Int>()
        startingCardsToLoad.keys.forEach { cardName ->
            val card = cardDao.getCardByName(cardName)
            card.let {
                startingCards[it] = startingCardsToLoad[cardName]!!
            }
        }

        /* val correctStartingCards = startingCards.mapNotNull { (cardName, count) ->
            cardDao.getCardByName(cardName)?.let { it to count }
        }.toMap() */

        Log.i(
            "Kingdom Generator",
            "Generated ${randomCards.size} random cards, ${basicCards.size} basic cards, ${dependentCards.size} dependent cards"
        )

        return Kingdom(randomCardMap, basicCardMap, dependentCardMap, startingCards)
    }

    suspend fun generateSingleRandomCard(excludeCards: kotlin.collections.Set<Card> = emptySet()): Card? {
        val excludedCardIds = excludeCards.map { it.id }.toSet()
        return cardDao.getSingleCardFromOwnedExpansionsWithExceptions(excludedCardIds)
    }

    suspend fun generateSingleRandomCardFromExpansion(sets: List<Set>, excludeCards: kotlin.collections.Set<Card> = emptySet()): Card? {
        val excludedCardIds = excludeCards.map { it.id }.toSet()

        if (sets.size == 2) {
            Log.i("Kingdom Generator", "Generating random card from ${sets[0].name} and ${sets[1].name}")
            return cardDao.getSingleCardFromExpansionWithExceptions(
                sets[0].name,
                sets[1].name,
                excludedCardIds
            )
        } else if (sets.size == 1) {
            Log.i("Kingdom Generator", "Generating random card from ${sets[0].name}")
                return cardDao.getSingleCardFromExpansionWithExceptions(sets[0].name, null,excludedCardIds)
        } else return null
    }

    private fun listToMap(list: List<Card>): LinkedHashMap<Card, Int> {
        val map = linkedMapOf<Card, Int>()
        list.forEach { card ->
            map[card] = 1 // Default value of 1
        }
        return map
    }

    private fun getDependentCards(cards: List<Card>): List<String> {

        // Schwierig: Ferryman, Young Witch, Black Market Riverboat, Approaching Army, Diving Wind, Inherited

        // TODO Efficiency: When a dependencyRule is met, the other ones are still checked. We should not check further rules when one is found, as this is just a waste of resources. We can change this by using any().

        val dependencyRules = listOf(

            // If there is a Curser present, add Curse card
            DependencyRule(
                condition = { it.categories.contains(Category.CURSER) },
                dependentCardNames = listOf("Curse")
            ),

            // If there is an Alchemy card present, add Potion
            DependencyRule(
                condition = { it.sets.contains(Set.ALCHEMY) },
                dependentCardNames = listOf("Potion")
            ),

            // If there is a Fate card present, add all Boons
            DependencyRule(
                condition = { it.types.contains(Type.FATE) },
                dependentCardNames = listOf("All 12 Boons", "Will-o'-Wisp")
            ),

            // If there is a Fate card present, add all Hexes and corresponding States
            DependencyRule(
                condition = { it.types.contains(Type.DOOM) },
                dependentCardNames = listOf(
                    "All 12 Hexes",
                    "Curse",
                    "Deluded",
                    "Envious",
                    "Miserable",
                    "Twice Miserable"
                )
            ),

            // If there is a card present that rewards loot, add all Loots
            DependencyRule(
                condition = { it.name == "Jewelled Egg" || it.name == "Peril" || it.name == "Search" || it.name == "Foray" || it.name == "Pickaxe" || it.name == "Wealthy Village" || it.name == "Cutthroat" || it.name == "Looting" || it.name == "Sack of Loot" || it.name == "Invasion" || it.name == "Prosper" },
                dependentCardNames = listOf("All 12 Loots")
            ),

            // If there is a Looter card present, add Ruins cards
            DependencyRule(
                condition = { it.types.contains(Type.LOOTER) },
                dependentCardNames = listOf("Ruins pile")
            ),

            // TODO: If Tournament -> add Prize
            // TODO: If Jouse -> Add Reward

            // If there is a Bandit Camp, Marauder oder Pillage card present, add Spoils cards
            DependencyRule(
                condition = { it.name == "Bandit Camp" || it.name == "Marauder" || it.name == "Pillage" },
                dependentCardNames = listOf("Spoils")
            ),

            // ARTIFACTS

            // If there is Border Guard present, add Lantern and Horn
            DependencyRule(
                condition = { it.name == "Border Guard" },
                dependentCardNames = listOf("Lantern", "Horn")
            ),

            // If there is Flag Bearer present, add Flag
            DependencyRule(
                condition = { it.name == "Flag Bearer" },
                dependentCardNames = listOf("Flag")
            ),

            // If there is Swashbuckler present, add Treasure Chest
            DependencyRule(
                condition = { it.name == "Swashbuckler" },
                dependentCardNames = listOf("Treasure Chest")
            ),

            // If there is Treasurer present, add Key
            DependencyRule(
                condition = { it.name == "Treasurer" },
                dependentCardNames = listOf("Key")
            ),

            // Watch out here that there is truly only ONE card added
            // This might not even make sense since people will probably prefer to pull a physical card

            // If there is a Liaison card present, add an Ally card
            DependencyRule(
                condition = { it.types.contains(Type.LIAISON) },
                dependentCardNames = listOf("Key") // TODO: Add ONE random Ally card
            ),

            // If there is an Omen card present, add a Prophecy card
            DependencyRule(
                condition = { it.types.contains(Type.OMEN) },
                dependentCardNames = listOf("Key") // TODO: Add ONE random Prophecy card
            ),

            // TODO would it be better to add these to the database entries directly?

            // If there is Fool present, add Lost in the Woods
            DependencyRule(
                condition = { it.name == "Fool" },
                dependentCardNames = listOf("Lost in the Woods")
            ),

            // If there is Necromancer present, add zombies
            DependencyRule(
                condition = { it.name == "Necromancer" },
                dependentCardNames = listOf("Zombie Apprentice", "Zombie Mason", "Zombie Spy")
            ),

            // If there is Vampire present, add Bat
            DependencyRule(
                condition = { it.name == "Vampire" },
                dependentCardNames = listOf("Bat")
            ),

            // If there is Leprechaun or Secret Cave present, add Wish
            DependencyRule(
                condition = { it.name == "Leprechaun" || it.name == "Secret Cave" },
                dependentCardNames = listOf("Wish")
            ),

            // If there is Hermit present, add Madman
            DependencyRule(
                condition = { it.name == "Hermit" },
                dependentCardNames = listOf("Madman")
            ),

            // If there is a trasher present, add Trash mat
            DependencyRule(
                condition = { it.categories.contains(Category.TRASHER) || it.categories.contains(Category.TRASH_FOR_BENEFIT) },
                dependentCardNames = listOf("Trash Mat")
            ),

            // TODO % chance?
            // Count Prosperity cards and add Platinum and Colony
            DependencyRuleCount(
                condition = { it.sets.contains(Set.PROSPERITY_1E) || it.sets.contains(Set.PROSPERITY_2E) },
                dependentCardNames = listOf("Platinum", "Colony"),
                minCount = 1
            )
        )

        val dependentCardNames = mutableSetOf<String>()
        dependencyRules.forEach { rule ->

            var count = 0
            cards.forEach { card ->

                when (rule) {
                    is DependencyRuleCount -> {
                        if (rule.condition(card)) {
                            count++
                        }
                    }

                    is DependencyRule -> {
                        if (rule.condition(card)) {
                            dependentCardNames += rule.dependentCardNames
                        }
                    }
                }
            }

            if (rule is DependencyRuleCount && count >= rule.minCount) {
                dependentCardNames += rule.dependentCardNames
            }
        }

        return dependentCardNames.toList()
    }

    private fun getStartingCards(randomCards: List<Card>): Map<String, Int> {

        val cards = mutableMapOf<String, Int>()

        // 10% per Dark Ages card to use Shelters instead of Estates
        val darkAgesCount = randomCards.count { it.sets.contains(Set.DARK_AGES) }
        Log.i("CardViewModel", "Dark Ages count: $darkAgesCount")
        if (isPercentChance(darkAgesCount * 10.0)) {
            Log.i("CardViewModel", "Adding Dark Ages cards")
            cards["Overgrown Estate"] = 1
            cards["Hovel"] = 1
            cards["Necropolis"] = 1
        } else {
            cards["Estate"] = 3
        }

        // Add Heirlooms
        var heirloomCount = 0
        cardPairs.forEach { (cardName, dependentCardName) ->
            if (randomCards.any { it.name == cardName }) {
                cards[dependentCardName] = 1
                heirloomCount++
            }
        }

        // 1 less Copper per Heirloom
        cards["Copper"] = 7 - heirloomCount

        return cards
    }

    // Data class to represent a dependency rule
    data class DependencyRule(
        val condition: (Card) -> Boolean,
        val dependentCardNames: List<String>
    )

    // Data class to represent a dependency rule
    data class DependencyRuleCount(
        val condition: (Card) -> Boolean,
        val minCount: Int = 1,
        val dependentCardNames: List<String>
    )

    private val cardPairs = listOf(
        "Fool" to "Lucky Coin",
        "Cemetery" to "Haunted Mirror",
        "Secret Cave" to "Magic Lamp",
        "Pixie" to "Goat",
        "Shepherd" to "Pasture",
        "Tracker" to "Pouch",
        "Pooka" to "Cursed Gold"
    )

}