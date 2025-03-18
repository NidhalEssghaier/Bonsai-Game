package entity

import kotlinx.serialization.Serializable

/**
 * Entity to represent the card type "Helper"
 *
 * @property tiles Saves, which tiles can be used for the action of the card.
 */
@Serializable
data class HelperCard(
    val tiles: List<TileType>,
    val id: Int
): ZenCard
