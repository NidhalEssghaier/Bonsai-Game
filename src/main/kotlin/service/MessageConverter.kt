package service

import messages.*
import entity.*

/**
 * Class used to convert between network message classes and game classes.
 */
class MessageConverter {

    /**
     * Convert [TileType] to [TileTypeMessage]
     *
     * @param tileType tile type to convert
     *
     * @throws IllegalArgumentException if the given [TileType] is [TileType.GENERIC]
     *
     * @return converted [TileTypeMessage]
     */
    fun fromTileType(tileType: TileType): TileTypeMessage {
        return when (tileType) {
            TileType.WOOD -> TileTypeMessage.WOOD
            TileType.LEAF -> TileTypeMessage.LEAF
            TileType.FLOWER -> TileTypeMessage.FLOWER
            TileType.FRUIT -> TileTypeMessage.FRUIT
            else -> throw IllegalArgumentException("This tile type cannot be used here.")
        }
    }

    /**
     * Convert [GoalCard] to a pair of [GoalTileTypeMessage] and [Int]
     *
     * @param goalCard [GoalCard] to convert
     *
     * @return converted pair of [GoalTileTypeMessage] and [Int]
     */
    fun fromGoal(goalCard: GoalCard): Pair<GoalTileTypeMessage, Int> {
        val color = when (goalCard.color) {
            GoalColor.BROWN -> GoalTileTypeMessage.BROWN
            GoalColor.ORANGE -> GoalTileTypeMessage.ORANGE
            GoalColor.GREEN -> GoalTileTypeMessage.GREEN
            GoalColor.RED -> GoalTileTypeMessage.PINK
            GoalColor.BLUE -> GoalTileTypeMessage.BLUE
        }
        val tier = when (goalCard.difficulty) {
            GoalDifficulty.LOW -> 0
            GoalDifficulty.INTERMEDIATE -> 1
            GoalDifficulty.HARD -> 2
        }

        return Pair(color, tier)
    }

    /**
     * Converts a list of [GoalCard] to a list of [GoalTileTypeMessage]
     *
     * @param goalCards list of [GoalCard] to convert
     *
     * @return converted list of [GoalTileTypeMessage]
     */
    fun fromGoalList(goalCards: MutableList<GoalCard?>): MutableList<GoalTileTypeMessage> {
        val goalColors: MutableList<GoalTileTypeMessage> = mutableListOf()

        for (i in goalCards.indices) {
            when (goalCards[i]?.color) {
                GoalColor.BROWN -> if (!goalColors.contains(GoalTileTypeMessage.BROWN))
                    goalColors.add(GoalTileTypeMessage.BROWN)
                GoalColor.ORANGE -> if (!goalColors.contains(GoalTileTypeMessage.ORANGE))
                    goalColors.add(GoalTileTypeMessage.ORANGE)
                GoalColor.GREEN -> if (!goalColors.contains(GoalTileTypeMessage.GREEN))
                    goalColors.add(GoalTileTypeMessage.GREEN)
                GoalColor.RED -> if (!goalColors.contains(GoalTileTypeMessage.PINK))
                    goalColors.add(GoalTileTypeMessage.PINK)
                GoalColor.BLUE -> if (!goalColors.contains(GoalTileTypeMessage.BLUE))
                    goalColors.add(GoalTileTypeMessage.BLUE)
                null -> {}
            }
        }

        return goalColors
    }

    /**
     * Converts [ZenCard] to a pair of [CardTypeMessage] and [Int]
     *
     * @param card [ZenCard] to convert
     *
     * @throws IllegalArgumentException if the card is invalid
     *
     * @return converted pair of [CardTypeMessage] and [Int]
     */
    fun fromCard(card: ZenCard): Pair<CardTypeMessage, Int> {
        return when (card) {
            is GrowthCard -> Pair(CardTypeMessage.GROWTH, card.id)
            is HelperCard -> Pair(CardTypeMessage.HELPER, card.id)
            is MasterCard -> Pair(CardTypeMessage.MASTER, card.id)
            is ParchmentCard -> Pair(CardTypeMessage.PARCHMENT, card.id)
            is ToolCard -> Pair(CardTypeMessage.TOOL, card.id)
            else -> throw IllegalArgumentException("invalid card")
        }
    }

