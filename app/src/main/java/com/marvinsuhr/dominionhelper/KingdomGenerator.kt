package com.marvinsuhr.dominionhelper

import android.util.Log
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.data.ExpansionDao
import com.marvinsuhr.dominionhelper.data.UserPrefsRepository
import com.marvinsuhr.dominionhelper.model.CardDependencies
import com.marvinsuhr.dominionhelper.model.CardNames
import com.marvinsuhr.dominionhelper.model.Expansion
import com.marvinsuhr.dominionhelper.model.Kingdom
import com.marvinsuhr.dominionhelper.model.Set
import com.marvinsuhr.dominionhelper.model.Type
import com.marvinsuhr.dominionhelper.ui.DarkAgesMode
import com.marvinsuhr.dominionhelper.ui.ProsperityMode
import com.marvinsuhr.dominionhelper.ui.RandomMode
import com.marvinsuhr.dominionhelper.ui.VetoMode
import com.marvinsuhr.dominionhelper.utils.isPercentChance
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

val predicateMapSettings: Map<String, (Card) -> Boolean> = mapOf(
    "Attack" to { it.types.contains(Type.ATTACK) },
    "Cost 2" to { it.cost == 2 }
)

val predicateMap: Map<String, (Card) -> Boolean> = mapOf(
    "Ally" to { it.types.contains(Type.ALLY) },
    "Prophecy" to { it.types.contains(Type.PROPHECY) }
)

