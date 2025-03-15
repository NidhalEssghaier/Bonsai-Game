package service

import entity.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EndGameTest {
    @Test
    fun testEndGame() {
        // start a game
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        val gameService = rootService.gameService
        assertFalse { testRefreshable.refreshAfterEndGameCalled }

        val player0 = Triple("Alice", 0, PotColor.RED)
        val player1 = Triple("Bob", 0, PotColor.PURPLE)
        val players = mutableListOf(player0, player1)

        val goalColors = listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)

        gameService.startNewGame(players, 3, goalColors)

        val game = rootService.currentGame
        checkNotNull(game)

        game.currentState.players[0]
            .bonsai.grid[0, -1] = BonsaiTile(TileType.WOOD) // 0
        game.currentState.players[0]
            .bonsai.grid[1, -1] = BonsaiTile(TileType.LEAF) // 3
        game.currentState.players[0]
            .bonsai.grid[1, -2] = BonsaiTile(TileType.LEAF) // 3
        game.currentState.players[0]
            .bonsai.grid[1, -3] = BonsaiTile(TileType.FLOWER) // 5
        game.currentState.players[0]
            .bonsai.grid[2, -2] = BonsaiTile(TileType.FRUIT) // 7

        game.currentState.players[0]
            .acceptedGoals
            .add(game.currentState.goalCards[0]) // 5

        val parchmentCard = ParchmentCard(1, ParchmentCardType.WOOD, 99)
        game.currentState.players[0]
            .hiddenDeck
            .add(parchmentCard)
        // +2 through wood tiles and parchment card

        assertThrows<IllegalStateException> { gameService.endGame() }
        game.currentState.drawStack.clear()

        val points = gameService.endGame()
        val player0Points = points[0].second
        assertEquals(player0Points, 25)
        assertTrue { testRefreshable.refreshAfterEndGameCalled }
    }
}
