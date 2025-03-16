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
        val path =
            when {
                goalCard.points == 5 && goalCard.color == GoalColor.BROWN && goalCard.difficulty == GoalDifficulty.LOW
                -> "goal_cards/goal_log_low.jpg"
                goalCard.points == 10 &&
                    goalCard.color == GoalColor.BROWN &&
                    goalCard.difficulty == GoalDifficulty.INTERMEDIATE
                -> "goal_cards/goal_log_intermediate.jpg"
                goalCard.points == 15 && goalCard.color == GoalColor.BROWN && goalCard.difficulty == GoalDifficulty.HARD
                -> "goal_cards/goal_log_hard.jpg"
                goalCard.points == 6 && goalCard.color == GoalColor.GREEN && goalCard.difficulty == GoalDifficulty.LOW
                -> "goal_cards/goal_leaf_low.jpg"
                goalCard.points == 9 &&
                    goalCard.color == GoalColor.GREEN &&
                    goalCard.difficulty == GoalDifficulty.INTERMEDIATE
                -> "goal_cards/goal_leaf_intermediate.jpg"
                goalCard.points == 12 && goalCard.color == GoalColor.GREEN && goalCard.difficulty == GoalDifficulty.HARD
                -> "goal_cards/goal_leaf_hard.jpg"
                goalCard.points == 9 && goalCard.color == GoalColor.ORANGE && goalCard.difficulty == GoalDifficulty.LOW
                -> "goal_cards/goal_fruit_low.jpg"
                goalCard.points == 11 &&
                    goalCard.color == GoalColor.ORANGE &&
                    goalCard.difficulty == GoalDifficulty.INTERMEDIATE
                -> "goal_cards/goal_fruit_intermediate.jpg"
                goalCard.points == 13 &&
                    goalCard.color == GoalColor.ORANGE &&
                    goalCard.difficulty == GoalDifficulty.HARD
                -> "goal_cards/goal_fruit_hard.jpg"
                goalCard.points == 8 && goalCard.color == GoalColor.RED && goalCard.difficulty == GoalDifficulty.LOW
                -> "goal_cards/goal_flower_protruding_low.jpg"
                goalCard.points == 12 &&
                    goalCard.color == GoalColor.RED &&
                    goalCard.difficulty == GoalDifficulty.INTERMEDIATE
                -> "goal_cards/goal_flower_protruding_intermediate.jpg"
                goalCard.points == 16 && goalCard.color == GoalColor.RED && goalCard.difficulty == GoalDifficulty.HARD
                -> "goal_cards/goal_flower_protruding_hard.jpg"
                goalCard.points == 7 && goalCard.color == GoalColor.BLUE && goalCard.difficulty == GoalDifficulty.LOW
                -> "goal_cards/goal_universal_protruding_low.jpg"
                goalCard.points == 10 &&
                    goalCard.color == GoalColor.BLUE &&
                    goalCard.difficulty == GoalDifficulty.INTERMEDIATE
                -> "goal_cards/goal_universal_protruding_intermediate.jpg"
                goalCard.points == 14 && goalCard.color == GoalColor.BLUE && goalCard.difficulty == GoalDifficulty.HARD
                -> "goal_cards/goal_universal_protruding_hard.jpg"
                else -> throw IllegalArgumentException(
                    "Unknown goal card: ${goalCard.points}, ${goalCard.color}, ${goalCard.difficulty}",
                )
            }
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
                TileType.GENERIC -> throw IllegalArgumentException("Not a playable tile type")
                TileType.UNPLAYABLE -> throw IllegalArgumentException("Not a playable tile type")
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
}
