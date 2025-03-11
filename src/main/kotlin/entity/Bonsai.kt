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
    val tileCount: MutableMap<TileType,Int> = TileType.entries.associateWith { 0 }.toMutableMap(),
    val tiles: MutableList<BonsaiTile> = mutableListOf()
)
{
    fun copy(): Bonsai {
        return Bonsai(
            grid.copy(),
            map.copy(),
            tileCount.toMutableMap(),
            tiles.toMutableList()
        )
    }
}
