package service

import entity.GoalColor
import entity.PotColor
import helper.peek
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UndoRedoTest {

    @Test
    fun `test undo`(){
        val mc = RootService()
        val testRefreshable =TestRefreshable()
        val gameService = mc.gameService
        val playerActionService = mc.playerActionService
        playerActionService.addRefreshable(testRefreshable)

        //check undoing with no gq,e
        val exception = assertThrows<IllegalStateException> {playerActionService.undo()}
        assertEquals("there is no active game",exception.message)

        gameService.startNewGame(listOf(Triple("Anas",0, PotColor.PURPLE),
            Triple("Iyed",1,PotColor.PURPLE)),5, listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE))

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)

        //undo stach hols initial game state, redo stack must be empty
        assertEquals(1, game.undoStack.size)
        assertTrue(game.redoStack.isEmpty())

        //currentGame stack should be added to undoStqck  after medidate should
        playerActionService.meditate(game.currentState.openCards[0])
        assertTrue(game.undoStack.isNotEmpty())
        assertTrue(game.redoStack.isEmpty())

        //assert not refreshed before undo
        assertFalse { testRefreshable.refreshAfterUndoRedoCalled }

        //assert removed game state from undoStack and added it to redoStack
        assertDoesNotThrow { playerActionService.undo() }
        assertTrue(game.undoStack.isEmpty())
        assertTrue(game.redoStack.isNotEmpty())

        //assert refreshed after undo
        assertTrue { testRefreshable.refreshAfterUndoRedoCalled }
    }

    @Test
    fun `test redo`(){
        val mc = RootService()
        val testRefreshable =TestRefreshable()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)
        playerActionService.addRefreshable(testRefreshable)

        //check undoing with no game
        val exception = assertThrows<IllegalStateException> {playerActionService.redo()}
        assertEquals("there is no active game",exception.message)

        gameService.startNewGame(listOf(Triple("Anas",0, PotColor.PURPLE),
            Triple("Iyed",1,PotColor.PURPLE)),5, listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE))

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)

        //undo stach hols initial game state, redo stack must be empty
        assertEquals(1, game.undoStack.size)
        assertTrue(game.redoStack.isEmpty())

        //currentGame stack should be added to undoStack  after medidate should
        playerActionService.meditate(game.currentState.openCards[0])

        //assert cant redo without undo
        assertThrows<NoSuchElementException> { playerActionService.redo() }

        assertDoesNotThrow { playerActionService.undo() }
        testRefreshable.reset()

        //assert not refreshed before redo
        assertFalse { testRefreshable.refreshAfterUndoRedoCalled }

        //assert redo stack is popped and added game state to undp
        assertDoesNotThrow { playerActionService.redo() }
        assertTrue(game.redoStack.isEmpty())
        assertTrue(game.undoStack.isNotEmpty())

        //assert refreshed after redo
        assertTrue { testRefreshable.refreshAfterUndoRedoCalled }
    }

    @Test
    fun `test undo after meditate`() {
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)

        gameService.startNewGame(
            listOf(
                Triple("Anas", 0, PotColor.PURPLE),
                Triple("Iyed", 1, PotColor.PURPLE)
            ),
            5,
            listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)
        )

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)

        //game
        val gameState = game.currentState
        playerActionService.meditate(game.currentState.openCards[0])
        playerActionService.endTurn()
        val gameStateBeforeUndo = game.undoStack.peek()

        //assert open cards are the same
        for (i in gameStateBeforeUndo.openCards.indices) {
            assertEquals(gameStateBeforeUndo.openCards[i], gameState.openCards[i])
        }
        //ass
    }


}
