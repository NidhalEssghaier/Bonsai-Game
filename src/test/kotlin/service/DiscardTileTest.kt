package service

import entity.*
import kotlin.test.*

/**
 * Class for testing the endTurn method of the PlayerActionService class.
 */
class DiscardTileTest {

    /** Test with no running game */
    @Test
    fun illegalStateTest() {
        // Services to test with
        val rootService = RootService()
        val playerActionService = rootService.playerActionService

        // Test
        val exception = assertFailsWith(exceptionClass = IllegalStateException::class, block = {
            playerActionService.discardTile(BonsaiTile(TileType.UNPLAYABLE))
        })
        assertEquals("No game is currently active.", exception.message)
    }

    /** Test discarding a tile not in the active players supply */
    @Test
    fun notInSupplyTest() {
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

        // Test
        val exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            playerActionService.discardTile(BonsaiTile(TileType.FRUIT))
        })
        assertEquals(
            "The given tile is not in the active players supply.",
            exception.message
        )
    }

    /** Test with too few tiles in the active players supply to discard */
    @Test
    fun tooFewSupplyTilesTest() {
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
        val tile = BonsaiTile(TileType.FRUIT)
        rootService.currentGame!!.currentState.players[0].supply.add(tile)

        // Test
        val exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            playerActionService.discardTile(tile)
        })
        assertEquals(
            "The current supply size is equal to or lower than the supply tile limit.",
            exception.message
        )
    }

    @Test
    fun tileDiscardedTest() {
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
        repeat (5) {
            rootService.currentGame!!.currentState.players[0].supply.add(
                BonsaiTile(TileType.WOOD)
            )
        }
        val tile = BonsaiTile(TileType.FRUIT)
        rootService.currentGame!!.currentState.players[0].supply.add(tile)

        // Test
        assertFalse { testRefreshable.refreshAfterDiscardTileCalled }
        playerActionService.discardTile(tile)
        assertTrue { testRefreshable.refreshAfterDiscardTileCalled }
    }

}
