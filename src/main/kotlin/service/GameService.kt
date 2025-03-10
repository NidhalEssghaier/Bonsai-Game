package service

import AbstractRefreshingService
import entity.*
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.util.Stack

class GameService(private val rootService:RootService):AbstractRefreshingService() {

    /**
     * Starts a new game and prepares different game elements
     * @param players A list of pairs that represent a player.
     * The pair consists of the player name and the type of the player as number:
     * local player (0), remote player (1), random bot (2), smart bot (3)
     * @param speed The simulation speed of the game.
     * @param remote If the game is played in remote or in hotseat mode.
     *
     * Preconditions:
     * - The application is running.
     * - The sorting order of the players has been defined.
     * - The type of the players has been defined.
     * - The simulation speed has been set.
     * - The game type has been set.
     *
     * Postconditions:
     * - A game was created.
     * - The goal plates have been configured.
     * - The card shop has been prepared.
     * - The tile storage has been prepared.
     * - Every player has a plant pot and a seishi plate.
     * - The inventories of the players have been filled with tiles.
     *
     * @return This method does not return anything ('Unit').
     *
     * @throws IllegalArgumentException If a player name or type is missing.
     * @throws IllegalArgumentException If the simulation speed is not set.
     * @throws IllegalArgumentException If game type and player types are not matching
     * (e.g. local players in remote).
     *
     * @sample startNewGame(mutableListOf(Pair("Max Mustermann",0)),3,false)
     */
    fun startNewGame(players: List<Pair<String, Int>>, speed: Int, remote: Boolean, goalCards: List<GoalCard>) {

        //prepare zen Cards
        val zenCards = listOf<ZenCard>(ParchmentCard(ParchmentCardType.MASTER),
            ParchmentCard(ParchmentCardType.GROWTH),
            ParchmentCard(ParchmentCardType.HELPER),
            ParchmentCard(ParchmentCardType.FLOWER),
            ParchmentCard(ParchmentCardType.FRUIT),
            ParchmentCard(ParchmentCardType.LEAF),
            ParchmentCard(ParchmentCardType.WOOD))

        //sort players
        val sortedPlayers = createPlayers(players)

        //start new game
        val bonsaiGame = BonsaiGame(speed, sortedPlayers)
        bonsaiGame.drawStack.pushAll(zenCards)
        rootService.currentGame =bonsaiGame

        onAllRefreshables { refreshAfterStartNewGame() }
    }

    private fun createPlayers(players: List<Pair<String, Int>>): List<Player> {
        val sortedPlayers = players.sortedBy { it.first }

        return sortedPlayers.map { (name, _) ->
            val grid = mapOf<HexagonGrid<HexagonView>, BonsaiTile>()
            val tiles = listOf<BonsaiTile>()
            val playerBonsai = Bonsai(grid, tiles)

            // should add logic for which types of player is it but have no info
            LocalPlayer(name, playerBonsai)
        }
    }

    /**
     * Ends the game and evaluates which player won.
     *
     * This method does not accept parameters.
     *
     * Preconditions:
     * - A game was started and is running.
     * - All cards have been revealed.
     *
     * Postconditions:
     * - All players are evaluated based on their goal tiles, as well as on the basic multipliers
     *   and parchment card multipliers applied to their bonsai and hidden cards.
     * - The player with the highest number of points is declared the winner.
     *
     * @returns This method does not return anything (`Unit`).
     *
     * @throws IllegalStateException If there is no started and running game,
     *                               or if at least one card has not been revealed.
     *
     * @sample endGame()
     */

    fun endGame() {
        // Method implementation
    }

    /**
     * Loads a game from save file
     *
     * This method loads and unserializes game state from a save file and
     * notifies the UI layer to update the game sense.
     *
     * Preconditions:
     * - The application is running.
     * - The save file exists.
     * - The save file is readable.
     *
     * Postconditions:
     * - The game was created.
     * - The goal plates have returned to the recorded state.
     * - The card shop has returned to the recorded state.
     * - The tile storage has returned to the recorded state.
     * - All player's plan pots and cards return to the recorded state.
     * - The undo/redo Stack has returned to the recorded state
     *
     * @throws IllegalStateException if the save file doesn't exist
     * @throws IOException if the save file can't be read
     */
    fun loadGame() {}

    /**
     * Saves the current game state to a save file
     *
     * This method serializes the current game state and writes it to a save file.
     * If the save file in the default path doesn't exist, it will be created, otherwise
     * it will be overwritten.
     *
     * Preconditions:
     * - The game was started and is running.
     * - When the save file already exists, it is writable.
     * - The save file path is writable.
     *
     * Postconditions:
     * - If the save file didn't exist, it was created.
     * - Otherwise, it was overwritten by the newest serialized game state.
     *
     * @throws IllegalStateException if the game hasn't started yet
     * @throws IOException if the save file can't be written to
     */
    fun saveGame() {}
}
