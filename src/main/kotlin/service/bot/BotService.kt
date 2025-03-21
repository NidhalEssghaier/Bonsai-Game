package service.bot

import entity.*
import entity.bot.*
import service.*
import kotlin.math.ln
import kotlin.math.sqrt
import  helper.TileUtils
import java.util.*
import kotlin.concurrent.timerTask

class BotService(private val rootService:RootService) : AbstractRefreshingService() {

    private val playerActionService = rootService.playerActionService
    /*fun playMove(lmove: PlayerAction?, simulation: Boolean) : Int? {
        /*val game = rootService.currentGame
        checkNotNull(game)
        val players = game.players
        println("play move: P1 points: ${players[0].points}, P2 points: ${players[1].points}")*/
        val playerActionService = PlayerActionService(rootService)
        var move = lmove
        if(move == null) {
            move = PlayerAction.entries.random()
        }
        var endGameStatus: Int? = null
        endGameStatus = when(move) {
            /*PlayerAction.INC1 -> playerActionService.increaseBy1(simulation)
            PlayerAction.INC2 -> playerActionService.increaseBy2(simulation)
            PlayerAction.INC5 -> playerActionService.increaseBy5(simulation)*/
            PlayerAction.MEDITATE -> meditateLogic()
            PlayerAction.CULTIVATE -> cultivateLogic()
        }
        return endGameStatus
    }*/

    fun playRandomMove() {
        val move = PlayerAction.entries.random()
        println("move: $move")

        when(move) {
            PlayerAction.MEDITATE -> meditateLogic()
            PlayerAction.CULTIVATE -> cultivateLogic()
        }
        println("Done: playRandomMove")
        /*var time = System.currentTimeMillis()
        while(System.currentTimeMillis() < time + 7000) {

        }*/
        val timer = Timer()
        timer.schedule(timerTask {playerActionService.endTurn()},10000L)
        println("executed endTurn()")
    }

    fun meditateLogic() {
        println("meditate logic")
        val game = rootService.currentGame
        checkNotNull(game)
        val selectedCard = game.currentState.openCards.random()
        playerActionService.meditate(selectedCard)
    }

    fun cultivateLogic() {
        println("cultivate logic")
        val game = rootService.currentGame
        checkNotNull(game)
        val player = game.currentState.players[game.currentState.currentPlayer]
        val bonsaiGrid = player.bonsai.grid
        var completed = false
        var q = -1
        var r = -1
        var tile: BonsaiTile? = null
        for(supplyTile in player.supply) {
            if(supplyTile.type == TileType.WOOD) {
                try {
                    val grid = player.bonsai.grid
                    val currentPlayerBonsaiTiles = player.bonsai.tiles()
                    val leastGroupOfTilesToBeRemoved =
                                TileUtils.leastGroupOfTilesToBeRemoved(currentPlayerBonsaiTiles,grid)
                    if(leastGroupOfTilesToBeRemoved.isNotEmpty()) {
                        playerActionService.removeTile(leastGroupOfTilesToBeRemoved.random())
                    }
                } catch (_: IllegalStateException) {

                }
            }
            println("Current supplyTile: ${supplyTile.type}")
            for(bonsaiTile in bonsaiGrid.getInternalMap().keys) {
                val emptySpaces = bonsaiGrid.getEmptySpace(bonsaiTile)
                if(emptySpaces.isNotEmpty()) {
                    val randomFreeSpace = emptySpaces.random()


                    val seishiAllowedTiles = player.treeTileLimit.keys // The 3 permanent Seishi tile types
                    // Tile types granted by Growth Cards
                    val growthAllowedTiles = player.seishiGrowth.map { (it as? GrowthCard)?.type }

                    val allowedTiles = seishiAllowedTiles + growthAllowedTiles
                    val isPlacementAllowed = supplyTile.type in allowedTiles

                    if (isPlacementAllowed) {
                        when (supplyTile.type) {
                            TileType.WOOD, TileType.LEAF -> {
                                if (bonsaiTile.type == TileType.WOOD) {
                                    tile = supplyTile
                                    q = randomFreeSpace.first
                                    r = randomFreeSpace.second
                                    completed = true
                                    println("completed")
                                }
                            }

                            TileType.FLOWER -> {
                                if (bonsaiTile.type == TileType.LEAF) {
                                    tile = supplyTile
                                    q = randomFreeSpace.first
                                    r = randomFreeSpace.second
                                        completed = true
                                        println("completed")
                                }
                            }

                            TileType.FRUIT -> {
                                val neighbors = bonsaiGrid.getNeighbors(bonsaiTile)
                                val leafNeighbor = neighbors.firstOrNull { it.type == TileType.LEAF }
                                if (bonsaiTile.type == TileType.LEAF && leafNeighbor != null) {
                                    val freeSpacesOfLeaf = bonsaiGrid.getEmptySpace(leafNeighbor).toSet()
                                    val combinedFreeSpaces = emptySpaces.intersect(freeSpacesOfLeaf)
                                    if (combinedFreeSpaces.isNotEmpty()) {
                                        val fruitPlace = combinedFreeSpaces.random()
                                        tile = supplyTile
                                        q = fruitPlace.first
                                        r = fruitPlace.second
                                        completed = true
                                        println("completed")
                                    }
                                }
                            }

                            else -> {}
                        }
                    } else {
                        println(supplyTile.type)
                    }
                }
                if(completed) {
                    break
                }
            }
        }
        if(!completed) {
            meditateLogic()
        } else {
            checkNotNull(tile)
            playerActionService.cultivate(tile,q,r)
        }
    }

    fun chooseCardLogic(chooseTilesByBoard: Boolean, chooseTilesByCard: Boolean) {
        if (chooseTilesByBoard) {
            val selectedTileByBoardType = TileType.entries.subList(0,2).random()
            rootService.playerActionService.applyTileChoice(selectedTileByBoardType, false)
        }
        if (chooseTilesByCard) {
            val selectedTileByCardType = TileType.entries.random()
            rootService.playerActionService.applyTileChoice(selectedTileByCardType, true)
        }
    }

    fun discardTileLogic(tilesToDiscard: Int) {
        val game = rootService.currentGame
        checkNotNull(game)
        val player = game.currentState.players[game.currentState.currentPlayer]
        repeat(tilesToDiscard) {
            val removedTile = player.supply.random()
            playerActionService.discardTile(removedTile)
        }
        onAllRefreshables { refreshAfterDiscardTile(0,null) }
        playerActionService.endTurn()
    }







    /*fun calculateNextMove(node: Node) : Int {
        //println("calculate next move")
        val game = rootService.currentGame
        checkNotNull(game)
        val nodePlayers = node.gameState.players
        val nodePoints0 = nodePlayers[0].points
        val nodePoints1 = nodePlayers[1].points
        repeat(10) {
            /*val initPlayer0 = Player(nodePoints0,0)
            val initPlayer1 = Player(nodePoints1,1)
            val initPlayers = listOf(initPlayer0,initPlayer1)*/
            calculateIteration(node)
            //game.players = initPlayers
        }
        var bestPath: Pair<Int,Int> = Pair(-1,0)
        for((index, child) in node.children.withIndex()) {
            checkNotNull(child)
            if(child.winRatio.second > bestPath.second) {
                bestPath = Pair(index,child.winRatio.second)
            }
        }
        val calculatedMove = node.children[bestPath.first]?.move
        println("Move: $calculatedMove")
        playMove(calculatedMove,false)

        return bestPath.first
    }

    fun calculateIteration(node: Node) : Int {
        //println("calculate iteration")
        var result: Int? = null
        if(node.isLeaf()) {
            //println("if1")
            result = playMove(node.move,true)
            if(result == null) {
                //println("if1.1")
                result = expansion(node)
            }
        } else {
            //println("else")
            val selected = selection(node)
            checkNotNull(selected)
            result = calculateIteration(selected)
        }
        //println("result: $result")
        //println("end")
        checkNotNull(result)
        updateNode(node,result)
        return result
    }

    fun selection(node: Node) : Node? {
        //println("selection")
        //find node to select
        var bestPathIndex = 0
        val resultList = mutableListOf<Double>()
        for((index, child) in node.children.withIndex()) {
            checkNotNull(child)
            if(child.winRatio.second != 0) {
                val exploitation = (child.winRatio.first / child.winRatio.second)
                val constant = sqrt(2.0)
                val exploration = sqrt((ln(node.winRatio.second.toDouble())) / child.winRatio.second)
                val calculatedValue = exploitation + constant * exploration
                println("win: ${child.winRatio.first}")
                println("simulations: ${child.winRatio.second}")
                println("calculatedValue = $calculatedValue")
                resultList.add(calculatedValue)
                if (calculatedValue > resultList[bestPathIndex]) {
                    bestPathIndex = index
                }
            }
        }
        val selectedChild = node.children[bestPathIndex]
        playMove(selectedChild?.move,true)
        return selectedChild
    }

    fun expansion(node: Node) : Int {
        //println("expansion")
        val newPlayerNumber = 1 - node.playerNumber
        //val parentNodeGame = node.gameState
        for(move in PlayerAction.entries) {
            playMove(move,true)
            val childNodeGame = rootService.currentGame
            checkNotNull(childNodeGame)
            node.children.add(Node(move, playerNumber = newPlayerNumber, gameState = childNodeGame))
        }
        val chosenChild = node.children.random()
        checkNotNull(chosenChild)
        return simulation(chosenChild)
    }

    fun updateNode(node: Node, result: Int) {
        //println("update node")
        if(result == node.playerNumber) {
            node.winRatio = Pair(node.winRatio.first + 1, node.winRatio.second + 1)
        } else {
            node.winRatio = Pair(node.winRatio.first, node.winRatio.second + 1)
        }
    }

    fun simulation(node: Node) : Int { //Int: Winner
        //println("simulation")
        /*if(!node.isLeaf()) {
            simulation(node)
        }*/
        var result: Int? = null
        while(result == null) {
            result = playMove(null,true)
        }
        return result
    }*/
}