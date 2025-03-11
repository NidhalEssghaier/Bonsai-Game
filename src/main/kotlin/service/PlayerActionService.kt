package service

import entity.BonsaiTile
import entity.TileType
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView

class PlayerActionService(private val rootService: RootService):AbstractRefreshingService() {
    /**
     * Ends the active players turn and advances the game to the next player.
     *
     * This method does not accept parameters.
     *
     * Preconditions:
     * - A game was started and is running.
     * - The active player must have started an action (either `meditate` or `cultivate`).
     *
     * Postconditions:
     * - If the player has more tiles than his storage limit allows,
     *   he has to choose tiles to discard, until he doesn't.
     * - The game saves the current state to the `undoStack` and clears the `redoStack`.
     * - If all cards are revealed, increase the `endGameCounter`.
     * - If the `endGameCounter` is larger than the player count, call `endGame`,
     *   otherwise advance to the next player.
     *
     * @returns This method does not return anything (`Unit`).
     *
     * @throws IllegalStateException If there is no started and running game,
     *                               or if the active player has not started an action.
     *
     * @sample endTurn()
     */
    fun endTurn() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active." }

        check(
            rootService.networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT
        ) { "The active player has not started an action." }

        while (
            game.players[game.currentPlayer].supply.size > game.players[game.currentPlayer].supplyTileLimit
        ) {
            onAllRefreshables {
                refreshAfterDiscardTile()
            }
        }

        game.undoStack.push(game)
        game.redoStack.clear()

