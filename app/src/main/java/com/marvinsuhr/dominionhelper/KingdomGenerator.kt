package com.marvinsuhr.dominionhelper

import android.util.Log
import com.marvinsuhr.dominionhelper.model.Card
import com.marvinsuhr.dominionhelper.data.CardDao
import com.marvinsuhr.dominionhelper.data.ExpansionDao
import com.marvinsuhr.dominionhelper.data.UserPrefsRepository
import com.marvinsuhr.dominionhelper.model.CardDependencies
import com.marvinsuhr.dominionhelper.model.CardNames
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

// Class representing a randomly generated round of Dominion
data class Kingdom(

    // Amount is needed because of victory cards like Garden
    val randomCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    // Amount is needed for basic victory cards
    val basicCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    // Amount is needed for cards like Ruins
    val dependentCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    // Copper and Estate can have varying amounts
    val startingCards: LinkedHashMap<Card, Int> = linkedMapOf(),
    // Amount not really needed
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

    fun getAllCards(): List<Card> {
        return randomCards.keys.toList() + basicCards.keys.toList() + dependentCards.keys.toList() + startingCards.keys.toList() + landscapeCards.keys.toList()
    }
}

@Singleton
class KingdomGenerator @Inject constructor(
    private val cardDao: CardDao,
    private val expansionDao: ExpansionDao,
    private val userPrefsRepository: UserPrefsRepository
) {

    suspend fun generateKingdom(): Kingdom {

        val cardList = mutableSetOf<Card>()
        val landscapeList = mutableSetOf<Card>()

        val randomMode = userPrefsRepository.randomMode.first()
        val totalCardsToGenerate = userPrefsRepository.numberOfCardsToGenerate.first()
        val totalLandscapesToGenerate = 2

        // Load candidate cards
        val (cardPool, landscapePool) = getCandidates(randomMode)
        Log.d("Kingdom Generator", "Portrait candidates: ${cardPool.size}")
        Log.d("Kingdom Generator", "Landscape candidates: ${landscapePool.size}")

        val requireAttack = true
        val require2cost = true

        // Make list / map of predicates for each setting, then iterate those
        val predicate: (Card) -> Boolean = { it.types.contains(Type.ATTACK) }
        val predicate2: (Card) -> Boolean = { it.cost == 2 }
        val predicate3: (Card) -> Boolean = {it.types.contains(Type.ALLY)}
        val predicate4: (Card) -> Boolean = {it.types.contains(Type.PROPHECY)}

        if (requireAttack) {
            val success = satisfyRequirement("Attack", cardPool, cardList, predicate)
            if (!success) {
                Log.w("KingdomGenerator", "Failed to add required Attack card.")
            }
        }

        if (require2cost) {
            val success = satisfyRequirement("Cost 2", cardPool, cardList, predicate2)
            if (!success) {
                Log.w("KingdomGenerator", "Failed to add required Cost 2 card.")
            }
        }

        // Fill remaining slots
        val cardsLeft = totalCardsToGenerate - cardList.size
        val cardsToFill = cardPool.shuffled().take(cardsLeft)
        Log.d("Kingdom Generator", "Cards to fill: ${cardsToFill.joinToString { it.name }}")
        cardList.addAll(cardsToFill)
        // TODO Shuffle list? Otherwise constraint cards are always on top

        if (cardList.any { it.types.contains(Type.LIAISON) }) {
            val success = satisfyRequirement("Ally", landscapePool, landscapeList, predicate3)
            if (!success) {
                Log.w("KingdomGenerator", "Failed to add required Ally card.")
            }
        }

        if (cardList.any { it.types.contains(Type.OMEN) }) {
            val success = satisfyRequirement("Prophecy", landscapePool, landscapeList, predicate4)
            if (!success) {
                Log.w("KingdomGenerator", "Failed to add required Prophecy card.")
            }
        }

        // Ignore Ally and Prophecy cards, those depend on Liaison and Omen respectively
        val landscapesLeft = totalLandscapesToGenerate - landscapeList.size
        val landscapesToFill = landscapePool
            .filter { it.types.none { type -> type == Type.ALLY || type == Type.PROPHECY } }
            .shuffled()
            .take(landscapesLeft)
        // TODO Log if no landscapes found
        Log.d("Kingdom Generator", "Landscapes to fill: ${landscapesToFill.joinToString { it.name }}")
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

    suspend fun getCandidates(randomMode: RandomMode): Pair<MutableSet<Card>, MutableSet<Card>> {

        val portraitCandidates = mutableSetOf<Card>()
        val landscapeCandidates = mutableSetOf<Card>()

        when (randomMode) {

            RandomMode.FULL_RANDOM -> {
                Log.i("Kingdom Generator", "Full random selected")
                portraitCandidates.addAll(cardDao.getEnabledOwnedCards())
                landscapeCandidates.addAll(cardDao.getEnabledOwnedLandscapes())
                return Pair(portraitCandidates, landscapeCandidates)
            }

            RandomMode.EVEN_AMOUNTS -> {
                Log.i("Kingdom Generator", "Even amounts selected")

                val numberOfExpansions = userPrefsRepository.randomExpansionAmount.first()

                if (numberOfExpansions <= 0) {
                    Log.w("Kingdom Generator", "Number of expansions was 0")
                    // Error?
                }

                val randomExpansions =
                    expansionDao.getFixedAmountOfOwnedExpansions(numberOfExpansions)

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