package service.bot

import entity.*
import entity.bot.*
import helper.TileUtils
import service.*
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Service for bot features of RandomBot and SmartBot
 * @property rootService The RootService of the application
 */
class BotService(
    private val rootService: RootService,
) : AbstractRefreshingService() {
    private val playerActionService = rootService.playerActionService

    private fun playMoveWhenPlayerSupplyLeq2(smart: Boolean, move: PlayerAction){
        if (smart) {
            when (move) {
                PlayerAction.MEDITATE -> meditateLogic(smart)
                PlayerAction.CULTIVATE -> {
                    val possibilities = cultivateLogic(smart)
                    if (!possibilities) {
                        meditateLogic(true)
                    }
                }
            }
        } else {
            when (move) {
                PlayerAction.MEDITATE -> meditateLogic(smart)
                PlayerAction.CULTIVATE -> cultivateLogic(smart)
            }
        }
    }

    /**
     * Is used to decide whether to play cultivate or meditate
     * @param smart If the smartBot is calling the method
     */
    fun playRandomMove(smart: Boolean) {
        val move = PlayerAction.entries.random()

        val game = rootService.currentGame?.currentState
        checkNotNull(game)
        if (smart && game.players[game.currentPlayer].supply.size > 2) {
            var possibilities = cultivateLogic(true)
            if (!possibilities) {
                meditateLogic(true)
            }
            while (possibilities) {
                possibilities = cultivateLogic(true)
            }
        } else {
            playMoveWhenPlayerSupplyLeq2(smart, move)
        }

        val timer = Timer()
        val speed = 1000 * game.gameSpeed
        timer.schedule(timerTask { playerActionService.endTurn() }, speed.toLong())
    }

    /**
     * The bot guidelines for meditate
     * @param smart If the smartBot is calling the method
     */
    fun meditateLogic(smart: Boolean) {
        val game = rootService.currentGame
        checkNotNull(game)
        var cardCollection = game.currentState.openCards
        if (smart) {
            cardCollection = cardCollection.subList(1, 4)
        }
        val selectedCard = cardCollection.random()
        playerActionService.meditate(selectedCard)
    }

    private fun tryPlaceTileWhenAllowed(
        supplyTile: BonsaiTile,
        bonsaiGrid: HexGrid,
        bonsaiTile: BonsaiTile,
        randomFreeSpace: Pair<Int, Int>,
        emptySpaces: List<Pair<Int, Int>>
    ): Triple<BonsaiTile?, Pair<Int, Int>, Boolean> {
        var tile: BonsaiTile? = null
        var q = -1
        var r = -1
        var completed = false

        when (supplyTile.type) {
            TileType.WOOD, TileType.LEAF -> {
                if (bonsaiTile.type == TileType.WOOD) {
                    tile = supplyTile
                    q = randomFreeSpace.first
                    r = randomFreeSpace.second
                    completed = true
                }
            }

            TileType.FLOWER -> {
                if (bonsaiTile.type == TileType.LEAF) {
                    tile = supplyTile
                    q = randomFreeSpace.first
                    r = randomFreeSpace.second
                    completed = true
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
                    }
                }
            }

            else -> {}
        }
        return Triple(tile, Pair(q, r), completed)
    }

    private fun tryPlaceTile(
        player: Player,
        smart: Boolean,
        possibleMoves: MutableList<Triple<BonsaiTile, Pair<Int, Int>, List<Pair<Int, Int>>>>,
        supplyTile: BonsaiTile,
        bonsaiGrid: HexGrid,
        tileList: List<BonsaiTile>
    ): Triple<BonsaiTile?, Pair<Int, Int>, Boolean> {
        var tile: BonsaiTile? = null
        var q = -1
        var r = -1
        var completed = false

        for (bonsaiTile in tileList) {
            val emptySpaces = bonsaiGrid.getEmptySpace(bonsaiTile)
            if (emptySpaces.isNotEmpty()) {
                val randomFreeSpace = emptySpaces.random()

                val seishiAllowedTiles = player.treeTileLimit.keys // The 3 permanent Seishi tile types
                // Tile types granted by Growth Cards
                val growthAllowedTiles = player.seishiGrowth.map { (it as? GrowthCard)?.type }

                val allowedTiles = seishiAllowedTiles + growthAllowedTiles
                val isPlacementAllowed = supplyTile.type in allowedTiles

                if (isPlacementAllowed) {
                    val result = tryPlaceTileWhenAllowed(
                        supplyTile,
                        bonsaiGrid,
                        bonsaiTile,
                        randomFreeSpace,
                        emptySpaces
                    )
                    if (result.third) {
                        tile = result.first
                        q = result.second.first
                        r = result.second.second
                        completed = true
                    }
                }
            }
            if (completed) {
                if (smart) {
                    checkNotNull(tile)
                    possibleMoves.add(Triple(tile, Pair(q, r), emptySpaces))
                } else {
                    break
                }
            }
        }
        return Triple(tile, Pair(q, r), completed)
    }

    /**
     * The bot guidelines for cultivate
     * @param smart If the smartBot is calling the method
     * @return Returns if there are more possibilities for cultivating
     */
    fun cultivateLogic(smart: Boolean): Boolean {
        var possibleMoves = mutableListOf<Triple<BonsaiTile, Pair<Int, Int>, List<Pair<Int, Int>>>>() // Tile, q, r
        val game = rootService.currentGame
        checkNotNull(game)
        val player = game.currentState.players[game.currentState.currentPlayer]
        val bonsaiGrid = player.bonsai.grid
        var completed = false
        var q = -1
        var r = -1
        var tile: BonsaiTile? = null
        val tileList = bonsaiGrid.tilesList()
        for (supplyTile in player.supply) {
            // completed = false
            possibleMoves = mutableListOf()
            removeTileLogic(supplyTile, player)
            val result = tryPlaceTile(player, smart, possibleMoves, supplyTile, bonsaiGrid, tileList)
            if(result.third) {
                tile = result.first
                q = result.second.first
                r = result.second.second
                completed = result.third
            }
        }
        if (!completed) {
            return false
        } else {
            if (smart) {
                possibleMoves.sortByDescending { it.third.size }
                val distinctPossibleMoves = possibleMoves.distinctBy { it.first }
                return chooseMove(distinctPossibleMoves)
            } else {
                checkNotNull(tile)
                playerActionService.cultivate(tile, q, r)
            }
            return false
        }
    }

    private fun chooseMoveByTileType(tile: BonsaiTile, coord: Pair<Int, Int>,
                                     move: Triple<BonsaiTile, Pair<Int, Int>, List<Pair<Int, Int>>>): Boolean {
        var finished = false
        when (tile.type) {
            TileType.WOOD -> {
                try {
                    playerActionService.cultivate(tile, coord.first, coord.second)
                    finished = true
                } catch (_: IllegalStateException) {
                }
            }

            TileType.LEAF -> {
                val emptySpaces = move.third
                if (emptySpaces.size > 1) {
                    try {
                        playerActionService.cultivate(tile, coord.first, coord.second)
                        finished = true
                    } catch (_: IllegalStateException) {
                    }
                }
            }

            TileType.FLOWER -> {
                try {
                    playerActionService.cultivate(tile, coord.first, coord.second)
                    finished = true
                } catch (_: IllegalStateException) {
                }
            }

            TileType.FRUIT -> {
                try {
                    playerActionService.cultivate(tile, coord.first, coord.second)
                    finished = true
                } catch (ex: Exception) {
                    when (ex) {
                        is IllegalStateException, is IllegalArgumentException -> {}
                    }
                }
            }

            else -> {}
        }
        return finished
    }

    /**
     * SmartBot method to execute the found possibilities for cultivate
     * @param possibleMoves The possibilities for cultivate
     */
    private fun chooseMove(possibleMoves: List<Triple<BonsaiTile, Pair<Int, Int>, List<Pair<Int, Int>>>>): Boolean {
        var finished = false
        for (move in possibleMoves) {
            val tile = move.first
            val coord = move.second
            finished = chooseMoveByTileType(tile, coord, move)
            if (finished) break
        }
        return finished
    }

    /**
     * Handles chooseCards event for bot
     */
    fun chooseCardLogic(
        chooseTilesByBoard: Boolean,
        chooseTilesByCard: Boolean,
    ) {
        if (chooseTilesByBoard) {
            val selectedTileByBoardType = TileType.entries.subList(0, 2).random()
            rootService.playerActionService.applyTileChoice(selectedTileByBoardType, false)
        }
        if (chooseTilesByCard) {
            val selectedTileByCardType = TileType.entries.random()
            rootService.playerActionService.applyTileChoice(selectedTileByCardType, true)
        }
    }

    /**
     * Handles discardTile event for bot
     */
    fun discardTileLogic(tilesToDiscard: Int) {
        val game = rootService.currentGame
        checkNotNull(game)
        val player = game.currentState.players[game.currentState.currentPlayer]
        repeat(tilesToDiscard) {
            val removedTile = player.supply.random()
            playerActionService.discardTile(removedTile)
        }
        onAllRefreshables { refreshAfterDiscardTile(0, null) }
        playerActionService.endTurn()
    }

    /**
     * Handles removeTile event for bot
     */
    private fun removeTileLogic(
        supplyTile: BonsaiTile,
        player: Player,
    ) {
        if (supplyTile.type == TileType.WOOD) {
            try {
                val grid = player.bonsai.grid
                val currentPlayerBonsaiTiles = player.bonsai.tiles()
                val leastGroupOfTilesToBeRemoved =
                    TileUtils.leastGroupOfTilesToBeRemoved(currentPlayerBonsaiTiles, grid)
                if (leastGroupOfTilesToBeRemoved.isNotEmpty()) {
                    playerActionService.removeTile(leastGroupOfTilesToBeRemoved.random())
                }
            } catch (_: IllegalStateException) {
            }
        }
    }
}
