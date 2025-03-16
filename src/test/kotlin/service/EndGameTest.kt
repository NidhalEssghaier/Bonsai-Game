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
        //start a game
        val rootService = RootService()
        val testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        val gameService = rootService.gameService
        assertFalse { testRefreshable.refreshAfterEndGameCalled }

        val player0 = Triple("Alice",0, PotColor.RED)
        val player1 = Triple("Bob",0, PotColor.PURPLE)
        val players = mutableListOf(player0,player1)

        val goalCards = mutableListOf(
            GoalCard(5, GoalColor.BROWN, GoalDifficulty.LOW),
            GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE),
            GoalCard(15, GoalColor.BROWN, GoalDifficulty.HARD),

            GoalCard(6, GoalColor.GREEN, GoalDifficulty.LOW),
            GoalCard(9, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE),
            GoalCard(12, GoalColor.GREEN, GoalDifficulty.HARD),

            GoalCard(9, GoalColor.ORANGE, GoalDifficulty.LOW),
            GoalCard(11, GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE),
            GoalCard(13, GoalColor.ORANGE, GoalDifficulty.HARD)
        )

        gameService.startNewGame(players,3,goalCards)

        val game = rootService.currentGame
        checkNotNull(game)

        game.currentState.players[0].bonsai.grid[0, -1] = BonsaiTile(TileType.WOOD) //0
        game.currentState.players[0].bonsai.grid[1, -1] = BonsaiTile(TileType.LEAF) //3
        game.currentState.players[0].bonsai.grid[1, -2] = BonsaiTile(TileType.LEAF) //3
        game.currentState.players[0].bonsai.grid[1, -3] = BonsaiTile(TileType.FLOWER) //5
        game.currentState.players[0].bonsai.grid[2, -2] = BonsaiTile(TileType.FRUIT) //7

        game.currentState.players[0].acceptedGoals.add(goalCards[0]) //5

        val parchmentCard = ParchmentCard(1,ParchmentCardType.WOOD,99)
        game.currentState.players[0].hiddenDeck.add(parchmentCard)
        //+2 through wood tiles and parchment card

        assertThrows<IllegalStateException> { gameService.endGame() }
        game.currentState.drawStack.clear()

        val points = gameService.endGame()
        val player0Points = points[0].second
        assertEquals(player0Points,25)
        assertTrue { testRefreshable.refreshAfterEndGameCalled }

    }
}