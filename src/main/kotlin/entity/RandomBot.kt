package entity

import tools.aqua.bgw.util.Stack

/**
 * Entity to represent a bot, based on random behaviour
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
class RandomBot(
    name: String,
    supplyTileLimit: Int,
    treeTileLimit: Map<TileType, Int>,
    declinedGoals: List<GoalCard>,
    seishiTool: Stack<ZenCard>,
    seishiGrowth: Stack<ZenCard>,
    hiddenDeck: List<ZenCard>,
    supply: List<BonsaiTile>,
    bonsai: Bonsai,
) : Player(
        name,
        supplyTileLimit,
        treeTileLimit,
        declinedGoals,
        seishiTool,
        seishiGrowth,
        hiddenDeck,
        supply,
        bonsai,
    )