@Singleton
class KingdomGenerator @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao,
    private val userPrefsRepository: UserPrefsRepository
) {

    suspend fun generateKingdom(): Kingdom {

        val randomMode = userPrefsRepository.randomMode.first()

        when (randomMode) {
            RandomMode.FULL_RANDOM -> {
                Log.i("Kingdom Generator", "Full random selected")
                return generateKingdomFullRandom()
            }

            RandomMode.EVEN_AMOUNTS -> {
                Log.i("Kingdom Generator", "Even amounts selected")
                return generateKingdomEvenAmounts()
            }
        }
    }

    private suspend fun generateKingdomFullRandom(): Kingdom {

        val cardList = mutableSetOf<Card>()
        val landscapeList = mutableSetOf<Card>()

        val totalCardsToGenerate = userPrefsRepository.numberOfCardsToGenerate.first()
        val totalLandscapesToGenerate = 2

        // Load candidate cards
        val (cardPool, landscapePool) = getCandidatesFullRandom()
        Log.d("Kingdom Generator", "Portrait candidates: ${cardPool.size}")
        Log.d("Kingdom Generator", "Landscape candidates: ${landscapePool.size}")

        // TODO: For each predicate, check if it is enabled
        for (predicate in predicateMapSettings) {
            val success = satisfyRequirement(predicate.key, cardPool, cardList, predicate.value)
            if (!success) {
                Log.w("KingdomGenerator", "Failed to add required ${predicate.key} card.")
            }
        }

        // Fill remaining portrait slots
        val cardsLeft = totalCardsToGenerate - cardList.size
        val cardsToFill = cardPool.shuffled().take(cardsLeft)
        Log.d("Kingdom Generator", "Cards to fill: ${cardsToFill.joinToString { it.name }}")
        cardList.addAll(cardsToFill)

        for (predicate in predicateMap) {
            val success =
                satisfyRequirement(predicate.key, landscapePool, landscapeList, predicate.value)
            if (!success) {
                Log.w("KingdomGenerator", "Failed to add required ${predicate.key} card.")
            }
        }

        // Fill remaining landscape slots
        // Ignore Ally and Prophecy cards, those depend on Liaison and Omen respectively
        val landscapesLeft = totalLandscapesToGenerate - landscapeList.size
        val landscapesToFill = landscapePool
            .filter { it.types.none { type -> type == Type.ALLY || type == Type.PROPHECY } }
            .shuffled()
            .take(landscapesLeft)
        // TODO Log if no landscapes found
        Log.d(
            "Kingdom Generator",
            "Landscapes to fill: ${landscapesToFill.joinToString { it.name }}"
        )
        landscapeList.addAll(landscapesToFill)
        // TODO: Generate 2 different landscapes if possible

        val basicCards = loadCards(CardNames.BASIC_CARDS.associateWith { 1 })

        val dependentCardsToLoad = getDependentCards(cardList)
        val dependentCards = loadCards(dependentCardsToLoad)

        val startingCardsToLoad = getStartingCards(cardList)
        val startingCards = loadCards(startingCardsToLoad)

        val randomCards = listToMap(cardList.toList())
        val landscapeCards = listToMap(landscapeList.toList())

        Log.i(
            "Kingdom Generator",
            "Generated ${randomCards.size} random cards, ${basicCards.size} basic cards, ${dependentCards.size} dependent cards, ${startingCards.size} starting cards, and ${landscapeCards.size} landscape cards."
        )

        return Kingdom(randomCards, basicCards, dependentCards, startingCards, landscapeCards)
    }

    suspend fun getCandidatesFullRandom(): Pair<MutableSet<Card>, MutableSet<Card>> {

        val portraitCandidates = mutableSetOf<Card>()
        val landscapeCandidates = mutableSetOf<Card>()

        // Gets all owned cards
        Log.i("Kingdom Generator", "Full random selected")
        portraitCandidates.addAll(cardDao.getEnabledOwnedCards())
        landscapeCandidates.addAll(cardDao.getEnabledOwnedLandscapes())
        return Pair(portraitCandidates, landscapeCandidates)
    }

    private suspend fun generateKingdomEvenAmounts(): Kingdom {

        val cardList = mutableSetOf<Card>()
        val landscapeList = mutableSetOf<Card>()

        val totalCardsToGenerate = userPrefsRepository.numberOfCardsToGenerate.first()
        val totalLandscapesToGenerate = 2

        val numberOfExpansions = userPrefsRepository.randomExpansionAmount.first()
        if (numberOfExpansions <= 0) {
            Log.w("Kingdom Generator", "Number of expansions was 0")
            // Error?
        }

        val randomExpansions =
            expansionDao.getFixedAmountOfOwnedExpansions(numberOfExpansions)
        // Check !empty

        // Load candidate cards
        val (cardPool, landscapePool) = getCandidatesEvenAmounts(randomExpansions)
        Log.d("Kingdom Generator", "Portrait candidates: ${cardPool.size}")
        Log.d("Kingdom Generator", "Landscape candidates: ${landscapePool.size}")

        // TODO: For each predicate, check if it is enabled
        for (predicate in predicateMapSettings) {
            val success = satisfyRequirement(predicate.key, cardPool, cardList, predicate.value)
            if (!success) {
                Log.w("KingdomGenerator", "Failed to add required ${predicate.key} card.")
            }
        }

        // Fill remaining portrait slots
        val cardsStillToSelectGlobally = totalCardsToGenerate - cardList.size

        if (cardsStillToSelectGlobally > 0) {
            val targetTotalCardsPerExpansion = (totalCardsToGenerate.toDouble() / randomExpansions.size).roundToInt()
            var actualCardsSelectedInThisPhase = 0 // Tracks cards selected specifically in this even distribution loop

            Log.d("Kingdom Generator", "Targeting $targetTotalCardsPerExpansion total cards per expansion.")

            // Create a mutable copy of the cardPool to allow removal as cards are selected
            val mutableCardPool = cardPool.toMutableSet()

            for (expansion in randomExpansions) {
                if (actualCardsSelectedInThisPhase >= cardsStillToSelectGlobally) break // Stop if we've filled all globally needed slots

                // 1. How many cards from THIS expansion are ALREADY in cardList (from predicates)?
                val cardsAlreadySelectedFromThisExpansion = cardList.count { card ->
                    card.sets.any { set -> set.name == expansion.id } // Or your card.expansionId == expansion.id
                }

                // 2. How many more cards do we ideally NEED from THIS expansion to reach the target?
                var cardsNeededFromThisExpansion = targetTotalCardsPerExpansion - cardsAlreadySelectedFromThisExpansion
                cardsNeededFromThisExpansion = maxOf(0, cardsNeededFromThisExpansion) // Ensure it's not negative

                // 3. But we can't select more than what's globally left to select
                cardsNeededFromThisExpansion = minOf(cardsNeededFromThisExpansion, cardsStillToSelectGlobally - actualCardsSelectedInThisPhase)

                if (cardsNeededFromThisExpansion <= 0) {
                    Log.d("Kingdom Generator", "Expansion ${expansion.name} already has enough cards (${
                        cardsAlreadySelectedFromThisExpansion} / ${targetTotalCardsPerExpansion}) or no more cards needed globally.")
                    continue // Move to the next expansion
                }

                // 4. Get available candidate cards from THIS expansion (that are not already in cardList)
                val candidateCardsFromThisExpansion = mutableCardPool
                    .filter { card: Card ->
                        card.sets.any { set -> set.name == expansion.id }
                    }
                // ^ this might go bad. When Base2 is owned, and a card from both is selected,
                // TODO No I THINK it's fine

                // 5. Determine actual number to take
                val takeAmount = minOf(candidateCardsFromThisExpansion.size, cardsNeededFromThisExpansion)

                if (takeAmount > 0) {
                    val selectedFromExpansion = candidateCardsFromThisExpansion.take(takeAmount)
                    Log.d(
                        "Kingdom Generator",
                        "Selected an additional ${selectedFromExpansion.size} cards from ${expansion.name} (already had $cardsAlreadySelectedFromThisExpansion, needed $cardsNeededFromThisExpansion, took $takeAmount): ${selectedFromExpansion.joinToString { it.name }}"
                    )
                    cardList.addAll(selectedFromExpansion)
                    mutableCardPool.removeAll(selectedFromExpansion.toSet())
                    actualCardsSelectedInThisPhase += selectedFromExpansion.size
                } else {
                    Log.d("Kingdom Generator", "No additional cards to select from ${expansion.name} (needed $cardsNeededFromThisExpansion, found ${candidateCardsFromThisExpansion.size}).")
                }
            }

            // Fill any remaining slots if the per-expansion logic didn't perfectly fill all cardsStillToSelectGlobally
            // (e.g., due to rounding, or specific expansions not having enough cards to meet their adjusted target)
            val finalRemainingToDistribute = cardsStillToSelectGlobally - actualCardsSelectedInThisPhase
            if (finalRemainingToDistribute > 0 && mutableCardPool.isNotEmpty()) {
                Log.d("Kingdom Generator", "Even distribution phase completed. Filling remaining $finalRemainingToDistribute slots from the general pool of available cards.")
                val fillFromTheRest = mutableCardPool.shuffled().take(finalRemainingToDistribute)
                Log.d("Kingdom Generator", "Filling with: ${fillFromTheRest.joinToString { it.name }}")
                cardList.addAll(fillFromTheRest)
                actualCardsSelectedInThisPhase += fillFromTheRest.size // Though not strictly needed to track anymore
            }

            if (cardsStillToSelectGlobally - actualCardsSelectedInThisPhase > 0) {
                val stillMissing = cardsStillToSelectGlobally - actualCardsSelectedInThisPhase
                Log.w("Kingdom Generator", "Could not select all $totalCardsToGenerate cards. Still need $stillMissing. This might happen if expansions have too few cards.")
            }

        } else if (cardsStillToSelectGlobally > 0) {
            // Fallback if no expansions were selected OR if all predicate cards filled the list (unlikely for this specific issue)
            Log.w("Kingdom Generator", "No expansions to draw from for even amounts, or no more cards needed after predicates. Filling remaining $cardsStillToSelectGlobally slots randomly if applicable.")
            val cardsToFill = cardPool.shuffled().take(cardsStillToSelectGlobally) // cardPool here should be mutableCardPool if predicates were applied
            cardList.addAll(cardsToFill)
        }

        for (predicate in predicateMap) {
            val success =
                satisfyRequirement(predicate.key, landscapePool, landscapeList, predicate.value)
            if (!success) {
                Log.w("KingdomGenerator", "Failed to add required ${predicate.key} card.")
            }
        }

        // Fill remaining landscape slots
        // Ignore Ally and Prophecy cards, those depend on Liaison and Omen respectively
        val landscapesLeft = totalLandscapesToGenerate - landscapeList.size
        val landscapesToFill = landscapePool
            .filter { it.types.none { type -> type == Type.ALLY || type == Type.PROPHECY } }
            .shuffled()
            .take(landscapesLeft)
        // TODO Log if no landscapes found
        Log.d(
            "Kingdom Generator",
            "Landscapes to fill: ${landscapesToFill.joinToString { it.name }}"
        )
        landscapeList.addAll(landscapesToFill)
        // TODO: Generate 2 different landscapes if possible

        val basicCards = loadCards(CardNames.BASIC_CARDS.associateWith { 1 })

        val dependentCardsToLoad = getDependentCards(cardList)
        val dependentCards = loadCards(dependentCardsToLoad)

        val startingCardsToLoad = getStartingCards(cardList)
        val startingCards = loadCards(startingCardsToLoad)

        val randomCards = listToMap(cardList.toList())
        val landscapeCards = listToMap(landscapeList.toList())

        Log.i(
            "Kingdom Generator",
            "Generated ${randomCards.size} random cards, ${basicCards.size} basic cards, ${dependentCards.size} dependent cards, ${startingCards.size} starting cards, and ${landscapeCards.size} landscape cards."
        )

        return Kingdom(randomCards, basicCards, dependentCards, startingCards, landscapeCards)
    }

    suspend fun getCandidatesEvenAmounts(
        randomExpansions: List<Expansion>
    ): Pair<MutableSet<Card>, MutableSet<Card>> {

        val portraitCandidates = mutableSetOf<Card>()
        val landscapeCandidates = mutableSetOf<Card>()

        Log.i("Kingdom Generator", "Even amounts selected")

        if (randomExpansions.isEmpty()) {
            Log.w(
                "KingdomGenerator",
                "Even amounts selected, but no owned expansions found or selected."
            )
            // Error?
        }

        for (expansion in randomExpansions) {
            portraitCandidates.addAll(
                cardDao.getPortraitsByExpansion(expansion.id)
            )
            landscapeCandidates.addAll(
                cardDao.getLandscapesByExpansion(expansion.id)
            )
        }

        return Pair(portraitCandidates, landscapeCandidates)
    }

    // Helper function to satisfy a requirement
    // Returns true if a card was successfully added, false otherwise
    private fun satisfyRequirement(
        predicateName: String,
        cardPool: MutableSet<Card>,
        cardList: MutableSet<Card>,
        predicate: (Card) -> Boolean
    ): Boolean {
        val candidates = cardPool.filter(predicate)
        Log.d(
            "Kingdom Generator",
            "$predicateName candidates: ${candidates.joinToString { it.name }}"
        )

        if (candidates.isEmpty()) {
            Log.e("Kingdom Generator", "No $predicateName cards found in card pool!")
            return false
        } else {
            val cardToAdd = candidates.random()
            Log.d("Kingdom Generator", "Selected $predicateName card: ${cardToAdd.name}")
            cardList.add(cardToAdd)
            cardPool.remove(cardToAdd)
            return true
        }
    }

    //---------

    // Pass List here?
    // TODO: Review this
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

    suspend fun replaceCardInKingdom(
        cardToRemove: Card,
        cardsToExclude: kotlin.collections.Set<Card>
    ): Card? {

        val isLandscape = cardToRemove.landscape
        val newCard: Card? = when (userPrefsRepository.vetoMode.first()) {

            // Reroll from the same expansion
            // TODO: This rerolls from any OWNED expansion, but we need to reroll from any SELECTED expansion probably
            VetoMode.REROLL_SAME -> {
                Log.i("KingdomGenerator", "Rerolling from the same expansion.")
                generateSingleRandomCardFromExpansion(
                    cardToRemove.sets,
                    cardsToExclude,
                    isLandscape
                )
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

    suspend fun generateSingleRandomCard(
        excludeCards: kotlin.collections.Set<Card> = emptySet(),
        isLandscape: Boolean
    ): Card? {
        val excludedCardIds = excludeCards.map { it.id }.toSet()

        Log.i("Kingdom Generator", "Generating random card from owned Expansions")
        return cardDao.getSingleCardFromOwnedExpansionsWithExceptions(
            excludedCardIds,
            isLandscape
        )
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
                    Log.i(
                        "KingdomGenerator",
                        "Adding Shelters because Dark Ages cards are present"
                    )
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
        CardNames.heirloomPairs.forEach { (cardName, dependentCardName) ->
            if (randomCards.any { it.name == cardName }) {
                Log.d("Kingdom Generator", "Adding Heirloom: $cardName")
                cards[dependentCardName] = 1
                heirloomCount++
            }
        }

        // 1 less Copper per Heirloom
        cards["Copper"] = 7 - heirloomCount
        return cards
    }
}