package service

import entity.*
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
        private set

    var refreshAfterDrawCardCalled: Boolean = false
        private set

    var refreshAfterClaimGoalCalled: Boolean = false
        private set

    var refreshAfterUndoRedoCalled: Boolean = false
        private set

    var refreshAfterEndTurnCalled: Boolean = false
        private set

    var refreshAfterDiscardTileCalled: Boolean = false
        private set

    var refreshToPromptTileChoiceCalled: Boolean = false
        private set

    var refreshAfterDrawHelperCardCalled: Boolean = false
        private set

    var refreshAfterRemoveTileCalled: Boolean = false
        private set

    var refreshAfterPlaceTileCalled: Boolean = false
        private set

    var refreshAfterReachGoalsCalled: Boolean = false
        private set

    /**
     * resets all *Called properties to false
     */
    fun reset() {
        refreshAfterStartTileRemoved = false
        refreshAfterStartNewGameCalled = false
        refreshAfterEndGameCalled = false
        refreshAfterDrawCardCalled = false
        refreshAfterClaimGoalCalled = false
        refreshAfterUndoRedoCalled = false
        refreshAfterEndTurnCalled = false
        refreshAfterDiscardTileCalled = false
        refreshToPromptTileChoiceCalled = false
        refreshAfterDrawHelperCardCalled = false
        refreshAfterRemoveTileCalled = false
        refreshAfterPlaceTileCalled = false
        refreshAfterReachGoalsCalled = false
    }

    override fun refreshAfterStartNewGame() {
        refreshAfterStartNewGameCalled = true
    }

    override fun refreshAfterDrawCard(
        card: ZenCard,
        drawnCardIndex: Int,
        chooseTilesByBoard: Boolean,
        chooseTilesByCard: Boolean,
    )  {
        refreshAfterDrawCardCalled = true
    }

//    override fun  refreshAfterReachGoals (reachedGoals: List<GoalCard>) {
//        refreshAfterClaimGoalCalled = true
//    }

    override fun refreshAfterUndoRedo() {
        refreshAfterUndoRedoCalled = true
    }

    override fun refreshAfterEndTurn() {
        refreshAfterEndTurnCalled = true
    }

    override fun refreshAfterDiscardTile(
        tilesToDiscard: Int,
        removedTile: BonsaiTile?,
    ) {
        refreshAfterDiscardTileCalled = true
    }

    override fun  refreshToPromptTileChoice(
        chooseByBoard: Boolean,
        chooseByCard: Boolean,
    ) {
        refreshToPromptTileChoiceCalled = true
    }

    override fun refreshAfterDrawHelperCard(helperCard: HelperCard) {
        refreshAfterDrawHelperCardCalled = true
    }

    override fun refreshAfterRemoveTile(tile: BonsaiTile) {
        refreshAfterRemoveTileCalled = true
    }

    override fun refreshAfterPlaceTile(placedTile: BonsaiTile) {
        refreshAfterPlaceTileCalled = true
    }

    override fun refreshAfterReachGoals(reachedGoals: List<GoalCard>) {
        refreshAfterReachGoalsCalled = true
    }

    override fun refreshAfterEndGame(scoreList: Map<Player, MutableList<Int>>) {
        refreshAfterEndGameCalled = true
    }


}