    /**
     * Converts [PotColor] to [ColorTypeMessage]
     *
     * @param potColor [PotColor] to convert
     *
     * @return converted [ColorTypeMessage]
     */
    fun fromPotColor(potColor: PotColor): ColorTypeMessage {
        return when (potColor) {
            PotColor.PURPLE -> ColorTypeMessage.PURPLE
            PotColor.GRAY -> ColorTypeMessage.BLACK
            PotColor.RED -> ColorTypeMessage.RED
            PotColor.BLUE -> ColorTypeMessage.BLUE
        }
    }

    /**
     * Converts a pair of [CardTypeMessage] and [Int] to [ZenCard]
     *
     * @param card pair of [CardTypeMessage] and [Int] to convert
     *
     * @throws IllegalArgumentException if the given [Int] is an invalid index
     *
     * @return converted [ZenCard]
     */
    fun toCard(card: Pair<CardTypeMessage, Int>): ZenCard {
        return when (card.first) {
            CardTypeMessage.GROWTH -> toGrowthCard(card)
            CardTypeMessage.HELPER -> toHelperCard(card)
            CardTypeMessage.MASTER -> toMasterCard(card)
            CardTypeMessage.PARCHMENT -> toParchmentCard(card)
            CardTypeMessage.TOOL -> when (card.second) {
                in (41..46) -> ToolCard(card.second)
                else -> throw IllegalArgumentException("invalid index")
            }
        }
    }

    private fun toGrowthCard(card: Pair<CardTypeMessage, Int>): ZenCard {
        return when (card.second) {
            0, 1, 8, 12 -> GrowthCard(TileType.WOOD, card.second)
            2, 3, 9, 10 -> GrowthCard(TileType.LEAF, card.second)
            4, 5, 11 -> GrowthCard(TileType.FLOWER, card.second)
            6, 7, 13 -> GrowthCard(TileType.FRUIT, card.second)
            else -> throw IllegalArgumentException("invalid index")
        }
    }

    private fun toHelperCard(card: Pair<CardTypeMessage, Int>): ZenCard {
        return when (card.second) {
            14, 15, 16 -> HelperCard(listOf(TileType.GENERIC, TileType.WOOD), card.second)
            17, 18 -> HelperCard(listOf(TileType.GENERIC, TileType.LEAF), card.second)
            19 -> HelperCard(listOf(TileType.GENERIC, TileType.FLOWER), card.second)
            20 -> HelperCard(listOf(TileType.GENERIC, TileType.FRUIT), card.second)
            else -> throw IllegalArgumentException("invalid index")
        }
    }

