package service

import AbstractRefreshingService

class PlayerActionService(rootService: RootService):AbstractRefreshingService(){
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

}
