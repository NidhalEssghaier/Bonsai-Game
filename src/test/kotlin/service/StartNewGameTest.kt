package service

import entity.GoalColor
import entity.PotColor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StartNewGameTest {
    @Test
    fun testStartNewGame() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        val gameService = rootService.gameService
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }

        // create players (two local players)
        val player0 = Triple("Alice", 0, PotColor.RED)
        val player1 = Triple("Bob", 0, PotColor.PURPLE)
        val players = mutableListOf(player0, player1)

        val goalColors = listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)

        gameService.startNewGame(players, 3, goalColors)
        assertTrue { testRefreshable.refreshAfterStartNewGameCalled }

        var game = rootService.currentGame
        checkNotNull(game)
        assertEquals(game.currentState.drawStack.size, 28)

        // test wrong initialization
        val playerWrong = Triple("Error", 5, PotColor.BLUE)
        players.add(playerWrong)
        assertThrows<IllegalArgumentException> { gameService.startNewGame(players, 2, goalColors) }
        players.remove(playerWrong)

        // test for three players (two players and a bot)
        val bot1 = Triple("Random", 2, PotColor.BLUE)
        players.add(bot1)
        gameService.startNewGame(players, 1, goalColors)
        game = rootService.currentGame
        checkNotNull(game)
        assertEquals(game.currentState.drawStack.size, 39)

        // test for four players (two players and two bots)
        val bot2 = Triple("Smart", 3, PotColor.GRAY)
        players.add(bot2)
        gameService.startNewGame(players, 2, goalColors)
        game = rootService.currentGame
        checkNotNull(game)
        assertEquals(game.currentState.drawStack.size, 43)
    }
}
