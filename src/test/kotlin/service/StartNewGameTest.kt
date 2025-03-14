package service

import entity.GoalCard
import entity.GoalColor
import entity.GoalDifficulty
import entity.PotColor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StartNewGameTest {
    @Test
    fun testStartNewGame() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        /*val testRefreshable = TestRefreshable()
        assertFalse { testRefreshable.refreshAfterStartNewGameCalled }*/

        //create players (two local players)
        val player0 = Triple("Alice",0,PotColor.RED)
        val player1 = Triple("Bob",0,PotColor.PURPLE)
        val players = mutableListOf(player0,player1)

        val goalCards = listOf(
            GoalCard(5,GoalColor.BROWN,GoalDifficulty.LOW),
            GoalCard(10,GoalColor.BROWN,GoalDifficulty.INTERMEDIATE),
            GoalCard(15,GoalColor.BROWN,GoalDifficulty.HARD),

            GoalCard(6,GoalColor.GREEN,GoalDifficulty.LOW),
            GoalCard(9,GoalColor.GREEN,GoalDifficulty.INTERMEDIATE),
            GoalCard(12,GoalColor.GREEN,GoalDifficulty.HARD),

            GoalCard(9,GoalColor.ORANGE,GoalDifficulty.LOW),
            GoalCard(11,GoalColor.ORANGE,GoalDifficulty.INTERMEDIATE),
            GoalCard(13,GoalColor.ORANGE,GoalDifficulty.HARD)
        )

        gameService.startNewGame(players,3,goalCards)
        //assertTrue { testRefreshable.refreshAfterStartNewGameCalled }

        var game = rootService.currentGame
        checkNotNull(game)
        assertEquals(game.currentState.drawStack.size, 28)

        //test for three players (two players and a bot)
        val bot1 = Triple("Random",2,PotColor.BLUE)
        players.add(bot1)
        gameService.startNewGame(players,1,goalCards)
        game = rootService.currentGame
        checkNotNull(game)
        assertEquals(game.currentState.drawStack.size, 39)

        //test for four players (two players and two bots)
        val bot2 = Triple("Smart",3,PotColor.GRAY)
        players.add(bot2)
        gameService.startNewGame(players,2,goalCards)
        game = rootService.currentGame
        checkNotNull(game)
        assertEquals(game.currentState.drawStack.size, 43)

    }
}