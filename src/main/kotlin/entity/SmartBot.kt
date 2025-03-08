package entity

import tools.aqua.bgw.util.Stack
import java.util.*

class SmartBot(
    name: String,
    supplyTileLimit: Int,
    treeTileLimit: Dictionary<TileType, Int>,
    declinedGoals: List<GoalCard>,
    seishiTool: Stack<ZenCard>,
    seishiGrowth: Stack<ZenCard>,
    hiddenDeck: List<ZenCard>,
    supply: List<BonsaiTile>,
    bonsai: Bonsai
) : Player(
    name,
    supplyTileLimit,
    treeTileLimit,
    declinedGoals,
    seishiTool,
    seishiGrowth,
    hiddenDeck,
    supply,
    bonsai
)
