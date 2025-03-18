package service

import entity.PotColor
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class UndoRedoTest {

    @Test
    fun `test undo`(){
    val mc = RootService()
    val gameService = GameService(mc)
    val playerActionService = PlayerActionService(mc)
    gameService.startNewGame(listOf(Triple("Anas",0, PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, mutableListOf())

    //check game not null
    val game = mc.currentGame
    checkNotNull(game)

    //undo and redo stack must be empty
    assertTrue(game.undoStack.isEmpty())
    assertTrue(game.redoStack.isEmpty())
    playerActionService.meditate(game.currentState.openCards[0])
    playerActionService.endTurn()
    }
}