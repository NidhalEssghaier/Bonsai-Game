package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class DrawCardTest {
    private lateinit var rootService: RootService
    private lateinit var gameService: GameService
    private lateinit var playerActionService: PlayerActionService
    private lateinit var testRefreshable: TestRefreshable
    private lateinit var game: BonsaiGame
    private lateinit var currentPlayer: Player

    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = rootService.gameService
        playerActionService = rootService.playerActionService
        testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        gameService.startNewGame(players = createTestPlayers(), 3, createGoalCards())

        game = rootService.currentGame ?: throw IllegalStateException("Game should be initialized")
        currentPlayer = game.currentState.players[game.currentState.currentPlayer]
    }

    private fun createTestPlayers(): MutableList<Triple<String, Int, PotColor>> =
        mutableListOf(
            Triple("Alice", 0, PotColor.RED),
            Triple("Bob", 0, PotColor.PURPLE),
        )

    private fun createGoalCards(): List<GoalColor> = listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)

    /**
     * Tests drawing a card from position 0, which should return no tiles.
     */
    @Test
    fun testDrawCardWithPosition0() {
        val tiles = playerActionService.drawCard(0)

        assertTrue(tiles.isEmpty())
    }

    /**
     * Tests drawing a card from position 2, which should return WOOD and FLOWER tiles.
     */
    @Test
    fun testDrawCardWithValidPosition2() {
        val tiles = playerActionService.drawCard(2)

        assertEquals(2, tiles.size)
        assertTrue(tiles.any { it.type == TileType.WOOD })
        assertTrue(tiles.any { it.type == TileType.FLOWER })
    }

    /**
     * Tests drawing a card from position 3, which should return LEAF and FRUIT tiles.
     */
    @Test
    fun testDrawCardWithValidPosition3() {
        val tiles = playerActionService.drawCard(3)

        assertEquals(2, tiles.size)
        assertTrue(tiles.any { it.type == TileType.LEAF })
        assertTrue(tiles.any { it.type == TileType.FRUIT })
    }

    /**
     * Tests drawing a card from invalid position (e.g., 4), which should return no tiles.
     */
    @Test
    fun testDrawCardWithInvalidPositionReturnsEmptyList() {
        val tiles = playerActionService.drawCard(4)

        assertTrue(tiles.isEmpty())
    }

    /**
     *  Tests attempting to draw a card when the openCards stack is empty, which should throw an exception.
     **/

    @Test
    fun testDrawCardWithEmptyOpenCards() {
        game.currentState.openCards.clear()
        val exception = assertThrows<IllegalStateException> { playerActionService.drawCard(2) } // Should fail
        assertEquals(exception.message, "No available cards to draw")
    }

    /**
     *  Tests drawing a card when no active game exists, which should throw an exception.
     **/
    @Test
    fun testDrawCardWithNoActiveGame() {
        val newRootService = RootService()
        val newPlayerActionService = newRootService.playerActionService

        val exception = assertThrows<IllegalStateException> { newPlayerActionService.drawCard(2) } // No game started
        assertEquals(exception.message, "No active game")
    }
}
