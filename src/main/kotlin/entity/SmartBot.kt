package entity

import helper.copy
import tools.aqua.bgw.util.Stack

/**
 * Entity to represent a bot, based on a smart algorithm
 *
 * @property name The name of the player
 * @property supplyTileLimit The maximum number of tiles allowed in the personal inventory
 * @property treeTileLimit The maximum number of tiles allowed to be placed in the bonsai
 * @property declinedGoals The goals the player declined
 * @property acceptedGoals The goals the player accepted
 * @property seishiTool The tool cards the player has acquired
 * @property seishiGrowth The growth cards the player has acquired
 * @property hiddenDeck The cards the player has already used
 * @property supply The tile inventory of the player
 * @property bonsai The bonsai of the player
 */
class SmartBot(
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
    /**
     * Make a deep copy of the SmartBot instance
     * @return The deep copy of the SmartBot instance
     */
    override fun copy(): SmartBot {
        return SmartBot(
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