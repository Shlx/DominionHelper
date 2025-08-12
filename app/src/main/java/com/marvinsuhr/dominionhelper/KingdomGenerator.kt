package com.marvinsuhr.dominionhelper

import android.util.Log
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.data.ExpansionDao
import com.marvinsuhr.dominionhelper.data.UserPrefsRepository
import com.marvinsuhr.dominionhelper.model.CardDependencies
import com.marvinsuhr.dominionhelper.model.CardNames
import com.marvinsuhr.dominionhelper.model.Set
import com.marvinsuhr.dominionhelper.ui.DarkAgesMode
import com.marvinsuhr.dominionhelper.ui.ProsperityMode
import com.marvinsuhr.dominionhelper.ui.RandomMode
import com.marvinsuhr.dominionhelper.ui.VetoMode
import com.marvinsuhr.dominionhelper.utils.isPercentChance
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// Class representing a randomly generated round of Dominion
data class Kingdom(
    val randomCards: LinkedHashMap<Card, Int> = linkedMapOf(), // Do I need an amount here? -> YES, victory cards are dependent on player amount!!
    val basicCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    val dependentCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    val startingCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    val landscapeCards: LinkedHashMap<Card, Int> = linkedMapOf()
) {

    fun hasDependentCards(): Boolean {
        return dependentCards.isNotEmpty()
    }

    fun hasLandscapeCards(): Boolean {
        return landscapeCards.isNotEmpty()
    }

    fun isEmpty(): Boolean {
        return randomCards.isEmpty() && basicCards.isEmpty() && dependentCards.isEmpty() && startingCards.isEmpty() && landscapeCards.isEmpty()
    }
}