    private fun toMasterCard(card: Pair<CardTypeMessage, Int>): ZenCard {
        return when (card.second) {
            21 -> MasterCard(listOf(TileType.WOOD, TileType.WOOD), card.second)
            22, 26 -> MasterCard(listOf(TileType.LEAF, TileType.LEAF), card.second)
            23, 29, 30 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF), card.second)
            24, 25, 28 -> MasterCard(listOf(TileType.GENERIC), card.second)
            27 -> MasterCard(listOf(TileType.LEAF, TileType.FRUIT), card.second)
            31 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF, TileType.FLOWER), card.second)
            32 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF, TileType.FRUIT), card.second)
            33 -> MasterCard(listOf(TileType.LEAF, TileType.FLOWER, TileType.FLOWER), card.second)
            else -> throw IllegalArgumentException("invalid index")
        }
    }

    private fun toParchmentCard(card: Pair<CardTypeMessage, Int>): ZenCard {
        return when (card.second) {
            34 -> ParchmentCard(2, ParchmentCardType.MASTER, card.second)
            35 -> ParchmentCard(2, ParchmentCardType.GROWTH, card.second)
            36 -> ParchmentCard(2, ParchmentCardType.HELPER, card.second)
            37 -> ParchmentCard(2, ParchmentCardType.FLOWER, card.second)
            38 -> ParchmentCard(2, ParchmentCardType.FRUIT, card.second)
            39 -> ParchmentCard(1, ParchmentCardType.LEAF, card.second)
            40 -> ParchmentCard(1, ParchmentCardType.WOOD, card.second)
            else -> throw IllegalArgumentException("invalid index")
        }
    }

    /**
     * Converts [ColorTypeMessage] to [PotColor]
     *
     * @param potColor [ColorTypeMessage] to convert
     *
     * @return converted [PotColor]
     */
    fun toPotColor(potColor: ColorTypeMessage): PotColor {
        return when (potColor) {
            ColorTypeMessage.BLUE -> PotColor.BLUE
            ColorTypeMessage.RED -> PotColor.RED
            ColorTypeMessage.PURPLE -> PotColor.PURPLE
            ColorTypeMessage.BLACK -> PotColor.GRAY
        }
    }

    /**
     * Converts a pair of [GoalTileTypeMessage] and [Int] to [GoalCard]
     *
     * @param goalCard pair of [GoalTileTypeMessage] and [Int] to convert
     *
     * @return converted [GoalCard]
     */
    fun toGoal(goalCard: Pair<GoalTileTypeMessage, Int>): GoalCard {
        return when (goalCard.first) {
            GoalTileTypeMessage.GREEN -> toGreenGoal(goalCard)
            GoalTileTypeMessage.BROWN -> toBrownGoal(goalCard)
            GoalTileTypeMessage.PINK -> toRedGoal(goalCard)
            GoalTileTypeMessage.ORANGE -> toOrangeGoal(goalCard)
            GoalTileTypeMessage.BLUE -> toBlueGoal(goalCard)
        }
    }

    private fun toGreenGoal(goalCard: Pair<GoalTileTypeMessage, Int>): GoalCard {
        return when (goalCard.second) {
            0 -> GoalCard(6, GoalColor.GREEN, GoalDifficulty.LOW)
            1 -> GoalCard(9, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE)
            2 -> GoalCard(12, GoalColor.GREEN, GoalDifficulty.HARD)
            else -> { error("invalid goal tier") }
        }
    }

    private fun toBrownGoal(goalCard: Pair<GoalTileTypeMessage, Int>): GoalCard {
        return when (goalCard.second) {
            0 -> GoalCard(5, GoalColor.BROWN, GoalDifficulty.LOW)
            1 -> GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE)
            2 -> GoalCard(15, GoalColor.BROWN, GoalDifficulty.HARD)
            else -> { error("invalid goal tier") }
        }
    }

    private fun toRedGoal(goalCard: Pair<GoalTileTypeMessage, Int>): GoalCard {
        return when (goalCard.second) {
            0 -> GoalCard(8, GoalColor.RED, GoalDifficulty.LOW)
            1 -> GoalCard(12, GoalColor.RED, GoalDifficulty.INTERMEDIATE)
            2 -> GoalCard(16, GoalColor.RED, GoalDifficulty.HARD)
            else -> { error("invalid goal tier") }
        }
    }

    private fun toOrangeGoal(goalCard: Pair<GoalTileTypeMessage, Int>): GoalCard {
        return when (goalCard.second) {
            0 -> GoalCard(9, GoalColor.ORANGE, GoalDifficulty.LOW)
            1 -> GoalCard(11, GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE)
            2 -> GoalCard(13, GoalColor.ORANGE, GoalDifficulty.HARD)
            else -> { error("invalid goal tier") }
        }
    }

    private fun toBlueGoal(goalCard: Pair<GoalTileTypeMessage, Int>): GoalCard {
        return when (goalCard.second) {
            0 -> GoalCard(7, GoalColor.BLUE, GoalDifficulty.LOW)
            1 -> GoalCard(10, GoalColor.BLUE, GoalDifficulty.INTERMEDIATE)
            2 -> GoalCard(14, GoalColor.BLUE, GoalDifficulty.HARD)
            else -> { error("invalid goal tier") }
        }
    }

    /**
     * Converts [TileTypeMessage] to [TileType]
     *
     * @param tileType [TileTypeMessage] to convert
     *
     * @return converted [TileType]
     */
    fun toTileType(tileType: TileTypeMessage): TileType {
        return when (tileType) {
            TileTypeMessage.WOOD -> TileType.WOOD
            TileTypeMessage.LEAF -> TileType.LEAF
            TileTypeMessage.FLOWER -> TileType.FLOWER
            TileTypeMessage.FRUIT -> TileType.FRUIT
        }
    }

    /**
     * Converts [GoalTileTypeMessage] to [GoalColor]
     *
     * @param goalColor [GoalTileTypeMessage] to convert
     *
     * @return converted [GoalColor]
     */
    fun toGoalColor(goalColor: GoalTileTypeMessage): GoalColor {
        return when (goalColor) {
            GoalTileTypeMessage.ORANGE -> GoalColor.ORANGE
            GoalTileTypeMessage.BLUE -> GoalColor.BLUE
            GoalTileTypeMessage.GREEN -> GoalColor.GREEN
            GoalTileTypeMessage.BROWN -> GoalColor.BROWN
            GoalTileTypeMessage.PINK -> GoalColor.RED
        }
    }

}