        if (game.drawStack.isEmpty()) {
            game.endGameCounter++
            if (game.endGameCounter > game.players.size) {
                rootService.gameService.endGame()
            } else {
                game.currentPlayer = (game.currentPlayer + 1) % game.players.size
                onAllRefreshables {
                    refreshAfterEndTurn()
                }
            }
        } else {
            game.currentPlayer = (game.currentPlayer + 1) % game.players.size
            onAllRefreshables {
                refreshAfterEndTurn()
            }
        }
    }

    /**
     * Removes the given tile from the active players supply.
     *
     * @param tile The tile to remove from the active players supply.
     *
     * Preconditions:
     * - A game was started and is running.
     * - The active player must have the given tile in their supply.
     *
     * Postconditions:
     * - The given tile is removed from the active players supply.
     *
     * @returns This method does not return anything (`Unit`).
     *
     * @throws IllegalStateException If there is no started and running game, or if the active
     *                               player does not have the given tile in their supply.
     *
     * @sample discardTile(BonsaiTile(TileType.WOOD))
     */
    fun discardTile(tile: BonsaiTile) {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active." }

        require(
            tile in game.players[game.currentPlayer].supply
        ) { "The given tile is not in the active players supply."}

        game.players[game.currentPlayer].supply.remove(tile)
    }

    /**
     * Undoes the last action by popping and restoring the last state from the [undoStack]
     * and pushing it onto the [redoStack].
     *
     * This method does not accept parameters.
     *
     * **Preconditions:**
     * - A game has been started and is currently running.
     * - The [undoStack] is not empty.
     *
     * **Postconditions:**
     * - The previous game state has been moved from the [undoStack] to the [redoStack].
     * - The previous game state has been restored.
     *
     * @throws IllegalStateException If there is no active game, or no action to undo.
     */
    fun undo() {
        // Method implementation
    }

    /**
     * Redoes the last undone action by popping and restoring the last state from the [redoStack]
     * and pushing it onto the [undoStack].
     *
     * **Preconditions:**
     * - A game has been started and is currently running.
     * - The [redoStack] is not empty.
     *
     * **Postconditions:**
     * - The following game state has been moved from the [redoStack] to the [undoStack].
     * - The following game state has been restored.
     *
     * @throws IllegalStateException If there is no active game, or no action to redo.
     */
    fun redo() {
        // Method implementation
    }

    /**
     * Executes the "cultivate" action, allowing the player to place tiles in Bonsai.
     * In the rare case that placing a wood tile is not possible at the beginning of the player's turn,
     * the player may remove existing tiles to enable placement.
     *
     * Preconditions:
     * - A game must have been started and must be currently running.
     * - The player must be on their turn.
     *
     * Postconditions:
     * - If placing a tile is possible , and the player chooses to, it will be placed.
     * - If the player chooses to remove tiles, they will be removed.
     * - The player's turn will end.
     *
     * @returns This method returns no value (`Unit`).
     *
     * @throws IllegalStateException If there is no active game.
     *
     * @sample cultivate()
     */
    fun cultivate() {
        // Method implementation
    }

    /**
     * Executes the "meditate" action, allowing the player to remove tiles and draw a card from the [BonsaiGame] [openCards].
     * Based on the card position, a [Player] receives bonsai tiles from the common supply and keep
     * them in his [supply].
     * Based on the card type, a player can play a tile, recieve tiles or get a bonus
     *
     * Preconditions:
     * - A [BonsaiGame] must have been started and must be currently running.
     * - [Player] must be on their turn.
     * - the Board must have at least one card
     *
     * Postconditions:
     * - [HelperCard],[MasterCard] and [ParchmentCard] will be added to [Player] [hiddenDeck].
     * - [ToolCard] will be added to [Player] [seichiTool].
     * - [GrowthCard] will be added to [Player] [seichiGrowth].
     * - The player's turn will end.
     *
     * @returns This method returns no value (`Unit`).
     *
     * @throws IllegalStateException If there is no active game.
     * @throws IllegalStateException If the Game's stacks are empty.
     *
     */
    fun medidate() {
        // Method implementation
    }

    fun removeTile(tile: BonsaiTile) {

        //check gamme is running
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }

        //is tile in current player bonsai
        val currentPlayer = game.players[game.currentPlayer]
        val currentPlayerBonsaiTiles = currentPlayer.bonsai.tiles
        check(currentPlayerBonsaiTiles.contains(tile)) { "cant remove a tile not in players bonsai" }

        //is it possible to play wood tile
        check(!currentPlayerBonsaiTiles.any { bonsaiTile -> bonsaiTile.type == TileType.WOOD
                && bonsaiTile.neighbors.size < 6
        }) { "player can play wood" }

        //is it part of the least number of tiles to be removed to make placing a wood possible
        check(leastGroupOfTilesToBeRemoved(currentPlayerBonsaiTiles).contains(tile))
        {"tile not part of the least number of tiles to be removed to make placing a wood possible"}

        //remove tile from bonsai tree
        currentPlayer.bonsai.tiles.remove(tile)
        val keyToRemove:HexagonGrid<HexagonView>? = currentPlayer.bonsai.grid.entries.find { it.value.equals(tile) }?.key
        if (keyToRemove != null) {
            currentPlayer.bonsai.grid.remove(keyToRemove)
        }

        //add tile to player supply
        currentPlayer.supply.add(tile)
    }

    private fun leastGroupOfTilesToBeRemoved(tiles: List<BonsaiTile>): List<BonsaiTile> {
        return tiles.filter { tile ->
            //tile is not neighbor to wood
            if (tile.neighbors.any { neighbor -> neighbor.type == TileType.WOOD }) return@filter false
            //tile is wood
            if (tile.type.equals(TileType.WOOD)) return@filter false
            //tile is surrounded
            if (tile.neighbors.size == 6) return@filter false
            //tile is fruit or flower
            if (tile.type.equals(TileType.FLOWER) || tile.type.equals(TileType.FRUIT)) return@filter true

            //tile is leaf
            if (tile.type == TileType.LEAF) {
                val neighborFruits = tile.neighbors.filter { neighbor -> neighbor.type == TileType.FRUIT }
                val neighborFlowers = tile.neighbors.filter { neighbor -> neighbor.type == TileType.FLOWER }

                //has no fruit or flower neighbors
                if (neighborFlowers.isEmpty() && neighborFruits.isEmpty()) return@filter true

                //neighbor flower have less then 2 leaves
                else if (
                    neighborFlowers.any { flower ->
                        (flower.neighbors.filter { neighbor -> neighbor.type == TileType.LEAF }.size) < 2
                    }
                ) { return@filter false }


                //neighbor fruit has no 2 adjacent leafs after deletion
                for (fruit in neighborFruits) {
                    val fruitLeafNeighbors = fruit.neighbors
                        .filter { neighbor -> neighbor.type == TileType.LEAF && !neighbor.equals(tile) }
                    if(!hasAdjacentPair(fruitLeafNeighbors)){
                        return@filter false
                    }
                }
                return@filter true
            }
            return@filter false
        }
    }

    private fun hasAdjacentPair(leafTiles: List<BonsaiTile>): Boolean {
        for (leaf in leafTiles) {
            if (leaf.neighbors.any { it in leafTiles }) {
                return true
            }
        }
        return false
    }
}




