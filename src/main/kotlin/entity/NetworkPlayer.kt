package entity

import helper.copy
import tools.aqua.bgw.util.Stack

/**
 * Entity to represent the player type "Network"
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
class NetworkPlayer(
    name: String,

    bonsai: Bonsai = Bonsai(),
    supplyTileLimit: Int = 5,
    treeTileLimit: MutableMap<TileType, Int> = mutableMapOf(),
    declinedGoals: MutableList<GoalCard> = mutableListOf(),
    acceptedGoals: MutableList<GoalCard> = mutableListOf(),
    seishiTool: Stack<ZenCard> = Stack(),
    seishiGrowth: Stack<ZenCard> = Stack(),
    hiddenDeck: MutableList<ZenCard> = mutableListOf(),
    supply: MutableList<BonsaiTile> = mutableListOf()
): Player(
    name,
    bonsai,
    supplyTileLimit,
    treeTileLimit,
    declinedGoals,
    acceptedGoals,
    seishiTool,
    seishiGrowth,
    hiddenDeck,
    supply
)
{
    override fun copy(): NetworkPlayer {
        return NetworkPlayer(
            name,
            bonsai.copy(),
            supplyTileLimit,
            treeTileLimit.toMutableMap(),
            declinedGoals.toMutableList(),
            acceptedGoals.toMutableList(),
            seishiTool.copy(),
            seishiGrowth.copy(),
            hiddenDeck.toMutableList(),
            supply.toMutableList()
        )
    }
}
