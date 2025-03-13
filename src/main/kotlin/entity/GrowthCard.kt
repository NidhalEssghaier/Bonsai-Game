package entity

import kotlinx.serialization.Serializable

/**
 * Entity to represent the card type "Growth"
 *
 * @property type The player can place more tiles of this type in the bonsai.
 */
@Serializable
class GrowthCard(val type: TileType) : ZenCard
