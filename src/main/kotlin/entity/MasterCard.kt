package entity

import kotlinx.serialization.Serializable

/**
 * Entity to represent the card type "Master"
 *
 * @property tiles The tiles the player is allowed to take with this card
 */
@Serializable
data class MasterCard(
    val tiles: List<TileType>,
    val id: Int
) : ZenCard
