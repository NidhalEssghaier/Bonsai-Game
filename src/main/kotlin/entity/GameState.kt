package entity

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import serializer.ArrayDequeZenCardSerializer

/**
 * Entity to represent the game state
 *
 * @property gameSpeed Value for the simulation speed
 * @property players List of players in the configured sorting order
 * @property goalCards List of chosen goal cards
 * @property drawStack The card stack in the shop
 * @property openCards List of the available cards in the shop
 * @property currentPlayer The active player, who can do actions.
 * @property endGameCounter Is used at the end of the game to allow every player only one last turn.
 */
@Serializable
class GameState(
    val gameSpeed: Int,
    val players: List<@Polymorphic Player>,
    val goalCards: MutableList<GoalCard>,
    @Serializable(with = ArrayDequeZenCardSerializer::class)
    val drawStack: ArrayDeque<ZenCard>,
    val openCards: MutableList<@Polymorphic ZenCard>,

    var currentPlayer: Int = 0,
    var endGameCounter: Int = 0
)
{
    /**
     * Creates a deep copy of the game state.
     * @return A deep copy of the game state.
     */
    fun copy(): GameState {
        return GameState(
            gameSpeed,
            players.map { it.copy() },
            goalCards,
            ArrayDeque(drawStack),
            openCards,
            this.currentPlayer,
            this.endGameCounter
        )
    }
}
