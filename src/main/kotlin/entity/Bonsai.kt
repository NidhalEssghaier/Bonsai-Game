package entity

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
    val grid: HexagonGrid<HexagonView>,
    val map: BidirectionalMap<HexagonView, BonsaiTile>,
    val tileCount: Map<TileType,Int>,
    val tiles: MutableList<BonsaiTile>
)
