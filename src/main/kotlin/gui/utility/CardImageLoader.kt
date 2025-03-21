package gui.utility

import entity.*
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.util.*

private const val CARDS_FILE = "zen_cards.jpg"

private const val IMG_HEIGHT = 260.25
private const val IMG_WIDTH = 186

/**
 * Provides access to the src/main/resources/zen_cards.jpg file that contains all card images in a raster. The returned
 * [ImageVisual] objects of [frontImageFor] and [backImage] are 372x520 pixels.
 */
class CardImageLoader {

    /** Provides the backside image of the [ZenCard]s */
    val backImage: ImageVisual get() = getImageByCoordinates(Pair(0, 0))

    private val masterCards: List<List<TileType>> =
        listOf(
            listOf(TileType.GENERIC),
            listOf(TileType.WOOD, TileType.WOOD),
            listOf(TileType.LEAF, TileType.LEAF),
            listOf(TileType.LEAF, TileType.FRUIT),
            listOf(TileType.LEAF, TileType.FLOWER),
            listOf(TileType.WOOD, TileType.LEAF),
            listOf(TileType.WOOD, TileType.LEAF, TileType.FRUIT),
            listOf(TileType.WOOD, TileType.LEAF, TileType.FLOWER),
            listOf(TileType.LEAF, TileType.FLOWER, TileType.FLOWER),
        )

    private val helperCards: List<List<TileType>> =
        listOf(
            listOf(TileType.GENERIC, TileType.FLOWER),
            listOf(TileType.GENERIC, TileType.FRUIT),
            listOf(TileType.GENERIC, TileType.WOOD),
            listOf(TileType.GENERIC, TileType.LEAF),
        )

    /**
     * Get [ImageVisual] based on [ZenCard]
     * @param card The [ZenCard] for which the [ImageVisual] is to be returned.
     *
     * @return [ImageVisual] with the correct visual for the given [card].
     *
     * @throws IllegalArgumentException If the card is unknown.
     */
    fun frontImageFor(card: ZenCard) =
        when (card) {
            PlaceholderCard -> Visual.EMPTY
            else -> getImageByCoordinates(getImageCoordinates(card))
        }

    private fun getImageByCoordinates(coordinates: Pair<Int, Int>) =
        ImageVisual(
            CARDS_FILE,
            width = IMG_WIDTH,
            height = IMG_HEIGHT.toInt(),
            coordinates.first * IMG_WIDTH,
            (coordinates.second * IMG_HEIGHT).toInt(),
        ).apply {
            style.borderRadius = BorderRadius.MEDIUM
        }

    private fun getImageCoordinates(card: ZenCard): Pair<Int, Int> =
        when (card) {
            is GrowthCard -> coordinateGrowthCards(card.type)
            is ToolCard -> Pair(1, 0)
            is HelperCard -> coordinateHelperCards(card.tiles)
            is MasterCard -> coordinateMasterCards(card.tiles)
            is ParchmentCard ->
                when (card.type) {
                    ParchmentCardType.FLOWER -> Pair(1, 3)
                    ParchmentCardType.FRUIT -> Pair(2, 3)
                    ParchmentCardType.WOOD -> Pair(3, 3)
                    ParchmentCardType.LEAF -> Pair(4, 3)
                    ParchmentCardType.HELPER -> Pair(5, 3)
                    ParchmentCardType.MASTER -> Pair(6, 3)
                    ParchmentCardType.GROWTH -> Pair(7, 3)
                }
            else -> throw IllegalArgumentException("Unknown card")
        }

    private fun coordinateMasterCards(tiles: List<TileType>): Pair<Int, Int> =
        when (tiles) {
            masterCards[0] -> Pair(4, 1)
            masterCards[1] -> Pair(5, 1)
            masterCards[2] -> Pair(6, 1)
            masterCards[3] -> Pair(7, 1)
            masterCards[4] -> Pair(8, 1)
            masterCards[5] -> Pair(0, 2)
            masterCards[6] -> Pair(1, 2)
            masterCards[7] -> Pair(3, 2)
            masterCards[8] -> Pair(4, 2)
            masterCards[9] -> Pair(5, 2)
            else -> Pair(0, 0)
        }

    private fun coordinateHelperCards(tiles: List<TileType>): Pair<Int, Int> =
        when (tiles) {
            helperCards[0] -> Pair(6, 2)
            helperCards[1] -> Pair(7, 2)
            helperCards[2] -> Pair(8, 2)
            helperCards[3] -> Pair(0, 3)
            else -> Pair(0, 0)
        }

    private fun coordinateGrowthCards(type: TileType): Pair<Int, Int> =
        when (type) {
            TileType.WOOD -> Pair(4, 0)
            TileType.LEAF -> Pair(7, 0)
            TileType.FLOWER -> Pair(0, 1)
            TileType.FRUIT -> Pair(2, 1)
            else -> Pair(0, 0)
        }
}
