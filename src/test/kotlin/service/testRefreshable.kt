package service

import gui.Refreshable

/**
 * [Refreshable] implementation that refreshes nothing, but remembers
 * if a refresh method has been called (since last [reset])
 */
class TestRefreshable : Refreshable {

    var refreshAfterStartTileRemoved: Boolean = false
        private set



    /**
     * resets all *Called properties to false
     */
    fun reset() {
        refreshAfterStartTileRemoved = false
    }

    override fun refreshAfterRemoveTile() {
        refreshAfterStartTileRemoved = true
    }


}