@Singleton
class KingdomGenerator @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao,
    private val userPrefsRepository: UserPrefsRepository
) {

    // Main function to generate a Kingdom based on user preferences
    suspend fun generateKingdom(): Kingdom {

        // TODO add 2 landscape cards first. They might affect the rest of the cards (for example Trait: Cursed)
        // Problem: Prophecy card depends on Omen. So maybe...
        // 1. Generate landscape cards
        // 2. Generate random cards
        // 3. Exchange 1 landscape for prophecy if there is an omen
        // 4. Recheck dependencies?

        // Or (this is better? Since landscape cards also have dependencies)
        // 1. Generate random cards
        // 2. Generate landscape cards, including omen if there is a prophecy
        // 3. Check dependencies

        val randomMode = userPrefsRepository.randomMode.first()
        val totalCardsToGenerate = userPrefsRepository.numberOfCardsToGenerate.first()

        val randomCards = getRandomCards(randomMode, totalCardsToGenerate)

        val landscapeCards = getLandscapeCards()

        val basicCards = loadCards(CardNames.BASIC_CARDS.associateWith { 1 })

        val dependentCardsToLoad = getDependentCards(randomCards.keys)
        val dependentCards = loadCards(dependentCardsToLoad)

        val startingCardsToLoad = getStartingCards(randomCards.keys)
        val startingCards = loadCards(startingCardsToLoad)

        Log.i(
            "Kingdom Generator",
            "Generated ${randomCards.size} random cards, ${basicCards.size} basic cards, ${dependentCards.size} dependent cards, ${startingCards.size} starting cards, and ${landscapeCards.size} landscape cards."
        )

        return Kingdom(randomCards, basicCards, dependentCards, startingCards, landscapeCards)
    }

    // Pass List here?
    suspend fun loadCards(cardsToLoad: Map<String, Int>): LinkedHashMap<Card, Int> {

        val cardNames = cardsToLoad.keys.toList()
        val loadedCardsList = cardDao.getCardsByNameList(cardNames)

        if (loadedCardsList.size != cardNames.size) {
            val missingNames =
                cardNames.filterNot { name -> loadedCardsList.any { it.name == name } }
            Log.e(
                "KingdomGenerator",
                "Critical error: Not all cards found in DB! Missing: $missingNames"
            )
            throw IllegalStateException("Failed to load some cards. Missing: $missingNames")
        }

        // Reconstruct the map with Card objects as keys and original Int values
        val result = LinkedHashMap<Card, Int>()
        loadedCardsList.forEach { card ->
            // Find the original amount from the input map.
            // The !! is safe here because of the size check above, ensuring card.name was in cardsToLoad.keys
            // Hmm
            result[card] = cardsToLoad[card.name]!!
        }
        return result
    }

    suspend fun getRandomCards(
        randomMode: RandomMode,
        totalCardsToGenerate: Int
    ): LinkedHashMap<Card, Int> {

        val generatedCardList: List<Card> = when (randomMode) {

            RandomMode.FULL_RANDOM -> {
                Log.i("Kingdom Generator", "Full random selected")

                cardDao.getRandomCardsFromOwnedExpansions(totalCardsToGenerate)
            }

            RandomMode.EVEN_AMOUNTS -> {
                Log.i("Kingdom Generator", "Even amounts selected")

                val generatedCards = mutableListOf<Card>()
                val numberOfExpansions = userPrefsRepository.randomExpansionAmount.first()

                if (numberOfExpansions > 0) {
                    val randomExpansions =
                        expansionDao.getFixedAmountOfOwnedExpansions(numberOfExpansions)




                    if (randomExpansions.isEmpty()) {
                        Log.w(
                            "KingdomGenerator",
                            "Even amounts selected, but no owned expansions found or selected."
                        )
                        return linkedMapOf() // Return empty map if no expansions to pick from
                    }

                    // Ensure we don't try to pick from more expansions than we actually got
                    // (e.g., user wants 3 expansions, but only owns 2)
                    // TODO: Trigger error message
                    val expansionsToUse = randomExpansions.take(numberOfExpansions)
                    if (expansionsToUse.isEmpty()) {
                        Log.w(
                            "KingdomGenerator",
                            "Even amounts selected, but expansionsToUse is empty after take()."
                        )
                        return linkedMapOf()
                    }

                    if (totalCardsToGenerate == 0) {
                        Log.i("KingdomGenerator", "totalCardsToGenerate is 0, returning empty map.")
                        return linkedMapOf()
                    }


                    // TODO: Check for division by zero to be safe
                    val baseAmountPerExpansion = totalCardsToGenerate / expansionsToUse.size
                    var remainingCardsToDistribute = totalCardsToGenerate % expansionsToUse.size

                    for (expansion in expansionsToUse) {
                        var cardsFromThisExpansion = baseAmountPerExpansion
                        if (remainingCardsToDistribute > 0) {
                            cardsFromThisExpansion++
                            remainingCardsToDistribute--
                        }
                        if (cardsFromThisExpansion > 0) {
                            generatedCards.addAll(
                                cardDao.getRandomCardsFromExpansion(
                                    expansion.id,
                                    cardsFromThisExpansion
                                )
                            )
                        }
                    }
                }
                generatedCards
            }
        }

        return listToMap(generatedCardList)
    }

    suspend fun getLandscapeCards(): LinkedHashMap<Card, Int> {
        // TODO Find way to generate DIFFERENT landscape types (from the same expansion), also when dismissing
        val cards = cardDao.getRandomLandscapeCardsFromOwnedExpansions(2)
        return listToMap(cards)
    }

    suspend fun replaceCardInKingdom(
        cardToRemove: Card,
        cardsToExclude: kotlin.collections.Set<Card>
    ): Card? {

        val isLandscape = cardToRemove.landscape
        val newCard: Card? = when (userPrefsRepository.vetoMode.first()) {

            // Reroll from the same expansion
            VetoMode.REROLL_SAME -> {
                Log.i("KingdomGenerator", "Rerolling from the same expansion.")
                generateSingleRandomCardFromExpansion(cardToRemove.sets, cardsToExclude, isLandscape)
            }

            // Reroll from any owned expansions
            VetoMode.REROLL_ANY -> {
                Log.i("KingdomGenerator", "Rerolling from any expansions.")
                generateSingleRandomCard(cardsToExclude, isLandscape)
            }

            // TODO: Veto mode NO_REROLL is checked beforehand. This is kind of messy tho.
            // In this case, returning null is an error case. Throw Exception here?
            VetoMode.NO_REROLL -> {
                Log.i("KingdomGenerator", "Not rerolling.")
                null
            }
        }

        return newCard
    }

    suspend fun generateSingleRandomCard(excludeCards: kotlin.collections.Set<Card> = emptySet(), isLandscape: Boolean): Card? {
        val excludedCardIds = excludeCards.map { it.id }.toSet()

        Log.i("Kingdom Generator", "Generating random card from owned Expansions")
        return cardDao.getSingleCardFromOwnedExpansionsWithExceptions(excludedCardIds, isLandscape)
    }

    suspend fun generateSingleRandomCardFromExpansion(
        sets: List<Set>,
        excludeCards: kotlin.collections.Set<Card> = emptySet(),
        isLandscape: Boolean
    ): Card? {
        val excludedCardIds = excludeCards.map { it.id }.toSet()

        val setName1: String? = sets.getOrNull(0)?.name
        val setName2: String? = sets.getOrNull(1)?.name

        return if (setName1 != null) {
            val logMessage = "Generating random card from $setName1" +
                    (if (setName2 != null) " and $setName2" else "")
            Log.i("Kingdom Generator", logMessage)

            cardDao.getSingleCardFromExpansionWithExceptions(
                setName1,
                setName2,
                excludedCardIds,
                isLandscape
            )
        } else {
            // No sets provided (sets list was empty), or set names were null
            Log.w("Kingdom Generator", "Cannot generate card: No valid sets provided.")
            null
        }
    }

    private fun listToMap(list: List<Card>): LinkedHashMap<Card, Int> {
        val map = linkedMapOf<Card, Int>()
        list.forEach { card ->
            map[card] = 1 // Default value of 1
        }
        return map
    }

    private suspend fun getDependentCards(cards: kotlin.collections.Set<Card>): LinkedHashMap<String, Int> {

        // TODO: If a trait is present, choose a random card

        val dependencyRules = CardDependencies().dependencyRules
        val dependentCardNames = mutableSetOf<String>()

        // TODO Efficiency: When a dependencyRule is met, the other ones are still checked.
        // We should not check further rules when one is found, as this is just a waste of resources.
        // We can change this by using any().
        // -> Is this true?
        dependencyRules.forEach { rule ->
            cards.forEach { card ->
                if (rule.condition(card)) {
                    dependentCardNames.addAll(rule.dependentCardNames)
                }
            }
        }

        dependentCardNames.addAll(checkProsperityBasicCards(cards))

        val dependentCardMap = LinkedHashMap<String, Int>()
        dependentCardNames.forEach { cardName ->
            dependentCardMap[cardName] = 1 // Default 1
        }

        return dependentCardMap
    }

    private suspend fun checkProsperityBasicCards(randomCards: kotlin.collections.Set<Card>): List<String> {

        val prosperityCardsToAdd = mutableListOf<String>()
        val prosperityMode = userPrefsRepository.prosperityBasicCardsMode.first()

        val prosperityCount = randomCards.count {
            it.sets.contains(Set.PROSPERITY_1E) || it.sets.contains(Set.PROSPERITY_2E)
        }

        when (prosperityMode) {

            // Don't add in any case
            ProsperityMode.NEVER -> {
                return emptyList()
            }

            // 10% chance per prosperity card
            ProsperityMode.TEN_PERCENT_PER_CARD -> {
                if (prosperityCount > 0) {
                    if (isPercentChance(prosperityCount * 10.0)) {
                        Log.i(
                            "KingdomGenerator",
                            "Adding Platinum and Colony - 10% per card ($prosperityCount)"
                        )
                        prosperityCardsToAdd.add("Platinum")
                        prosperityCardsToAdd.add("Colony")
                    }
                }
            }

            // Always add Platinum and Colony if at least one Prosperity card is in the 10 random kingdom cards
            ProsperityMode.IF_PRESENT -> {
                if (prosperityCount > 0) {
                    Log.i(
                        "KingdomGenerator",
                        "Adding Platinum and Colony (Prosperity card present in Kingdom rule)"
                    )
                    prosperityCardsToAdd.add("Platinum")
                    prosperityCardsToAdd.add("Colony")
                }
            }
        }

        return prosperityCardsToAdd
    }

    private suspend fun getStartingCards(randomCards: kotlin.collections.Set<Card>): Map<String, Int> {

        val cards = mutableMapOf<String, Int>()
        val darkAgesMode = userPrefsRepository.darkAgesStarterCardsMode.first()
        val darkAgesCount = randomCards.count { it.sets.contains(Set.DARK_AGES) }

        when (darkAgesMode) {

            // Don't add in any case
            DarkAgesMode.NEVER -> {
                cards["Estate"] = 3
            }

            // 10% per Dark Ages card to use Shelters instead of Estates
            DarkAgesMode.TEN_PERCENT_PER_CARD -> {
                if (isPercentChance(darkAgesCount * 10.0)) {
                    Log.i("KingdomGenerator", "Adding Shelters - 10% per card ($darkAgesCount)")
                    cards["Overgrown Estate"] = 1
                    cards["Hovel"] = 1
                    cards["Necropolis"] = 1
                } else {
                    cards["Estate"] = 3
                }
            }

            // Always add Shelters if at least one Dark Ages card is in the 10 random kingdom cards
            DarkAgesMode.IF_PRESENT -> {
                if (darkAgesCount > 0) {
                    Log.i("KingdomGenerator", "Adding Shelters because Dark Ages cards are present")
                    cards["Overgrown Estate"] = 1
                    cards["Hovel"] = 1
                    cards["Necropolis"] = 1
                } else {
                    cards["Estate"] = 3
                }
            }
        }

        // Add Heirlooms
        var heirloomCount = 0
        CardNames.cardPairs.forEach { (cardName, dependentCardName) ->
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
}