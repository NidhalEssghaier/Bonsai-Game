package entity

import tools.aqua.bgw.util.Stack

class BonsaiGame(
    currentPlayer: Int,
    endGameCounter: Int,
    undoStack: Stack<BonsaiGame>,
    redoStack: Stack<BonsaiGame>,
    gameSpeed: Int,
    drawStack: Stack<ZenCard>,
    openCards: List<ZenCard>,
    players: List<Player>,
    goalCards: List<GoalCard>
)
