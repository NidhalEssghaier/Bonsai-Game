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

    private fun createTestPlayers(): MutableList<Triple<String, Int, PotColor>> {
        return mutableListOf(
            Triple("Alice", 0, PotColor.RED),
            Triple("Bob", 0, PotColor.PURPLE)
        )
    }

    private fun createGoalCards(): List<GoalColor> {
        return listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)
    }

    /**
     * Tests applying a valid tile choice (WOOD) and ensuring it is added to the player's supply.
     */
    @Test
    fun testApplyTileChoiceWithWood() {
        val supplyBefore = currentPlayer.supply.size

        playerActionService.applyTileChoice(TileType.WOOD, chooseFromAll = false) // Choosing WOOD

        assertEquals(supplyBefore + 1, currentPlayer.supply.size) // 1 tile should be added
        assertTrue { currentPlayer.supply.any { it.type == TileType.WOOD } }
        assertTrue { testRefreshable.refreshAfterChooseTileCalled }
    }

    /**
     * Tests applying a valid tile choice (LEAF) and ensuring it is added to the player's supply.
     */
    @Test
    fun testApplyTileChoiceWithLeaf() {
        val supplyBefore = currentPlayer.supply.size

        playerActionService.applyTileChoice(TileType.LEAF, chooseFromAll = false) // Choosing LEAF

        assertEquals(supplyBefore + 1, currentPlayer.supply.size) // 1 tile should be added
        assertTrue { currentPlayer.supply.any { it.type == TileType.LEAF } }
        assertTrue { testRefreshable.refreshAfterChooseTileCalled }
    }

    /**
     * Tests applying a valid tile choice (FLOWER) when choosing from all options.
     */
    @Test
    fun testApplyTileChoiceWithFlower() {
        val supplyBefore = currentPlayer.supply.size

        playerActionService.applyTileChoice(TileType.FLOWER, chooseFromAll = true) // Choosing FLOWER

        assertEquals(supplyBefore + 1, currentPlayer.supply.size) // 1 tile should be added
        assertTrue { currentPlayer.supply.any { it.type == TileType.FLOWER } }
        assertTrue { testRefreshable.refreshAfterChooseTileCalled }
    }

    /**
     * Tests applying a valid tile choice (FRUIT) when choosing from all options.
     */
    @Test
    fun testApplyTileChoiceWithFruit() {
        val supplyBefore = currentPlayer.supply.size

        playerActionService.applyTileChoice(TileType.FRUIT, chooseFromAll = true) // Choosing FRUIT

        assertEquals(supplyBefore + 1, currentPlayer.supply.size) // 1 tile should be added
        assertTrue { currentPlayer.supply.any { it.type == TileType.FRUIT } }
        assertTrue { testRefreshable.refreshAfterChooseTileCalled }
    }

    /**
     * Tests applying an invalid tile choice (FLOWER) when restricted to WOOD or LEAF.
     */
    @Test
    fun testApplyTileChoiceWithInvalidChoice() {
        assertThrows<IllegalArgumentException> {
            playerActionService.applyTileChoice(TileType.FLOWER, chooseFromAll = false) // Invalid choice
        }
    }

    /**
     * Tests applying an invalid tile choice (FLOWER) when restricted to WOOD or LEAF.
     */
    @Test
    fun testApplyTileChoiceWithInvalidChoice2() {
        assertThrows<IllegalArgumentException> {
            playerActionService.applyTileChoice(TileType.GENERIC, chooseFromAll = true) // Invalid choice
        }
    }

    /**
     * Tests applying a tile choice when no active game exists, which should throw an exception.
     */
    @Test
    fun testApplyTileChoiceWithNoActiveGame() {
        val newRootService = RootService()
        val newPlayerActionService = newRootService.playerActionService

        assertThrows<IllegalStateException> {
            newPlayerActionService.applyTileChoice(TileType.WOOD) // No active game
        }
    }
}
