package gui.utility

import entity.*
import tools.aqua.bgw.visual.ImageVisual

/**
 * Provides access to the images of the game components cotained in src/main/resources/\*
 * and returns those as [ImageVisual]s.
 */
class ItemImageLoader {
    /**
     * Get [ImageVisual] based on [GoalCard]
     * @param goalCard The [GoalCard] for which the [ImageVisual] is to be returned.
     *
     * @return [ImageVisual] with the correct visual for the given [goalCard].
     *
     * @throws IllegalArgumentException If the card is unknown.
     */
    fun imageFor(goalCard: GoalCard): ImageVisual {
        val path = goalCardImagePath[Triple(
            goalCard.color,
            goalCard.difficulty,
            goalCard.points
        )] ?: throw IllegalArgumentException("Unknown goal card: ${goalCard.points}, " +
                "${goalCard.color}, " +
                "${goalCard.difficulty}")
        val originalWith =
            when (goalCard.difficulty) {
                GoalDifficulty.LOW -> 125
                GoalDifficulty.INTERMEDIATE -> 139
                GoalDifficulty.HARD -> 179
            }

        return ImageVisual(path, originalWith, 80)
    }

    /**
     * Get [ImageVisual] based on [BonsaiTile]
     * @param tile The [BonsaiTile] for which the [ImageVisual] is to be returned.
     * @param grayScale Determines if the returned [ImageVisual] is in gray scale
     *
     * @return [ImageVisual] with the correct visual for the given [tile].
     *
     * @throws IllegalArgumentException If the tile is of generic type.
     */
    fun imageFor(
        tile: BonsaiTile,
        grayScale: Boolean = false,
    ): ImageVisual {
        var grayScaleAddition = ""
        if (grayScale) {
            grayScaleAddition = "_gray"
        }

        val path =
            when (tile.type) {
                TileType.WOOD -> "tiles/log_piece$grayScaleAddition.png"
                TileType.LEAF -> "tiles/leaf_piece$grayScaleAddition.png"
                TileType.FLOWER -> "tiles/flower_piece$grayScaleAddition.png"
                TileType.FRUIT -> "tiles/fruit_piece$grayScaleAddition.png"
                else -> throw IllegalArgumentException("Not a playable tile type")
            }

        return ImageVisual(path, 100, 115)
    }

    /**
     * Get [ImageVisual] based on [path]
     * @param path The path for which the [ImageVisual] is to be returned.
     * @param width The width of the original image
     * @param height The height of the original image
     *
     * @return [ImageVisual] with the correct visual for the given [path] and size [width] x [height].
     *
     * @throws IllegalArgumentException If the tile is of generic type.
     */
    fun imageFor(
        path: String,
        width: Int,
        height: Int,
    ): ImageVisual = ImageVisual(path, width, height)

    private companion object {
        private val goalCardImagePath = mapOf(
            Triple(GoalColor.BROWN, GoalDifficulty.LOW, 5) to "goal_cards/goal_log_low.jpg",
            Triple(GoalColor.BROWN, GoalDifficulty.INTERMEDIATE, 10) to "goal_cards/goal_log_intermediate.jpg",
            Triple(GoalColor.BROWN, GoalDifficulty.HARD, 15) to "goal_cards/goal_log_hard.jpg",
            Triple(GoalColor.GREEN, GoalDifficulty.LOW, 6) to "goal_cards/goal_leaf_low.jpg",
            Triple(GoalColor.GREEN, GoalDifficulty.INTERMEDIATE, 9) to "goal_cards/goal_leaf_intermediate.jpg",
            Triple(GoalColor.GREEN, GoalDifficulty.HARD, 12) to "goal_cards/goal_leaf_hard.jpg",
            Triple(GoalColor.ORANGE, GoalDifficulty.LOW, 9) to "goal_cards/goal_fruit_low.jpg",
            Triple(GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE, 11) to "goal_cards/goal_fruit_intermediate.jpg",
            Triple(GoalColor.ORANGE, GoalDifficulty.HARD, 13) to "goal_cards/goal_fruit_hard.jpg",
            Triple(GoalColor.RED, GoalDifficulty.LOW, 8) to "goal_cards/goal_flower_protruding_low.jpg",
            Triple(GoalColor.RED, GoalDifficulty.INTERMEDIATE, 12) to
                    "goal_cards/goal_flower_protruding_intermediate.jpg",
            Triple(GoalColor.RED, GoalDifficulty.HARD, 16) to "goal_cards/goal_flower_protruding_hard.jpg",
            Triple(GoalColor.BLUE, GoalDifficulty.LOW, 7) to "goal_cards/goal_universal_protruding_low.jpg",
            Triple(GoalColor.BLUE, GoalDifficulty.INTERMEDIATE, 10) to
                    "goal_cards/goal_universal_protruding_intermediate.jpg",
            Triple(GoalColor.BLUE, GoalDifficulty.HARD, 14) to "goal_cards/goal_universal_protruding_hard.jpg",
        )
    }
}
