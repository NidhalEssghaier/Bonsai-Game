package service

import entity.PotColor
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLoadGameTest {


    @Test
    fun `test save game with no game`() {
        val mc = RootService()
        val gameService = GameService(mc)

        //check saving a game
        var exception = assertThrows<IllegalStateException> {gameService.saveGame() }
        assertEquals("No game has been started yet.",exception.message)



    }
    @Test
    fun `test load game with no saved file`() {
        val mc = RootService()
        val gameService = GameService(mc)

        //check saving a game
        val file = File("data/save.json")
        if (file.exists()) {
            file.delete()
        }
        var exception = assertThrows<IllegalStateException> {gameService.loadGame() }
        assertEquals("Save file doesn't exist.",exception.message)
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, mutableListOf())
        val currentGame = mc.currentGame
        checkNotNull(currentGame)
        gameService.saveGame()
        assertDoesNotThrow { gameService.loadGame() }
    }

    @Test
    fun `test dir path`() {
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)

        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, mutableListOf())
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