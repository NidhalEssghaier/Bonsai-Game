package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


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
    private fun createTestPlayers(): MutableList<Triple<String, Int, PotColor>> {
        return mutableListOf(
            Triple("Alice", 0, PotColor.RED),
            Triple("Bob", 0, PotColor.PURPLE)
        )
    }

   private  fun createGoalCards() : List<GoalColor> {
        return  listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)
    }
    /**
     * Tests drawing a card from position 0, which should not award any tiles.
     */
    @Test
    fun testDrawCardWithValidPosition0() {
        val supplyBefore = currentPlayer.supply.size

        playerActionService.drawCard(0) // Position 0: No tiles awarded

        assertEquals(supplyBefore, currentPlayer.supply.size) // No tiles should be added

    }
    /**
     *  Tests drawing a card from position 2, which should add WOOD and FLOWER to the player's supply.
     */
  //  @Test
//    fun testDrawCardWithValidPosition2() {
//        val supplyBefore = currentPlayer.supply.size
//
//        playerActionService.drawCard(2) // Should add WOOD and FLOWER
//
////        assertEquals(supplyBefore + 2, currentPlayer.supply.size)
//        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.WOOD) }
//        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.FLOWER) }
//
//    }

    /**
     *  Tests drawing a card from position 3, which should add LEAF and FRUIT to the player's supply.
     * */
//    @Test
//    fun testDrawCardWithValidPosition3() {
//        val supplyBefore = currentPlayer.supply.size
//
//        playerActionService.drawCard(3) // Should add LEAF and FRUIT
//
//        assertEquals(supplyBefore + 2, currentPlayer.supply.size)
//        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.LEAF) }
//        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.FRUIT) }
//        assertTrue { testRefreshable.refreshAfterDrawCardCalled }
//    }

//    /**
//     * Tests drawing a card from position 1, which should not add tiles but trigger a UI prompt for tile selection.
//     **/
//    @Test
//    fun testDrawCardWithPosition1TriggersChoicePrompt() {
//        playerActionService.drawCard(1) // Should NOT add tiles yet, instead prompt GUI
//
//        assertTrue { testRefreshable.refreshToPromptTileChoiceCalled } // Ensure GUI prompt triggered
//    }

    /**
     *  Tests attempting to draw a card when the openCards stack is empty, which should throw an exception.
     **/

    @Test
    fun testDrawCardWithEmptyOpenCards() {
        game.currentState.openCards.clear()
        assertThrows<IllegalStateException> { playerActionService.drawCard(2) } // Should fail
    }
    /**
     *  Tests drawing a card when no active game exists, which should throw an exception.
     **/
    @Test
    fun testDrawCardWithNoActiveGame() {
        val newRootService = RootService()
        val newPlayerActionService = newRootService.playerActionService

        assertThrows<IllegalStateException> { newPlayerActionService.drawCard(2) } // No game started
    }

}
