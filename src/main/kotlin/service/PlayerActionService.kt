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
     * Places a tile while considering the player's helper cards and growth card effects.
     *
     * This method verifies whether the specified tile is in the player's personal supply.
     * If the player has an unused helper card, it checks whether the tile type matches
     * any of the helper card's allowed tiles or if a generic tile can be used.
     *
     * If a matching tile is found, the tile is placed, and the matched tile in the helper card is marked as used
     * by adding it to `usedHelperTiles`. The helper card itself is also marked as used by adding it to
     * `usedHelperCards`.
     *
     * If the tile does not match the conditions of the helper card, an exception is thrown.
     *
     * If the player does not use a HelperCard, the tile is placed normally according to the rules of [placeTile].
     * The tile must either match one of the player's Seishi starting tiles or be allowed by an active GrowthCard.
     *
     * @param tile The BonsaiTile to be placed.
     * @param r The row coordinate for tile placement.
     * @param q The column coordinate for tile placement.
     *
     * @throws IllegalStateException if there is no active game or if the tile is not in the player's supply.
     * @throws IllegalStateException if a helper card is available but the tile does not follow its placement rules of the card
     * @throws IllegalStateException if no helper card is used and the tile is not allowed based on Seishi
     * or GrowthCard rules.
     */
    fun cultivate(tile: BonsaiTile, r: Int, q: Int) {

        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        val personalSupply = currentPlayer.supply
        check(tile in personalSupply) { "this Tile is not in your personal supply" }

        // Find an unused helper card in the hidden deck
        val helperCard = currentPlayer.hiddenDeck.find {
            it is HelperCard && it !in currentPlayer.usedHelperCards
        } as? HelperCard

        // placing a Tile when having  a helper Card
        if (helperCard != null) {


            val respectedTile = helperCard.tiles.find { it == tile.type  && it !in currentPlayer.usedHelperTiles }
            val genericTile = helperCard.tiles.find {
                it == TileType.GENERIC && it !in currentPlayer.usedHelperTiles
            }

            if (respectedTile != null || genericTile != null) {
                placeTile(tile, r, q)
                currentPlayer.usedHelperCards.add(helperCard)

                // add the specific tile that was matched to usedHelperTiles
                if (respectedTile != null) {
                    currentPlayer.usedHelperTiles.add(respectedTile)
                } else if (genericTile != null) {
                    currentPlayer.usedHelperTiles.add(genericTile)

                    // wichtig: player´s usedHelperTiles must be cleared when the player ends his Turn
                }
            }
            else {

                throw IllegalStateException(" Unrespected placing rules according to Helper Card")
            }
        }
        // placing a Tile without a helper Card
        else {
            val seishiAllowedTiles = currentPlayer.treeTileLimit.keys // The 3 permanent Seishi tile types
            // Tile types granted by Growth Cards
            val growthAllowedTiles = currentPlayer.seishiGrowth.map { (it as? GrowthCard)?.type }

            val allowedTiles = seishiAllowedTiles + growthAllowedTiles
            val isPlacementAllowed = tile.type in allowedTiles

            if (isPlacementAllowed) {
                placeTile(tile, r, q)
            } else {
                throw IllegalStateException("Tile placement not allowed based on Seishi StartingTile ans Growth Cards.")
            }
        }

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
            is ParchmentCard -> currentPlayer.hiddenDeck += card // Store card within hiddenDeck
            is HelperCard -> {
                currentPlayer.hiddenDeck += card
                onAllRefreshables { refreshAfterDrawHelperCard(card) }
            }
        }

        val placeholderCard = object : ZenCard {}
        game.currentState.openCards[cardIndex] = placeholderCard
        // Shift remaining cards and enforce game constraints
        shiftBoardAndRefill(cardIndex)

    }

    private fun placeTile(tile: BonsaiTile, r: Int, q: Int){

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
     * Draws a card from the specified position in the openCards stack and processes it.
     * For certain positions (e.g., cardStack 1), it requires player input for tile choice,
     * deferring the addition of tiles to the supply until applyTileChoice is called.
     *
     * Preconditions:
     * - A game must be active (rootService.currentGame != null).
     * - The openCards list must not be empty.
     *
     * Postconditions:
     * - For cardStack 0, 2, 3, tiles are immediately added to the player's supply, and the UI is refreshed.
     * - For cardStack 1, the function triggers a GUI prompt via refreshToPromptTileChoice and exits early,
     *   leaving tile assignment to applyTileChoice.
     * - For invalid cardStack values, no tiles are added.
     *
     * @param cardStack The position in openCards to draw from (0-based index).
     */
    fun drawCard(cardStack: Int) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")

        if (game.currentState.openCards.isEmpty()) throw IllegalStateException("No available cards to draw")

        // Determine which tiles to assign based on the card's position in openCards
        val acquiredTiles = when (cardStack) {
            0 -> emptyList() // Position 0: No tiles awarded
            1 -> {
                // Position 1: Player must choose between WOOD or LEAF
                // Notify the GUI (via Refreshable objects) to prompt the player for a tile choice
                onAllRefreshables { refreshToPromptTileChoice() }
                // Exit early; tile assignment is deferred until applyTileChoice is called by the GUI
                return
            }
            2 -> listOf(BonsaiTile(TileType.WOOD), BonsaiTile(TileType.FLOWER))
            3 -> listOf(BonsaiTile(TileType.LEAF), BonsaiTile(TileType.FRUIT))
            else -> emptyList() // Any other position: No tiles (default case)
        }

        // If we reach here (i.e., not cardStack 1), add the tiles to the current player's supply
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        currentPlayer.supply += acquiredTiles

        // Notify the GUI to update (e.g., refresh supply display, board, etc.) with the drawn card
        onAllRefreshables { refreshAfterDrawCard(game.currentState.openCards[cardStack]) }
    }

    /**
     * Applies the player's tile choice for cardStack 1 after the GUI prompts them.
     * This is called by the GUI after the player selects WOOD or LEAF.
     *
     * Preconditions:
     * - A game must be active.
     * - The choice must be either WOOD or LEAF.
     *
     * Postconditions:
     * - The chosen tile is added to the player's supply.
     * - The UI is refreshed to reflect the change.
     *
     * @param cardStack The position in openCards (should be 1 for this case).
     * @param choice The tile type chosen by the player (WOOD or LEAF).
     */

    fun applyTileChoice(cardStack: Int, choice: TileType) {
        val game = rootService.currentGame ?: return

        // Validate that the choice is WOOD or LEAF; throw an exception if invalid
        require(choice == TileType.WOOD || choice == TileType.LEAF) { "Invalid choice" }

        // Add the chosen tile to the current player's supply
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        currentPlayer.supply += listOf(BonsaiTile(choice))

        // Notify the GUI to update with the drawn card (same as in drawCard)
        onAllRefreshables { refreshAfterDrawCard(game.currentState.openCards[cardStack]) }
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






