package entity

import helper.copy
import helper.deepBonsaiTileCopy
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.util.BidirectionalMap

/**
 * Entity to represent the bonsai of a player
 *
 * @property grid Store the [HexagonView] and its coordinates in the bonsai.
 * @property map [BidirectionalMap] to store the mapping between [HexagonView] and [BonsaiTile].
 * @property tileCount Saves the count of each tile type.
 * @property tiles Returns the list of all tiles in the bonsai.
 */
class Bonsai(
    val grid: HexagonGrid<HexagonView> = HexagonGrid(coordinateSystem=HexagonGrid.CoordinateSystem.AXIAL),
    val map: BidirectionalMap<HexagonView, BonsaiTile> = BidirectionalMap(),
    val tileCount: MutableMap<TileType,Int> =
                TileType.entries.associateWith{ type -> if (type == TileType.WOOD) 1 else 0 }.toMutableMap()
)
{
    val tiles = {map.keysBackward.toList()}

    /**
     * Method to deep copy the bonsai
     *
     * @return [Bonsai] deep copy of the bonsai
     */
    fun copy(): Bonsai {
        val copy = Bonsai(grid.copy(), map.deepBonsaiTileCopy(), tileCount.toMutableMap())

        // Fix neighbors references
        val tilesListCopy = copy.tiles()
        for (copiedTile in tilesListCopy) {
            val newNeighbors = copiedTile.neighbors.map { old -> copy.map.forward(map.backward(old)) }
            copiedTile.neighbors.clear()
            copiedTile.neighbors.addAll(newNeighbors)
        }
        return copy
    }
}
