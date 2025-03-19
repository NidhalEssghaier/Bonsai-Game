package service
import entity.*
import gui.*
import helper.*

class PlayerActionService(
    private val rootService: RootService,
) : AbstractRefreshingService() {


    private var allowedTiles:MutableMap<TileType,Int> = mutableMapOf(
        TileType.GENERIC to 1,
        TileType.WOOD to 1,
        TileType.LEAF to 1
    )
    /**
     * The companion object contains the [switchPlayer] method, which is used to switch the active player
     */
    companion object {
        /**
         * Switches the active player to the next player in the game.
         * This method is called in endTurn() and BonsaiGameSerializer.
         * @param game the [BonsaiGame] object representing the game
         *
         * Preconditions:
         * - A game was started and is running.
         *
         * Postconditions:
         * - The current player is switched to the next player in the game.
         * - The used helper tiles of the current player are cleared.
         */
        fun switchPlayer(game: BonsaiGame) {
            game.currentState.currentPlayer =
                (game.currentState.currentPlayer + 1) % game.currentState.players.size
            // Reset hasDrawnCard for the new player
            game.currentState.players[game.currentState.currentPlayer].hasDrawnCard = false
        }
    }

    /**
     * Ends the active players turn and advances the game to the next player.
     *
     * This method does not accept parameters.
     *
     * Preconditions:
     * - A game was started and is running.
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

        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        currentPlayer.hasCultivated = false

        // discard tiles first if necessary
        val tilesToDiscard =
            currentPlayer.supply.size - currentPlayer.supplyTileLimit
        if (tilesToDiscard > 0) {
            onAllRefreshables {
                refreshAfterDiscardTile(tilesToDiscard, null)
            }
            return // break to allow discarding tiles
        }

        // fix Bug : insure helper cant be used in later rounds
        // Ensure that any (fully)unused HelperCard is moved to usedHelperCards
        val lastHelperCard = currentPlayer.hiddenDeck.lastOrNull { it is HelperCard } as? HelperCard
        if (lastHelperCard != null ) {
            currentPlayer.usedHelperCards.add(lastHelperCard)
        }

        // clear used helper card tiles of the current player
        currentPlayer.usedHelperTiles.clear()

        //return tree limit to state beofre playing
        allowedTiles = currentPlayer.treeTileLimit.toMutableMap()
        //prepare allowed tiles for next player
        game.undoStack.push(game.currentState.copy())
        game.redoStack.clear()

        if (game.currentState.drawStack.isEmpty()) {
            game.currentState.endGameCounter++
        }
        if (game.currentState.endGameCounter > game.currentState.players.size) {
            rootService.gameService.endGame()
        } else {
            switchPlayer(game)
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
            tile in game.currentState.players[game.currentState.currentPlayer].supply,
        ) { "The given tile is not in the active players supply." }

        val tilesToDiscard =
            game.currentState.players[game.currentState.currentPlayer]
                .supply.size -
                game.currentState.players[game.currentState.currentPlayer].supplyTileLimit

        require(
            tilesToDiscard > 0
        ) { "The current supply size is equal to or lower than the supply tile limit." }

        game.currentState.players[game.currentState.currentPlayer]
            .supply
            .remove(tile)

        onAllRefreshables { refreshAfterDiscardTile(tilesToDiscard - 1, tile) }
    }

    /**
     * Undoes the last action by popping and restoring the last state from the [BonsaiGame.undoStack]
     * and pushing it onto the [BonsaiGame.redoStack].
     *
     * This method does not accept parameters.
     *
     * **Preconditions:**
     * - A game has been started and is currently running.
     * - The [BonsaiGame.undoStack] is not empty.
     *
     * **Postconditions:**
     * - The previous game state has been moved from the undoStack to the redoStack.
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
     * Redoes the last undone action by popping and restoring the last state from the [BonsaiGame.redoStack]
     * and pushing it onto the [BonsaiGame.undoStack].
     *
     * **Preconditions:**
     * - A game has been started and is currently running.
     * - The redoStack is not empty.
     *
     * **Postconditions:**
     * - The following game state has been moved from the redoStack to the undoStack.
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
    fun cultivate(
        tile: BonsaiTile,
        q: Int,
        r: Int,
    ) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        val personalSupply = currentPlayer.supply
        check(tile in personalSupply) { "this Tile is not in your personal supply" }

        // Find an unused helper card in the hidden deck
        val helperCard =
            currentPlayer.hiddenDeck.find {
                it is HelperCard && it !in currentPlayer.usedHelperCards
            } as? HelperCard

        // placing a Tile when having  a helper Card
        if (helperCard != null) {
            val matchingTile  = helperCard.tiles.find { it == tile.type && it !in currentPlayer.usedHelperTiles }
            val genericTile =
                helperCard.tiles.find {
                    it == TileType.GENERIC && it !in currentPlayer.usedHelperTiles
                }

            if (matchingTile != null || genericTile != null) {
                placeTile(tile, q, r)

                // Mark the used tile type
                if (matchingTile != null) {
                    currentPlayer.usedHelperTiles.add(matchingTile)
                } else if (genericTile != null) {
                    currentPlayer.usedHelperTiles.add(genericTile)
                }
                // fix bug : allowing to place a "generic" tile after the "specific" one.
                // Remove the helper card only if both tiles were used
                // but if the player clicks on endTurn button this   helper Card will be added to usedHelperCard and
                //cant be used in later rounds . see [endTurn]
                if (currentPlayer.usedHelperTiles.containsAll(helperCard.tiles)) {
                    currentPlayer.usedHelperCards.add(helperCard)
                }
            } else {
                throw IllegalStateException(" violated placing rules according to Helper Card")
            }
        }
        // placing a Tile without a helper Card
        else {


            // Allow placement if Generic ;imit bigger or tile type limit bigger then 0
            val isPlacementAllowed = allowedTiles[TileType.GENERIC]!! > 0
                    || (allowedTiles.contains(tile.type) && allowedTiles[tile.type]!! > 0)

            if (isPlacementAllowed) {
                placeTile(tile, q, r)
            } else {
                throw IllegalStateException("Tile placement not allowed based on Seishi StartingTile ans Growth Cards.")
            }
        }
        onAllRefreshables { refreshAfterPlaceTile(tile) }
    }


    /**
     * Places a tile on the bonsai grid, ensuring placement rules are followed.
     *
     * @param tile The BonsaiTile to be placed.
     * @param r The row coordinate for placement.
     * @param q The column coordinate for placement.
     *
     * @throws IllegalStateException If placement rules are violated.
     */
    private fun placeTile(
        tile: BonsaiTile,
        q: Int,
        r: Int,
    ) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]

        // Bug fix ensure player cant cultivate after drawing a card
        val helperCard =
            currentPlayer.hiddenDeck.find {
                it is HelperCard && it !in currentPlayer.usedHelperCards
            } as? HelperCard
        check(!currentPlayer.hasDrawnCard || (helperCard!=null && helperCard !in currentPlayer.usedHelperCards)){
            "you cant cultivate after meditating or you have drawn a helper Card and have fully used it "
        }

        // Mark that the player has placed a Tile
        currentPlayer.hasCultivated= true

        val bonsai = currentPlayer.bonsai
        val grid = bonsai.grid

        // Ensure the placement position is valid
        require(grid.isNotPot(q, r)) { "Cannot place a tile in the Pot Area." }
        require(grid.isEmpty(q, r)) { "Cannot place a tile on top of another tile." }


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
                // Ensure those two leaf tiles are adjacent to each other using hasAdjacentPair
                require(TileUtils.hasAdjacentPair(adjacentLeaves,grid)) {
                    "The two leaf tiles must also be adjacent to each other."
                }
                // Ensure a fruit tile is not placed adjacent to another fruit tile
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


        //update tree tile limit
        if(!allowedTiles.containsKey(tile.type)){
            allowedTiles[TileType.GENERIC] = allowedTiles[TileType.GENERIC]!!-1
        }
        else if(allowedTiles[tile.type]!! >0){
            allowedTiles[tile.type] = allowedTiles[tile.type]!! - 1
        }else{
            allowedTiles[TileType.GENERIC] = allowedTiles[TileType.GENERIC]!!-1

        }

        // Refresh the game scene
        onAllRefreshables { refreshAfterPlaceTile(tile) }

        // Check if any goal condition is met
        val metGoals = checkGoalsAfterPlacement(bonsai, grid)
        if (metGoals.isNotEmpty()) onAllRefreshables { refreshAfterReachGoals(metGoals) }
    }

    /**
     * Checks if any goal conditions are met after placing a tile.
     *
     * @param bonsai The player's bonsai tree.
     * @param grid The hexagonal grid representing the bonsai.
     * @return A list of GoalCards that have been achieved.
     */
    private fun checkGoalsAfterPlacement(
        bonsai: Bonsai,
        grid: HexGrid,
    ): List<GoalCard> {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val goalCards = game.currentState.goalCards
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]

        val metGoals = mutableListOf<GoalCard>()
        val tiles = bonsai.grid.tilesList()
        val protrudingTiles = tiles.filter { grid.isProtruding(it) }

        for (goalCard in goalCards) {
            if (goalCard == null) continue

             //fix Bug
            // prevent checking goals that the player has manually declined or were automatically forbidden due to claiming another
            if (goalCard in currentPlayer.declinedGoals || goalCard in currentPlayer.forbiddenGoals) continue

            val isGoalMet = when (goalCard.color) {
                GoalColor.BROWN ->
                    (bonsai.tileCount[TileType.WOOD] ?: 0) >= when (goalCard.difficulty) {
                        GoalDifficulty.LOW -> 8
                        GoalDifficulty.INTERMEDIATE -> 10
                        GoalDifficulty.HARD -> 12
                    }

                GoalColor.ORANGE ->
                    (bonsai.tileCount[TileType.FRUIT] ?: 0) >= when (goalCard.difficulty) {
                        GoalDifficulty.LOW -> 3
                        GoalDifficulty.INTERMEDIATE -> 4
                        GoalDifficulty.HARD -> 5
                    }

                GoalColor.GREEN -> {
                    val maxLeafGroup = findLargestLeafGroup(grid, tiles)
                    maxLeafGroup >= when (goalCard.difficulty) {
                        GoalDifficulty.LOW -> 5
                        GoalDifficulty.INTERMEDIATE -> 7
                        GoalDifficulty.HARD -> 9
                    }
                }

                GoalColor.RED -> {
                    val leftProtrusions = protrudingTiles.count {
                        it.type == TileType.FLOWER && grid.getPotSide(it) == PotSide.LEFT
                    }
                    val rightProtrusions = protrudingTiles.count {
                        it.type == TileType.FLOWER && grid.getPotSide(it) == PotSide.RIGHT
                    }
                    maxOf(leftProtrusions, rightProtrusions) >= when (goalCard.difficulty) {
                        GoalDifficulty.LOW -> 3
                        GoalDifficulty.INTERMEDIATE -> 4
                        GoalDifficulty.HARD -> 5
                    }
                }

                GoalColor.BLUE -> when (goalCard.difficulty) {
                    GoalDifficulty.LOW -> protrudingTiles.any { grid.getPotSide(it) == PotSide.RIGHT }
                    GoalDifficulty.INTERMEDIATE -> protrudingTiles.any { grid.getPotSide(it) == PotSide.LEFT } &&
                            protrudingTiles.any { grid.getPotSide(it) == PotSide.RIGHT }
                    GoalDifficulty.HARD -> protrudingTiles.any { grid.getPotSide(it) == PotSide.BELOW } &&
                            (protrudingTiles.any { grid.getPotSide(it) == PotSide.LEFT } ||
                                    protrudingTiles.any { grid.getPotSide(it) == PotSide.RIGHT })
                }
            }

            if (isGoalMet) {
                metGoals.add(goalCard)
            }
        }

        return metGoals
    }


    /**
     * Finds the largest connected cluster of leaf tiles in the bonsai grid.
     *
     *  This function uses Depth-First Search (DFS) algorithm  to traverse all connected leaf tiles,
     *  determining the largest contiguous group of leafs. It is used to evaluate whether the
     *  GREEN goal cards' conditions are met.
     *
     * @param grid The hexagonal grid representing the bonsai.
     * @param tiles The list of all tiles in the bonsai.
     * @return The size of the largest connected group of leaf tiles.
     */
    private fun findLargestLeafGroup(
        grid: HexGrid,
        tiles: List<BonsaiTile>,
    ): Int {
        val visited = mutableSetOf<BonsaiTile>()
        var maxClusterSize = 0

        fun dfs(tile: BonsaiTile): Int {
            if (tile in visited || tile.type != TileType.LEAF) return 0
            visited.add(tile)
            return 1 + grid.getNeighbors(tile).sumOf { dfs(it) }
        }

        for (tile in tiles.filter { it.type == TileType.LEAF }) {
            if (tile !in visited) {
                maxClusterSize = maxOf(maxClusterSize, dfs(tile))
            }
        }
        return maxClusterSize
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
    fun decideGoalClaim(
        goalCard: GoalCard,
        claim: Boolean,
    ) {
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        if (claim) {
            // Add the claimed goal to acceptedGoals
            (currentPlayer.acceptedGoals).add(goalCard)

            // remove goal from game
            game.currentState.goalCards[game.currentState.goalCards.indexOf(goalCard)] = null

            // Find all goal cards of the same color and forbid them
            currentPlayer.forbiddenGoals.addAll(
                game.currentState.goalCards.filter {
                    it?.color == goalCard.color && it !in currentPlayer.forbiddenGoals }.filterNotNull()
            )
        } else {
            // Only forbid this specific goal card
            if (goalCard !in currentPlayer.declinedGoals) {
                currentPlayer.declinedGoals.add(goalCard)
            }
        }
        onAllRefreshables { refreshAfterDecideGoal() }
    }

    /**
     * Executes the "meditate" action, allowing the player to remove tiles and draw a card from the BonsaiGame openCards.
     * Based on the card position, a [Player] receives bonsai tiles from the common supply and keep
     * them in his [Player.supply].
     * Based on the card type, a player can play a tile, recieve tiles or get a bonus
     *
     * Preconditions:
     * - A [BonsaiGame] must have been started and must be currently running.
     * - [Player] must be on their turn.
     * - the Board must have at least one card
     *
     * Postconditions:
     * - [HelperCard], [MasterCard] and [ParchmentCard] will be added to Player hiddenDeck]
     * - [ToolCard] will be added to [Player.seishiTool].
     * - [GrowthCard] will be added to [Player.seishiGrowth].
     * - The player's turn will end.
     *
     * @returns This method returns no value (`Unit`).
     *
     * @throws IllegalStateException If there is no active game.
     * @throws IllegalStateException If the Game's stacks are empty.
     * @throws IllegalArgumentException If the selected [card] is not in openCards.
     *
     */
    fun meditate(card: ZenCard) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]

        // Bug fix ensure player cant meditate after cultivating
        check(!currentPlayer.hasCultivated){"cant meditate after cultivate"}

        // Ensure the player has not already drawn a card this turn
        if (currentPlayer.hasDrawnCard) {
            throw IllegalStateException("The player has already drawn a card this turn")
        }
        // Mark the player as having drawn a card
        currentPlayer.hasDrawnCard = true

        // Ensure there are available cards in openCards
        if (game.currentState.openCards.isEmpty()) throw IllegalStateException("No available cards to draw")

        // Validate that the selected card exists in openCards
        require(card in game.currentState.openCards) { "The selected card is not in openCards" }

        // Find the selected card's position in openCards
        val cardIndex = game.currentState.openCards.indexOf(card)
        if (cardIndex == -1) throw IllegalStateException("The selected card is not in openCards")

        // Draw the card and mark its position as taken
        val receivedTiles = drawCard(cardIndex)
        var chooseTilesByBoard = false
        var chooseTilesByCard = false
        if (cardIndex == 1) {
            // choose tile later
            chooseTilesByBoard = true
        } else {
            currentPlayer.supply += receivedTiles
        }

        // Process the card's effect based on its type
        when (card) {
            is GrowthCard -> {

                //add to tree tile limit based on tile type on growth card
                if(currentPlayer.treeTileLimit.containsKey(card.type)) {
                    currentPlayer.treeTileLimit[card.type] = currentPlayer.treeTileLimit[card.type]!! + 1
                }
                else{
                    currentPlayer.treeTileLimit[card.type] = 1
                }
                currentPlayer.seishiGrowth.push(card)
            }
            is ToolCard -> {
                // Tool cards are added to the player's tool stack, increasing their tile limit
                currentPlayer.seishiTool.push(card)
                currentPlayer.supplyTileLimit += 2
            }
            is MasterCard -> {
                // can choose tile later if master card contains generic tile type
                if (card.tiles.contains(TileType.GENERIC)) {
                    chooseTilesByCard = true
                } else {
                    currentPlayer.supply += card.tiles.map { BonsaiTile(it) } // Add tiles to supply
                }
                // Store the card in the hidden deck; tile limit checks will be enforced at the end of the turn
                currentPlayer.hiddenDeck += card
            }
            is ParchmentCard -> currentPlayer.hiddenDeck += card // Store card within hiddenDeck
            is HelperCard -> {
                currentPlayer.hiddenDeck += card
            }
            is PlaceholderCard -> {}  // Placeholder cards do nothing
        }

        // Replace the drawn card with a placeholder and shift remaining cards
        game.currentState.openCards[cardIndex] = PlaceholderCard
        shiftBoardAndRefill(cardIndex)

        // refresh to show draw card animation & choose tiles optionally based on drawn card & chosen stack
        onAllRefreshables { refreshAfterDrawCard(card, cardIndex, chooseTilesByBoard, chooseTilesByCard) }
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
    fun drawCard(cardStack: Int): List<BonsaiTile> {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")

        if (game.currentState.openCards.isEmpty()) throw IllegalStateException("No available cards to draw")

        // Determine which tiles to assign based on the card's position in openCards
        val acquiredTiles =
            when (cardStack) {
                0 -> emptyList() // Position 0: No tiles awarded
                2 -> listOf(BonsaiTile(TileType.WOOD), BonsaiTile(TileType.FLOWER))
                3 -> listOf(BonsaiTile(TileType.LEAF), BonsaiTile(TileType.FRUIT))
                else -> emptyList() // Any other position: No tiles (default case)
            }

        return acquiredTiles
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
     * @param choice The tile type chosen by the player (WOOD or LEAF).
     */

    fun applyTileChoice(
        choice: TileType,
        chooseFromAll: Boolean = false,
    ) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")

        // Validate that the choice is valid
        if (!chooseFromAll) {
            require(choice == TileType.WOOD || choice == TileType.LEAF) { "Invalid choice" }
        } else {
            require(
                choice == TileType.WOOD || choice == TileType.LEAF || choice == TileType.FLOWER || choice == TileType.FRUIT,
            ) { "Invalid choice" }
        }
        // Add the chosen tile to the current player's supply
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        currentPlayer.supply += listOf(BonsaiTile(choice))

        // Notify the GUI to update with the drawn card (same as in drawCard)
        onAllRefreshables { refreshAfterChooseTile() }
    }

    /**
     * Shifts all face-up cards to the right and fills the empty position with a new card.
     */
    fun shiftBoardAndRefill(cardStack: Int) {
        val game = rootService.currentGame ?: throw IllegalStateException("No active game")

        for (i in cardStack downTo 1) {
            game.currentState.openCards[i] = game.currentState.openCards[i - 1]
        }

        if (game.currentState.drawStack.isNotEmpty()) {
            val newCard = game.currentState.drawStack.pop()
            game.currentState.openCards[0] = newCard
        } else {
            game.currentState.openCards[0] = PlaceholderCard // Ensures a valid state
        }
    }

    /**
    In the improbable case that, at the beginning of a turn, it is not
    possible to add a wood tile to the [Player.bonsai], player may remove the least number of
    tiles from it (put them in the common supply) needed to make it possible again.
     * Preconditions:
     * - A game must be active.
     * - The choice must be from players bonsai.
     * - it should not be possible to play a wood
     * - removed tile should be a part of leat number of tiles to be removed
     *
     * **Postconditions:**
     * - tile is removed from player bonsai.
     *
     * @throws IllegalStateException If there is no active game, invalid tile.
     */
    fun removeTile(tile: BonsaiTile) {
        // check if game is running
        val game = rootService.currentGame
        checkNotNull(game) { "there is no active game" }

        // is tile in current player bonsai
        val currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        val currentPlayerBonsaiTiles = currentPlayer.bonsai.tiles()
        check(currentPlayerBonsaiTiles.contains(tile)) { "cant remove a tile not in players bonsai" }

        // get first wood tile
        val grid = currentPlayer.bonsai.grid

        // is it possible to play wood tile
        check(
            !currentPlayerBonsaiTiles.any { bonsaiTile ->
                (bonsaiTile.type == TileType.WOOD && grid.getNeighbors(bonsaiTile).size < 6)
            },
        ) { "player can play wood" }

        // is it part of the least number of tiles to be removed to make placing a wood possible
        val leastGroupOfTilesToBeRemoved = TileUtils.leastGroupOfTilesToBeRemoved(currentPlayerBonsaiTiles,grid)
        check(leastGroupOfTilesToBeRemoved.contains(tile)) {
            "tile not part of the least number of tiles to be removed to make placing a wood possible"
        }

        // remove tile from bonsai tree
        currentPlayer.bonsai.grid.remove(tile)

        onAllRefreshables {
            refreshAfterRemoveTile()
        }
    }

}
