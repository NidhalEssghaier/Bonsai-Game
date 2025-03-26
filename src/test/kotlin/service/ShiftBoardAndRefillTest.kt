package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class for Unit Tests for [PlayerActionService.shiftBoardAndRefill]
 */
class ShiftBoardAndRefillTest {
    private lateinit var rootService: RootService
    private lateinit var gameService: GameService
    private lateinit var playerActionService: PlayerActionService
    private lateinit var testRefreshable: TestRefreshable
    private lateinit var game: BonsaiGame
    private lateinit var currentPlayer: Player

    /**
     * Setup the test environment
     */
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
     * Tests shifting board and refilling when a valid index (2) is given.
     * Ensures cards are shifted correctly, a new card is drawn, and the draw stack size decreases.
     */
    @Test
    fun testShiftBoardAndRefillValidIndex() {
        val initialOpenCards = game.currentState.openCards.toList()
        val initialDrawStackSize = game.currentState.drawStack.size
        val initialDrawStackFirstCard = game.currentState.drawStack.first()

        playerActionService.shiftBoardAndRefill(2) // Shift from index 2

        // Ensure cards have shifted correctly
        assertEquals(initialOpenCards[1], game.currentState.openCards[2])
        assertEquals(initialOpenCards[0], game.currentState.openCards[1])
        assertEquals(initialDrawStackFirstCard, game.currentState.openCards[0])
        assertEquals(game.currentState.drawStack.size, initialDrawStackSize - 1) // Draw stack reduced
    }

    /**
     * Tests shifting board when the draw stack is empty.
     * Ensures that shifting still happens, but a `PlaceholderCard` is placed instead of a new card.
     */
    @Test
    fun testShiftBoardAndRefillWithEmptyDrawStack() {
        game.currentState.drawStack.clear() // Simulate empty drawStack
        game.currentState.drawStack.isEmpty()

        val initialOpenCards = game.currentState.openCards.toList()

        playerActionService.shiftBoardAndRefill(2) // Attempt to shift with no new card

        // Ensure cards still shift correctly
        assertEquals(initialOpenCards[1], game.currentState.openCards[2])
        assertEquals(initialOpenCards[0], game.currentState.openCards[1])

        // Check for `PlaceholderCard`, not `null`
        assertTrue(game.currentState.openCards[0] is PlaceholderCard)
    }

    /**
     * Tests shifting board when there is no active game.
     * Ensures an exception is thrown.
     */
    @Test
    fun testShiftBoardAndRefillWithNoActiveGame() {
        val newRootService = RootService()
        val newPlayerActionService = newRootService.playerActionService

        assertThrows<IllegalStateException> { newPlayerActionService.shiftBoardAndRefill(1) } // No game
    }

    /**
     * Tests shifting the board after performing a `meditate` action.
     * Ensures that meditation triggers a shift, the first card in the draw stack is placed at index 0,
     * and the draw stack size decreases.
     */
    @Test
    fun testShiftBoardAfterMeditate() {
        val masterCard = MasterCard(tiles = listOf(TileType.WOOD, TileType.LEAF), id = 2)
        val initialDrawStackSize = game.currentState.drawStack.size
        game.currentState.openCards[2] = masterCard
        val initialDrawStackFirstCard = game.currentState.drawStack.first()
        val initialBoardCardP1 = game.currentState.openCards[0]
        val initialBoardCardP2 = game.currentState.openCards[1]
        val initialBoardCardP3 = game.currentState.openCards[3]

        playerActionService.meditate(masterCard) // Meditate should trigger shift

        // Ensure a new card is in position 0
        assertEquals(initialDrawStackFirstCard, game.currentState.openCards[0])
        assertEquals(initialBoardCardP1, game.currentState.openCards[1])
        assertEquals(initialBoardCardP2, game.currentState.openCards[2])
        assertEquals(initialBoardCardP3, game.currentState.openCards[3])
        assertEquals(game.currentState.drawStack.size, initialDrawStackSize - 1) // Draw stack reduced
    }
}
