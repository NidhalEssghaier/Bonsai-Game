package entity

import helper.copy
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.util.BidirectionalMap

/**
 * Entity to represent the bonsai of a player
 *
 * @property grid Saves the positions of tiles in the bonsai.
 * @property tiles Saves used tiles as list.
 */
class Bonsai(
    val grid: HexagonGrid<HexagonView> = HexagonGrid(coordinateSystem=HexagonGrid.CoordinateSystem.AXIAL),
    val map: BidirectionalMap<HexagonView, BonsaiTile> = BidirectionalMap(),
    val tileCount: MutableMap<TileType,Int> = TileType.entries.associateWith { 0 }.toMutableMap()
)
{
    val tiles = {map.keysBackward.toList()}

    fun copy(): Bonsai {
        val copy = Bonsai(grid.copy(), map.copy(), tileCount.toMutableMap())

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
