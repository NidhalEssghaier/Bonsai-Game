package service

import entity.*
import helper.push
import kotlin.test.*

/**
 * Class for testing the endTurn method of the PlayerActionService class.
 */
class EndTurnTest {

    /** Test with no running game */
    @Test
    fun illegalStateTest() {
        // Services to test with
        val rootService = RootService()
        val playerActionService = rootService.playerActionService

        // Test
        val exception = assertFailsWith(exceptionClass = IllegalStateException::class, block = {
            playerActionService.endTurn()
        })
        assertEquals("No game is currently active.", exception.message)
    }

    /** Test discarding tiles from supply */
    @Test
    fun discardTilesTest() {
        // Services to test with
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService

        // Prepare Test
        gameService.startNewGame(
            listOf(
                Triple("Tim", 0, PotColor.RED),
                Triple("Tom", 0, PotColor.BLUE)
            ),
            1,
            listOf(
                GoalColor.ORANGE,
                GoalColor.BROWN,
                GoalColor.GREEN
            )
        )
        assertNotNull(rootService.currentGame)
        repeat (6) {
            rootService.currentGame!!.currentState.players[0].supply.add(
                BonsaiTile(TileType.WOOD)
            )
        }

        // Test
        assertFalse { testRefreshable.refreshAfterDiscardTileCalled }
        playerActionService.endTurn()
        assertTrue { testRefreshable.refreshAfterDiscardTileCalled }
    }

    /** Test changes in undo- and redoStack */
    @Test
    fun undoRedoTest() {
        // Services to test with
        val rootService = RootService()
        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService

        // Prepare Test
        gameService.startNewGame(
            listOf(
                Triple("Tim", 0, PotColor.RED),
                Triple("Tom", 0, PotColor.BLUE)
            ),
            1,
            listOf(
                GoalColor.ORANGE,
                GoalColor.BROWN,
                GoalColor.GREEN
            )
        )
        assertNotNull(rootService.currentGame)
        rootService.currentGame!!.undoStack.clear()
        rootService.currentGame!!.redoStack.push(
            rootService.currentGame!!.currentState.copy()
        )

        // Test
        assert(rootService.currentGame!!.undoStack.isEmpty())
        assert(!rootService.currentGame!!.redoStack.isEmpty())
        playerActionService.endTurn()
        assert(!rootService.currentGame!!.undoStack.isEmpty())
        assert(rootService.currentGame!!.redoStack.isEmpty())
    }

    /** Test advancing endGameCounter */
    @Test
    fun endGameCounterTest() {
        // Services to test with
        val rootService = RootService()
        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService

        // Prepare Test
        gameService.startNewGame(
            listOf(
                Triple("Tim", 0, PotColor.RED),
                Triple("Tom", 0, PotColor.BLUE)
            ),
            1,
            listOf(
                GoalColor.ORANGE,
                GoalColor.BROWN,
                GoalColor.GREEN
            )
        )
        assertNotNull(rootService.currentGame)
        rootService.currentGame!!.currentState.drawStack.clear()
        val counter = rootService.currentGame!!.currentState.endGameCounter + 1

        // Test
        playerActionService.endTurn()
        assertEquals(counter, rootService.currentGame!!.currentState.endGameCounter)
    }

    /** Test if endGame is called when it should be */
    @Test
    fun endGameTest() {
        // Services to test with
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService

        // Prepare Test
        gameService.startNewGame(
            listOf(
                Triple("Tim", 0, PotColor.RED),
                Triple("Tom", 0, PotColor.BLUE)
            ),
            1,
            listOf(
                GoalColor.ORANGE,
                GoalColor.BROWN,
                GoalColor.GREEN
            )
        )
        assertNotNull(rootService.currentGame)
        rootService.currentGame!!.currentState.endGameCounter = 2
        rootService.currentGame!!.currentState.drawStack.clear()

        // Test
        assertFalse { testRefreshable.refreshAfterEndGameCalled }
        playerActionService.endTurn()
        assertTrue { testRefreshable.refreshAfterEndGameCalled }
    }

    /** Test advancement to the next player without discarding tiles */
    @Test
    fun nextPlayerTest() {
        // Services to test with
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService

        // Prepare Test
        gameService.startNewGame(
            listOf(
                Triple("Tim", 0, PotColor.RED),
                Triple("Tom", 0, PotColor.BLUE)
            ),
            1,
            listOf(
                GoalColor.ORANGE,
                GoalColor.BROWN,
                GoalColor.GREEN
            )
        )
        assertNotNull(rootService.currentGame)

        // Test
        assertFalse { testRefreshable.refreshAfterEndTurnCalled }
        assertFalse { testRefreshable.refreshAfterEndGameCalled }
        assertFalse { testRefreshable.refreshAfterDiscardTileCalled }
        assertEquals(0, rootService.currentGame!!.currentState.endGameCounter)
        playerActionService.endTurn()
        assertEquals(1, rootService.currentGame!!.currentState.currentPlayer)
        assertTrue { testRefreshable.refreshAfterEndTurnCalled }
        assertFalse { testRefreshable.refreshAfterEndGameCalled }
        assertFalse { testRefreshable.refreshAfterDiscardTileCalled }
        assertEquals(0, rootService.currentGame!!.currentState.endGameCounter)

        // Test setting current player back to 0
        playerActionService.endTurn()
        assertEquals(0, rootService.currentGame!!.currentState.currentPlayer)
    }

    /** Test resetting of hasCultivated and hasDrawnCards */
    @Test
    fun resettingTest() {
        // Services to test with
        val rootService = RootService()
        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService

        // Prepare Test
        gameService.startNewGame(
            listOf(
                Triple("Tim", 0, PotColor.RED),
                Triple("Tom", 0, PotColor.BLUE)
            ),
            1,
            listOf(
                GoalColor.ORANGE,
                GoalColor.BROWN,
                GoalColor.GREEN
            )
        )
        assertNotNull(rootService.currentGame)
        rootService.currentGame!!.currentState.players[0].hasCultivated = true
        rootService.currentGame!!.currentState.players[1].hasDrawnCard = true

        // Test
        playerActionService.endTurn()
        assertFalse { rootService.currentGame!!.currentState.players[0].hasCultivated }
        assertFalse { rootService.currentGame!!.currentState.players[1].hasDrawnCard }
    }

    /** Test if usedHelperCards and usedHelperTiles are handled properly */
    @Test
    fun helperTest() {
        // Services to test with
        val rootService = RootService()
        val gameService = rootService.gameService
        val playerActionService = rootService.playerActionService

        // Prepare Test
        gameService.startNewGame(
            listOf(
                Triple("Tim", 0, PotColor.RED),
                Triple("Tom", 0, PotColor.BLUE)
            ),
            1,
            listOf(
                GoalColor.ORANGE,
                GoalColor.BROWN,
                GoalColor.GREEN
            )
        )
        assertNotNull(rootService.currentGame)
        val card = HelperCard(listOf(TileType.GENERIC, TileType.FRUIT), 20)
        rootService.currentGame!!.currentState.players[0].hiddenDeck.add(card)
        rootService.currentGame!!.currentState.players[0].usedHelperTiles.add(
            TileType.FRUIT
        )

        // Test
        playerActionService.endTurn()
        assert(rootService.currentGame!!.currentState.players[0].usedHelperCards.contains(card))
        assert(rootService.currentGame!!.currentState.players[0].usedHelperTiles.isEmpty())
    }

}
