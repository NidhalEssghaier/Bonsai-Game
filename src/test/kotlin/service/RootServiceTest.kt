package service

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Test cases for [RootService] */
class RootServiceTest {
    /** Tests if [RootService.addRefreshable] properly forwards the added [Refreshable] to its service classes. */
    @Test
    fun testRootServiceSingleRefreshable() {
        val rootService = RootService()
        val testRefreshable = TestRefreshable()

        rootService.addRefreshable(testRefreshable)

        // Test if testRefreshable was successfully added to GameService
        assertFalse(testRefreshable.refreshAfterStartNewGameCalled)
        rootService.gameService.onAllRefreshables { refreshAfterStartNewGame() }
        assertTrue(testRefreshable.refreshAfterStartNewGameCalled)
        testRefreshable.reset()

        // Test if testRefreshable was successfully added to PlayerActionService
        assertFalse(testRefreshable.refreshAfterStartNewGameCalled)
        rootService.playerActionService.onAllRefreshables { refreshAfterStartNewGame() }
        assertTrue(testRefreshable.refreshAfterStartNewGameCalled)
        testRefreshable.reset()
    }

    /** Tests if [RootService.addRefreshable] properly forwards the added [Refreshable] to its service classes. */
    @Test
    fun testRootServiceMultiRefreshable() {
        val rootService = RootService()
        val testRefreshable1 = TestRefreshable()
        val testRefreshable2 = TestRefreshable()

        rootService.addRefreshables(testRefreshable1, testRefreshable2)

        // Test if testRefreshables were successfully added to GameService
        assertFalse(testRefreshable1.refreshAfterStartNewGameCalled)
        assertFalse(testRefreshable2.refreshAfterStartNewGameCalled)
        rootService.gameService.onAllRefreshables { refreshAfterStartNewGame() }
        assertTrue(testRefreshable1.refreshAfterStartNewGameCalled)
        assertTrue(testRefreshable2.refreshAfterStartNewGameCalled)
        testRefreshable1.reset()
        testRefreshable2.reset()

        // Test if testRefreshable was successfully added to PlayerActionService
        assertFalse(testRefreshable1.refreshAfterStartNewGameCalled)
        assertFalse(testRefreshable2.refreshAfterStartNewGameCalled)
        rootService.playerActionService.onAllRefreshables { refreshAfterStartNewGame() }
        assertTrue(testRefreshable1.refreshAfterStartNewGameCalled)
        assertTrue(testRefreshable2.refreshAfterStartNewGameCalled)
        testRefreshable1.reset()
        testRefreshable2.reset()
    }
}
