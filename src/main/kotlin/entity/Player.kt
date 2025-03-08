package entity

import tools.aqua.bgw.util.Stack
import java.util.Dictionary

abstract class Player(
    name: String,
    supplyTileLimit: Int,
    treeTileLimit: Dictionary<TileType, Int>,
    declinedGoals: List<GoalCard>,
    seishiTool: Stack<ZenCard>,
    seishiGrowth: Stack<ZenCard>,
    hiddenDeck: List<ZenCard>,
    supply: List<BonsaiTile>,
    bonsai: Bonsai
)
