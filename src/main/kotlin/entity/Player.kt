package entity

import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.util.Stack

/**
 * Abstract Entity to represent the player
 *
 * @property name The name of the player
 * @property supplyTileLimit The maximum number of tiles allowed in the personal inventory
 * @property treeTileLimit The maximum number of tiles allowed to be placed in the bonsai
 * @property declinedGoals The goals the player declined
 * @property seishiTool The tool cards the player has acquired
 * @property seishiGrowth The growth cards the player has acquired
 * @property hiddenDeck The cards the player has already used
 * @property supply The tile inventory of the player
 * @property bonsai The bonsai of the player
 */
abstract class Player(
    val name: String
    )
{
    var bonsai: Bonsai = Bonsai(mutableMapOf<HexagonGrid<HexagonView>,BonsaiTile>(), emptyMap<TileType,Int>(), mutableListOf<BonsaiTile>())
    var supplyTileLimit: Int = 5
    var treeTileLimit: Map<TileType, Int> = mapOf()
    var declinedGoals: MutableList<GoalCard> = mutableListOf()
    val acceptedGoals: MutableList<GoalCard> = mutableListOf()
    val forbiddenGoals: MutableList<GoalCard> = mutableListOf()
    var seishiTool: Stack<ZenCard> = Stack()
    var seishiGrowth: Stack<ZenCard> = Stack()
    var hiddenDeck: MutableList<ZenCard> = mutableListOf()
    var supply: MutableList<BonsaiTile> = mutableListOf()
}