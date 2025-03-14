package entity

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity to represent the card type "Helper"
 *
 * @property tiles Saves, which tiles can be used for the action of the card.
 * * @property hasPlacedChosenTile Indicates whether the player has placed a tile of their choice.
 *  * @property hasPlacedShownTile Indicates whether the player has placed the tile shown on the card.
 */
@Serializable
data class HelperCard @OptIn(ExperimentalUuidApi::class) constructor(
    val tiles: List<TileType>,
    private val uuid: Uuid = Uuid.random()
)
