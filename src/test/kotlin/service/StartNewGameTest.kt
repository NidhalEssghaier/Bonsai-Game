package service

import entity.GoalColor
import entity.PotColor
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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

        var goalColors = listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)

        gameService.startNewGame(players, 3, goalColors)
        assertTrue { testRefreshable.refreshAfterStartNewGameCalled }

        var game = rootService.currentGame
        checkNotNull(game)
        assertEquals(game.currentState.drawStack.size, 28)

        // test with wrong player-type initialization
        val playerWrong = Triple("Error", 5, PotColor.BLUE)
        players.add(playerWrong)
        assertThrows<IllegalArgumentException> { gameService.startNewGame(players, 2, goalColors) }
        players.remove(playerWrong)

        // test for three players (two players and a bot)
        goalColors = listOf(GoalColor.RED, GoalColor.BLUE, GoalColor.ORANGE)
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

    @Test
    fun `test start game with wrong player amount`() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        val gameService = rootService.gameService
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }

        val goals = listOf(GoalColor.BROWN, GoalColor.ORANGE, GoalColor.GREEN)

        // test with only one player
        val players: MutableList<Triple<String, Int, PotColor>> = mutableListOf()
        val player0 = Triple("Alice", 0, PotColor.RED)
        players.add(player0)

        var exception =
            assertThrows<IllegalArgumentException> {
                gameService.startNewGame(players, 1, goals)
            }
        assertEquals("Need 2-4 players to play the game", exception.message)
        assertNull(rootService.currentGame)
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }
        testRefreshable.reset()

        // test with five players
        repeat(4) { players.add(player0) }
        exception =
            assertThrows<IllegalArgumentException> {
                gameService.startNewGame(players, 1, goals)
            }
        assertEquals("Need 2-4 players to play the game", exception.message)
        assertNull(rootService.currentGame)
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }
    }

    @Test
    fun `test start game with wrong goal colors selected`() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        val gameService = rootService.gameService
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }

        val players: MutableList<Triple<String, Int, PotColor>> = mutableListOf()
        repeat(4) { players.add(Triple("Alice", 0, PotColor.RED)) }

        // Test with same goal colors
        var goals = listOf(GoalColor.BROWN, GoalColor.BROWN, GoalColor.GREEN)
        var exception =
            assertThrows<IllegalArgumentException> {
                gameService.startNewGame(players, 1, goals)
            }
        assertEquals("Must select 3 different goal colors", exception.message)
        assertNull(rootService.currentGame)
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }
        testRefreshable.reset()

        // Test with too few goal colors
        goals = listOf(GoalColor.BROWN, GoalColor.GREEN)
        exception =
            assertThrows<IllegalArgumentException> {
                gameService.startNewGame(players, 1, goals)
            }
        assertEquals("Must select 3 different goal colors", exception.message)
        assertNull(rootService.currentGame)
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }
        testRefreshable.reset()

        // Test with too many goal colors
        goals = listOf(GoalColor.BROWN, GoalColor.ORANGE, GoalColor.GREEN, GoalColor.RED)
        exception =
            assertThrows<IllegalArgumentException> {
                gameService.startNewGame(players, 1, goals)
            }
        assertEquals("Must select 3 different goal colors", exception.message)
        assertNull(rootService.currentGame)
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }
        testRefreshable.reset()
    }
}
