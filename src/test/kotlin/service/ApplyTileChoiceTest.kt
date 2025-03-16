package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplyTileChoiceTest {

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
    fun createTestPlayers(): MutableList<Triple<String, Int, PotColor>> {
        return mutableListOf(
            Triple("Alice", 0, PotColor.RED),
            Triple("Bob", 0, PotColor.PURPLE)
        )
    }

    fun createGoalCards(): MutableList<GoalCard> {
        return mutableListOf(
            GoalCard(5, GoalColor.BROWN, GoalDifficulty.LOW),
            GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE),
            GoalCard(15, GoalColor.BROWN, GoalDifficulty.HARD),

            GoalCard(6,GoalColor.GREEN,GoalDifficulty.LOW),
            GoalCard(9,GoalColor.GREEN,GoalDifficulty.INTERMEDIATE),
            GoalCard(12,GoalColor.GREEN,GoalDifficulty.HARD),

            GoalCard(9,GoalColor.ORANGE,GoalDifficulty.LOW),
            GoalCard(11,GoalColor.ORANGE,GoalDifficulty.INTERMEDIATE),
            GoalCard(13,GoalColor.ORANGE,GoalDifficulty.HARD)
        )
    }
    /**
     * Tests applying a valid tile choice (WOOD) and ensuring it is added to the player's supply.
     * */
    @Test
    fun testApplyTileChoiceWithWood() {
        val supplyBefore = currentPlayer.supply.size

        playerActionService.applyTileChoice(1, TileType.WOOD) // Choosing WOOD

        assertEquals(supplyBefore + 1, currentPlayer.supply.size) // 1 tile should be added
        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.WOOD) }
        assertTrue { testRefreshable.refreshAfterDrawCardCalled }
    }

    /**
    * Tests applying a valid tile choice (LEAF) and ensuring it is added to the player's supply.
    */
    @Test
    fun testApplyTileChoiceWithLeaf() {
        val supplyBefore = currentPlayer.supply.size

        playerActionService.applyTileChoice(1, TileType.LEAF) // Choosing LEAF

        assertEquals(supplyBefore + 1, currentPlayer.supply.size) // 1 tile should be added
        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.LEAF) }
        assertTrue { testRefreshable.refreshAfterDrawCardCalled }
    }

    /**
     *  Tests applying an invalid tile choice (FLOWER), which should throw an exception.
     */
    @Test
    fun testApplyTileChoiceWithInvalidChoice() {
        assertThrows<IllegalArgumentException> {
            playerActionService.applyTileChoice(1, TileType.FLOWER) // Invalid choice
        }
    }
    /**
     * Tests applying a tile choice when no active game exists, which should throw an exception.
     * */
    @Test
    fun testApplyTileChoiceWithNoActiveGame() {
        val newRootService = RootService()
        val newPlayerActionService = newRootService.playerActionService

        assertThrows<IllegalStateException> {
            newPlayerActionService.applyTileChoice(1, TileType.WOOD) // No active game
        }
    }
    /**
     *  Tests applying a tile choice with an invalid card stack index, which should throw an exception.
     */
    @Test
    fun testApplyTileChoiceWithInvalidCardStack() {
        assertThrows<IllegalArgumentException> {
            playerActionService.applyTileChoice(7, TileType.WOOD) // Invalid position
        }
    }

}
