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

    private fun placeTile(tile: BonsaiTile, r: Int, q: Int) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        val bonsai = currentPlayer.bonsai
        val grid = bonsai.grid

        // Ensure the placement position is valid
        require(grid.isNotEmpty()) { "Cannot place a tile in an empty grid." }
        require(grid.isEmpty(q, r)) { "Cannot place a tile on top of another tile." }
        require(grid.isNotPot(q, r)) { "Cannot place a tile in the Pot Area." }

        // Get neighboring tiles
        val neighbors = grid.getNeighbors(q, r)

        // Check placement rules based on tile type
        when (tile.type) {
            TileType.WOOD -> {
                require(neighbors.any { it.type == TileType.WOOD }) {
                    "A wood tile must be placed adjacent to another wood tile."
                }
            }
            TileType.LEAF -> {
                require(neighbors.any { it.type == TileType.WOOD }) {
                    "A leaf tile must be placed adjacent to a wood tile."
                }
            }
            TileType.FLOWER -> {
                require(neighbors.any { it.type == TileType.LEAF }) {
                    "A flower tile must be placed adjacent to a leaf tile."
                }
            }
            TileType.FRUIT -> {
                val adjacentLeaves = neighbors.filter { it.type == TileType.LEAF }
                require(adjacentLeaves.size >= 2) {
                    "A fruit tile must be placed between two adjacent leaf tiles."
                }
                require(neighbors.none { it.type == TileType.FRUIT }) {
                    "A fruit tile cannot be placed adjacent to another fruit tile."
                }
            }
            else -> {
                throw IllegalStateException("Invalid tile type for placement.")
            }
        }

        // Remove tile from player's supply
        currentPlayer.supply.remove(tile)

        // Place tile in bonsai grid
        grid[q, r] = tile

        // Update tile count
        bonsai.tileCount[tile.type] = bonsai.tileCount.getOrDefault(tile.type, 0) + 1

        // Refresh the game scene
        onAllRefreshables { refreshAfterPlaceTile(tile) }

        // Check if any goal condition is met
       /* val metGoals = checkGoalsAfterPlacement(bonsai, grid)
        onAllRefreshables { refreshAfterReachGoals(metGoals) }*/
    }

    /*private fun checkGoalsAfterPlacement(bonsai: Bonsai, grid: HexGrid): List<GoalCard> {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val goalCards = game.currentState.goalCards

        val metGoals = mutableListOf<GoalCard>()
        for (goalCard in goalCards) {
            val isGoalMet = when (goalCard.color) {
                GoalColor.BROWN -> (bonsai.tileCount[TileType.WOOD] ?: 0) >= (when (goalCard.difficulty) {
                    GoalDifficulty.LOW -> 8
                    GoalDifficulty.INTERMEDIATE -> 10
                    GoalDifficulty.HARD -> 12
                })
                GoalColor.ORANGE -> (bonsai.tileCount[TileType.FRUIT] ?: 0) >= (when (goalCard.difficulty) {
                    GoalDifficulty.LOW -> 3
                    GoalDifficulty.INTERMEDIATE -> 4
                    GoalDifficulty.HARD -> 5
                })
                GoalColor.GREEN -> {
                    val maxAdjacentLeaves = bonsai.grid.tilesList().filter { it.type == TileType.LEAF }
                        .map { grid.getNeighbors(it).count { neighbor -> neighbor.type == TileType.LEAF } }
                        .maxOrNull() ?: 0
                    maxAdjacentLeaves >= (when (goalCard.difficulty) {
                        GoalDifficulty.LOW -> 5
                        GoalDifficulty.INTERMEDIATE -> 7
                        GoalDifficulty.HARD -> 9
                    })
                }
                GoalColor.RED -> {
                    val leftProtrusions = bonsai.grid.tilesList().count { grid.isProtruding(it.q, it.r) && it.q <= -2 }
                    val rightProtrusions = bonsai.grid.tilesList().count { grid.isProtruding(it.q, it.r) && it.q >= 3 }
                    maxOf(leftProtrusions, rightProtrusions) >= (when (goalCard.difficulty) {
                        GoalDifficulty.LOW -> 3
                        GoalDifficulty.INTERMEDIATE -> 4
                        GoalDifficulty.HARD -> 5
                    })
                }
                GoalColor.BLUE -> {
                    when (goalCard.difficulty) {
                        GoalDifficulty.LOW -> bonsai.grid.tilesList().any { grid.isProtruding(it.q, it.r) && it.q >=3  }
                        GoalDifficulty.INTERMEDIATE -> bonsai.grid.tilesList().any { grid.isProtruding(it.q, it.r) && it.q <= -2 } &&
                                bonsai.grid.tilesList().any { grid.isProtruding(it.q, it.r) && it.q >= 3 }
                        GoalDifficulty.HARD -> bonsai.grid.tilesList().any { grid.isProtruding(it.q, it.r) && it.q >= 3 } &&
                                bonsai.grid.tilesList().any { grid.isProtruding(it.q, it.r) && it.r >= 2 } ||
                                bonsai.grid.tilesList().any { grid.isProtruding(it.q, it.r) && it.q <= -2 } &&
                                bonsai.grid.tilesList().any { grid.isProtruding(it.q, it.r) && it.r >= 2 }
                    }
                }
            }
            if (isGoalMet) {
                metGoals.add(goalCard)
            }
        }
        return metGoals

    }*/





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




