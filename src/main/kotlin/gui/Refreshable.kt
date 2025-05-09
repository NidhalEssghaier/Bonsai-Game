package gui

import entity.*
import service.AbstractRefreshingService
import service.ConnectionState
import service.PlayerActionService

/**
 * This interface provides a mechanism for the service layer classes to communicate (usually to the view classes) that
 * certain changes have been made to the entity layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing UI classes only need to react to
 * events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {
    /** perform refreshes that are necessary after a new game started */
    fun refreshAfterStartNewGame() {}

    /**
     * Performs necessary refresh operations after a card has been drawn.
     *
     * @param card The card that was drawn.
     */
    fun refreshAfterDrawCard(
        card: ZenCard,
        drawnCardIndex: Int,
        chooseTilesByBoard: Boolean,
        chooseTilesByCard: Boolean,
    ) {
    }

    /** perform refreshes after a player claims a goal card */
    fun refreshAfterDecideGoal() {}

    /** perform refreshes that are necessary after undo / redo */
    fun refreshAfterUndoRedo() {}

    /** Refresh relevant GUI elements after the network connection status changes. */
    fun refreshConnectionState(
        newState: ConnectionState,
        string: String?,
        list: List<String>?,
    ) {
    }

    /** Refresh affected GUI Elements after a turn has ended. */
    fun refreshAfterEndTurn() {}

    /** Select a tile to discard. */
    fun refreshAfterDiscardTile(
        tilesToDiscard: Int,
        removedTile: BonsaiTile?,
    ) {
    }

    /**
     * Performs refresh operations necessary when a player is prompted to choose a tile while meditating, if the drawn
     * card is in the first position.
     *
     * This refreshable function must call `applyTileChoice(cardStack: Int, choice: TileType)` from the game service
     * to apply the necessary logical updates based on the player's choice. It ensures that the game state and UI are
     * updated accordingly.
     */
    fun refreshToPromptTileChoice(
        chooseByBoard: Boolean,
        chooseByCard: Boolean,
    ) {
    }

    /**
     * perform refreshes that are necessary after a [HelperCard] has been drawn this refresh is called in
     * [PlayerActionService.meditate] when a [HelperCard] has been drawn in gui : the player should choose wich tile
     * he wants to play and the position in wich the tile should be placed , these parameters need to be passed to the
     * cultivate Method and the cultivate method needs to be called
     */
    fun refreshAfterDrawHelperCard(helperCard: HelperCard) {}

    /** perform refreshes that are necessary after undo / redo */
    fun refreshAfterRemoveTile(tile: BonsaiTile) {}

    /** refresh to show a new scene displaying the scores of the players */
    fun refreshAfterEndGame(scoreList: Map<Player, MutableList<Int>>) {}

    /** refreshes the Game Scene when the places a tile */
    fun refreshAfterPlaceTile(placedTile: BonsaiTile) {}

    /**
     * * refreshes the game scene once the player achieves the requirement of at least one GoalCard player should see
     *   wich Goals have been achieved in gui : the player should then decide to claim or renounce of every achieved
     *   Goal then the Method [decideGoalClaim(goalCard: GoalCard,claim: Boolean)] needs to be called for every decision
     */
    fun refreshAfterReachGoals(reachedGoals: List<GoalCard>) {}

    /**
     * refreshes the game elements that get modified by choosing a tile after drawing a card that allows to choose a
     * tile by position or card action
     */
    fun refreshAfterChooseTile() {}
}
