package service
import entity.*
import gui.*
import helper.*

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
        // Method implementation
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
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }

        game.redoStack.push(game.currentState.copy())
        game.currentState = game.undoStack.pop()
        onAllRefreshables { refreshAfterUndoRedo() }
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
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }

        game.undoStack.push(game.currentState.copy())
        game.currentState = game.redoStack.pop()
        onAllRefreshables { refreshAfterUndoRedo() }
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
    fun meditate(card: ZenCard) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]

        if (game.currentState.openCards.isEmpty()) throw IllegalStateException("No available cards to draw")

        // Find the card in openCards and ensure it's valid
        val cardIndex = game.currentState.openCards.indexOf(card)
        if (cardIndex == -1) throw IllegalStateException("The selected card is not in openCards")

        // Draw the card and mark its position as taken
        drawCard(cardIndex)

        // Process card effects based on type
        when (card) {
            is GrowthCard -> currentPlayer.seishiGrowth.push(card)
            is ToolCard -> {
                currentPlayer.seishiTool.push(card)
                currentPlayer.supplyTileLimit += 2 // Increase tile limit for future turns
            }
            is MasterCard -> {
                currentPlayer.supply += card.tiles.map { BonsaiTile(it) } // Add tiles to supply
                // Store the card in the hidden deck; tile limit checks will be enforced at the end of the turn
                currentPlayer.hiddenDeck += card
            }
            is ParchmentCard, is HelperCard ->
                currentPlayer.hiddenDeck += card // Store card within hiddenDeck

            is PlaceholderCard -> {}
        }

        game.currentState.openCards[cardIndex] = PlaceholderCard
        // Shift remaining cards and enforce game constraints
        shiftBoardAndRefill(cardIndex)

    }

    fun placeTile(tile: TileType, r: Int, q: Int){

    }

    /**
     * Decides whether to claim or renounce a goal card.
     *
     * @param goalCard The goal card being considered.
     * @param claim If true, the player claims the goal. If false, the player renounces it.
     *
     * If the player claims the goal, the card is added to the player's accepted goals list,
     * and all other goal cards of the same color  are forbidden.
     * If the player renounces the goal, only this specific goal card is forbidden.
     */
    fun decideGoalClaim(goalCard: GoalCard,claim: Boolean){
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        if (claim) {
            // Add the claimed goal to acceptedGoals
            (currentPlayer.acceptedGoals).add(goalCard)
            onAllRefreshables { refreshAfterClaimGoal(goalCard)  }

            // Find all goal cards of the same color and forbid them
            currentPlayer.forbiddenGoals.addAll(
                currentPlayer.declinedGoals.filter { it.color == goalCard.color && it !in currentPlayer.forbiddenGoals }
            )
        } else {
            // Only forbid this specific goal card
            if (goalCard !in currentPlayer.forbiddenGoals) {
                currentPlayer.forbiddenGoals.add(goalCard)
            }
        }


    }
    /**
     * Draws a card from the draw stack and processes it according to its type.
     *
     * **Preconditions:**
     * - A game must be active.
     * - The `openCards` list must not be empty.
     *
     * **Postconditions:**
     * - The selected card is drawn and its effects are applied.
     * - If the card position grants bonsai tiles, they are added to the player's supply.
     * - The UI is updated to reflect the drawn card.
     *
     * @param cardStack The position in the stack to draw from.
     */

    fun drawCard(cardStack: Int) {

        val game = rootService.currentGame ?: throw IllegalStateException("No active game")

        if (game.currentState.openCards.isEmpty()) throw IllegalStateException("No available cards to draw")

        // Assign bonsai tiles based on the drawn card's board position
        val acquiredTiles = when (cardStack) {
            0 -> emptyList()
            1 -> {
                // Allow the player to choose between WOOD or LEAF
                val choice = onAllRefreshables { refreshTogetUserTileChoice() } as TileType
                require(choice == TileType.WOOD  || choice == TileType.LEAF  ){throw IllegalStateException()}
                listOf(BonsaiTile(choice))
            }
            2 -> listOf(BonsaiTile(TileType.WOOD), BonsaiTile(TileType.FLOWER))
            3 -> listOf(BonsaiTile(TileType.LEAF), BonsaiTile(TileType.FRUIT))
            else -> emptyList()
        }

        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        currentPlayer.supply += acquiredTiles
        onAllRefreshables { refreshAfterDrawCard(game.currentState.openCards[cardStack])}
    }

    /**
     * Handles the placement of tiles when a HelperCard is drawn.
     *
     * This function will be directly triggered in the GUI when the player selects a HelperCard.
     * It ensures that at least one tile is placed according to the HelperCard's rules.
     *
     * @param card The HelperCard being processed.
     * @param tile The tile selected by the player.
     * @param r The row coordinate where the tile should be placed.
     * @param q The column coordinate where the tile should be placed.
     */
    fun placeHelperCardTiles(card: HelperCard, tile: TileType, r: Int ,q: Int ) {

        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]

        // Check if the chosen tile is different from the one shown on the HelperCard
        // and ensure that the player has not already placed a chosen tile

        if (tile != card.tiles[1] && !card.hasPlacedChosenTile )
        {   placeTile(tile, r,q)  // Place the chosen tile on the board
            card.hasPlacedChosenTile = true // Mark the chosen tile as placed

        }
        // Check if the selected tile matches the shown tile on the HelperCard
        // and ensure that the player has this tile in their supply and has not placed it yet

        else if (tile == card.tiles[1] && currentPlayer.supply.contains(BonsaiTile(tile)) && !card.hasPlacedShownTile)
        { placeTile(tile, r,q)
            card.hasPlacedShownTile = true
        }

    }

    /**
     * Shifts all face-up cards to the right and fills the empty position with a new card.
     */
    fun shiftBoardAndRefill(cardStack: Int) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")

        if (game.currentState.drawStack.isEmpty()) return

        for (i in cardStack  downTo 1) {
            game.currentState.openCards[i] = game.currentState.openCards[i - 1]
        }

        val newCard = game.currentState.drawStack.pop()
        game.currentState.openCards[0] = newCard
    }

    fun removeTile(tile: BonsaiTile) {

        //check if game is running
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }

        //is tile in current player bonsai
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        val currentPlayerBonsaiTiles = currentPlayer.bonsai.tiles()
        check(currentPlayerBonsaiTiles.contains(tile)) { "cant remove a tile not in players bonsai" }

        val grid = currentPlayer.bonsai.grid
        val neighbors = grid.getNeighbors(tile)

        //is it possible to play wood tile
        check(!currentPlayerBonsaiTiles.any { bonsaiTile -> bonsaiTile.type == TileType.WOOD
                && neighbors.size < 6
        }) { "player can play wood" }

        //is it part of the least number of tiles to be removed to make placing a wood possible
        check(leastGroupOfTilesToBeRemoved(currentPlayerBonsaiTiles).contains(tile))
        {"tile not part of the least number of tiles to be removed to make placing a wood possible"}

        //remove tile from bonsai tree
        currentPlayer.bonsai.grid.remove(tile)

        //add tile to player supply
        currentPlayer.supply.add(tile)
    }

    private fun leastGroupOfTilesToBeRemoved(tiles: List<BonsaiTile>): List<BonsaiTile> {
        //check if game is running
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }

        return tiles.filter { tile ->
            //get grid
            val grid = game.currentState.players[game.currentState.currentPlayer].bonsai.grid

            //get neighbors of tile
            val neighbors = grid.getNeighbors(tile)

            //tile is not neighbor to wood
            if (neighbors.any { neighbor -> neighbor.type == TileType.WOOD }) return@filter false
            //tile is wood
            if (tile.type.equals(TileType.WOOD)) return@filter false
            //tile is surrounded
            if (neighbors.size == 6) return@filter false
            //tile is fruit or flower
            if (tile.type.equals(TileType.FLOWER) || tile.type.equals(TileType.FRUIT)) return@filter true

            //tile is leaf
            if (tile.type == TileType.LEAF) {
                val neighborFruits = neighbors.filter { neighbor -> neighbor.type == TileType.FRUIT }
                val neighborFlowers = neighbors.filter { neighbor -> neighbor.type == TileType.FLOWER }

                //has no fruit or flower neighbors
                if (neighborFlowers.isEmpty() && neighborFruits.isEmpty()) return@filter true

                //neighbor flower have less then 2 leaves
                else if (
                    neighborFlowers.any { flower ->
                        (grid.getNeighbors(flower).filter { neighbor -> neighbor.type == TileType.LEAF }.size) < 2
                    }
                ) { return@filter false }


                //neighbor fruit has no 2 adjacent leafs after deletion
                for (fruit in neighborFruits) {
                    val fruitLeafNeighbors = grid.getNeighbors(fruit)
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
        //check if game is running
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }

        val grid = game.currentState.players[game.currentState.currentPlayer].bonsai.grid

        for (leaf in leafTiles) {
            if (grid.getNeighbors(leaf).any { it in leafTiles }) {
                return true
            }
        }
        return false
    }
}




