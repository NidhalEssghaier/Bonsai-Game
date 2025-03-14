package gui

import entity.TileType
import entity.*
import service.AbstractRefreshingService

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the view classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * UI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 *
 */
interface Refreshable {

    /**
     * perform refreshes that are necessary after a new game started
     */
    fun refreshAfterStartNewGame() {}
    fun refreshAfterDrawCard(card: ZenCard){}
    fun refreshTogetUserTileChoice(): TileType

    /**
     * perform refreshes after a player claims a goal card
     * @param goalCard the claimed [GoalCard]
     */
    fun refreshAfterClaimGoal(goalCard: GoalCard) {}

    /**
     * perform refreshes that are necessary after undo / redo
     */
    fun refreshAfterUndoRedo() {}

    /**
     * Refresh affected GUI Elements after a turn has ended.
     */
    fun refreshAfterEndTurn() {}

    /**
     * Select a tile to discard.
     */
    fun refreshAfterDiscardTile() {}

    /**
     * perform refreshes that are necessary when a player choose a tile between
     */
    fun refreshToPromptTileChoice() {}

    /**
     * perform refreshes that are necessary after a [HelperCard] has been drawn
     *  this refresh is called in [Meditate] when a [HelperCard] has been drawn
     * in gui : the player should  choose wich tile he wants to play and the position in wich the tile
     * should be placed , these parameters need to be passed to the cultivate Method and the cultivate method needs to
     * be called
     */

    fun refreshAfterDrawHelperCard(helperCard: HelperCard) {}

    /**
     * perform refreshes that are necessary after undo / redo
     */
    fun refreshAfterRemoveTile(){}


}