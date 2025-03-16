package entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entity to represent the card type "Growth"
 *
 * @property type The player can place more tiles of this type in the bonsai.
 */
@Serializable
data class GrowthCard(
    @SerialName("TileType")
    val type: TileType,
    val id: Int
) : ZenCard