package entity

import kotlinx.serialization.Serializable

/**
 * Entity to represent a bonsai tile
 *
 * @property type The type of the tile (e.g. wood)
 */
@Serializable
class BonsaiTile(
    val type: TileType,
)
{
    /**
     * Copy function to create a new instance of the tile
     * @return The new instance of the [BonsaiTile]
     */
    fun copy(): BonsaiTile {
        return BonsaiTile(type)
    }
}
