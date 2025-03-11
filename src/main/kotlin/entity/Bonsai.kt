package entity

import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView

/**
 * Entity to represent the bonsai of a player
 *
 * @property grid Saves the positions of tiles in the bonsai.
 * @property tiles Saves used tiles as list.
 */
class Bonsai(
    val grid: Map<HexagonGrid<HexagonView>,BonsaiTile>,
    val tiles: Map<TileType,Int>
)
