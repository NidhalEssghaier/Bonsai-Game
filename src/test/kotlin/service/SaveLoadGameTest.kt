package service

import entity.GoalColor
import entity.PotColor
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class for Unit Tests for [GameService.saveGame] and [GameService.loadGame]
*/
class SaveLoadGameTest {


    /**
     * Test save game with no game
     */
    @Test
    fun `test save game with no game`() {
        val mc = RootService()
        val gameService = GameService(mc)

        //check saving a game
        val exception = assertThrows<IllegalStateException> {gameService.saveGame() }
        assertEquals("No game has been started yet.",exception.message)



    }

    /**
     * Test load game with no saved file
     */
    @Test
    fun `test load game with no saved file`() {
        val mc = RootService()
        val gameService = GameService(mc)

        //check saving a game
        val file = File("data/save.json")
        if (file.exists()) {
            file.delete()
        }
        val exception = assertThrows<IllegalStateException> {gameService.loadGame() }
        assertEquals("Save file doesn't exist.",exception.message)
        gameService.startNewGame(
            listOf(
                Triple("Anas",0,PotColor.PURPLE),
                Triple("Iyed",1,PotColor.PURPLE)
            ),
            5,
            listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)
        )
        val currentGame = mc.currentGame
        checkNotNull(currentGame)
        gameService.saveGame()
        assertDoesNotThrow { gameService.loadGame() }
    }

    /**
     * Test dir path
     */
    @Test
    fun `test dir path`() {
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)

        gameService.startNewGame(
            listOf(
                Triple("Anas",0,PotColor.PURPLE),
                Triple("Iyed",1,PotColor.PURPLE)
            ),
            5,
            listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)
        )
        val currentGame = mc.currentGame
        checkNotNull(currentGame)

        //dirPath
        assertDoesNotThrow { gameService.saveGame() }

        gameService.dirPath.delete()

        assertDoesNotThrow { gameService.saveGame() }

        playerActionService.meditate(currentGame.currentState.openCards[0])
        playerActionService.endTurn()

    }
}