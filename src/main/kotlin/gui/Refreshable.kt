package gui

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
     *  this refresh is called in [meditate] when a [HelperCard] has been drawn
     * in gui : the player should  choose wich tile he wants to play and the position in wich the tile
     * should be placed , these parameters need to be passed to the cultivate Method and the cultivate method needs to
     * be called
     */
    fun refreshAfterDrawHelperCard(helperCard: HelperCard) {}

    /**
     * perform refreshes that are necessary after undo / redo
     */
    fun refreshAfterRemoveTile() {}



    /**
     * refreshes the Game Scene when the places a tile
     */
    fun refreshAfterPlaceTile(placedTile: BonsaiTile) {}

    /**
     * * refreshes the game scene once the player achieves the requirement of at least one GoalCard
     * player should see wich Goals have been achieved
     * in gui : the player should then decide to claim or renounce of every achieved Goal
     * then the Method  [decideGoalClaim(goalCard: GoalCard,claim: Boolean)] needs to be called for every decision
     */
    fun refreshAfterReachGoals(reachedGoals:List<GoalCard>){}

    fun refreshAfterEndGame(scoreList: List<Pair<Player,Int>>) {}

}


