package entity

import kotlinx.serialization.Serializable

/**
 * Entity to represent the bonsai of a player
 *
 * @property grid Store the [BonsaiTile] and its coordinates in the bonsai.
 * @property tileCount Saves the count of each tile type.
 * @property tiles Returns the list of all tiles in the bonsai.
 */
@Serializable
class Bonsai private constructor(
    val grid: HexGrid,
    val tileCount: MutableMap<TileType,Int>
)
{
    /**
     * Public secondary constructor for external usage
     */
    constructor(): this(
        grid = HexGrid(20),
        tileCount = TileType.entries.associateWith{ type -> if (type == TileType.WOOD) 1 else 0 }.toMutableMap()
    )

    val tiles = grid.tilesList

    /**
     * Method to deep copy the bonsai
     *
     * @return [Bonsai] deep copy of the bonsai
     */
    fun copy(): Bonsai {
        return Bonsai(grid.copy(), tileCount.toMutableMap())
    }
}
