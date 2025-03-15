package service

import entity.Player
import gui.Refreshable

/**
 * [Refreshable] implementation that refreshes nothing, but remembers
 * if a refresh method has been called (since last [reset])
 */
class TestRefreshable : Refreshable {

    var refreshAfterStartTileRemoved: Boolean = false
        private set

    var refreshAfterStartNewGameCalled: Boolean = false
        private set

    var refreshAfterEndGameCalled: Boolean = false



    /**
     * resets all *Called properties to false
     */
    fun reset() {
        refreshAfterStartTileRemoved = false
        refreshAfterStartNewGameCalled = false
        refreshAfterEndGameCalled = false
    }

    override fun refreshAfterRemoveTile() {
        refreshAfterStartTileRemoved = true
    }

    override fun refreshAfterStartNewGame() {
        refreshAfterStartNewGameCalled = true
    }

    override fun refreshAfterEndGame(scoreList: List<Pair<Player, Int>>) {
        refreshAfterEndGameCalled = true
    }


}
