package service

import messages.*
import entity.GoalCard
import entity.TileType

/**
 * Class used to construct player action network messages.
 */
class MessageBuilder {

    /**
     * possible parameters for [CultivateMessage] and [MeditateMessage]
     */
    private var removedTilesAxialCoordinates: MutableList<Pair<Int, Int>>? = null
    private var chosenCardPosition: Int? = null
    private var playedTiles: MutableList<Pair<TileTypeMessage, Pair<Int, Int>>>? = null
    private var drawnTiles: MutableList<TileTypeMessage>? = null
    private var claimedGoals: MutableList<Pair<GoalTileTypeMessage, Int>>? = null
    private var renouncedGoals: MutableList<Pair<GoalTileTypeMessage, Int>>? = null
    private var discardedTiles: MutableList<TileTypeMessage>? = null

    /** Used to convert between entity classes and ntf classes. */
    private val converter: MessageConverter = MessageConverter()

    //Error message
    private val errorMessage = "Internal error: it shouldn't be null"

    /**
     * Add a tile that was removed from the bonsai to the message.
     *
     * @param coordinates The position the tile was removed from.
     */
    fun addRemovedTile(coordinates: Pair<Int, Int>) {
        if (removedTilesAxialCoordinates == null) {
            removedTilesAxialCoordinates = mutableListOf()
        }

        val checkedRemovedTilesAxialCoordinates = removedTilesAxialCoordinates
        checkNotNull(checkedRemovedTilesAxialCoordinates) { errorMessage }

        checkedRemovedTilesAxialCoordinates.add(coordinates)
    }

    /**
     * Add a tile that was placed into the bonsai to the message.
     *
     * @param tileType The type the placed tile has.
     * @param coordinates The position the tile was placed.
     */
    fun addPlacedTile(tileType: TileType, coordinates: Pair<Int, Int>) {
        if (playedTiles == null) {
            playedTiles = mutableListOf()
        }

        val checkedPlayedTiles = playedTiles
        checkNotNull(checkedPlayedTiles) {errorMessage}

        checkedPlayedTiles.add(Pair(converter.fromTileType(tileType), coordinates))
    }

    /**
     * Add a goal card that was claimed to the message.
     *
     * @param goalCard The goal card that was claimed.
     */
    fun addClaimedGoal(goalCard: GoalCard) {
        if (claimedGoals == null) {
            claimedGoals = mutableListOf()
        }

        val checkedClaimedGoals = claimedGoals
        checkNotNull(checkedClaimedGoals)

        checkedClaimedGoals.add(converter.fromGoal(goalCard))
    }

    /**
     * Add a goal card that was renounced to the message.
     *
     * @param goalCard The goal card that was renounced.
     */
    fun addRenouncedGoal(goalCard: GoalCard) {
        if (renouncedGoals == null) {
            renouncedGoals = mutableListOf()
        }

        val checkedRenouncedGoals = renouncedGoals
        checkNotNull(checkedRenouncedGoals)

        checkedRenouncedGoals.add(converter.fromGoal(goalCard))
    }

    /**
     * Set the index of the open card that was drawn.
     *
     * @param openCardIndex The index of the open card that was drawn.
     */
    fun setDrawnCard(openCardIndex: Int) {
        chosenCardPosition = openCardIndex
    }

    /**
     * Add the [TileType] of the drawn tile to the message.
     *
     * @param tileType The [TileType] of the drawn tile.
     */
    fun addDrawnTile(tileType: TileType) {
        if (drawnTiles == null) {
            drawnTiles = mutableListOf()
        }

        val checkedDrawnTiles = drawnTiles
        checkNotNull(checkedDrawnTiles)

        checkedDrawnTiles.add(converter.fromTileType(tileType))
    }

    /**
     * Add the [TileType] of the discarded tile to the message.
     *
     * @param tileType The [TileType] of the discarded tile.
     */
    fun addDiscardedTile(tileType: TileType) {
        if (discardedTiles == null) {
            discardedTiles = mutableListOf()
        }

        val checkedDiscardedTiles = discardedTiles
        checkNotNull(checkedDiscardedTiles)

        checkedDiscardedTiles.add(converter.fromTileType(tileType))
    }

    /**
     * Construct either a [CultivateMessage] or [MeditateMessage] from class variables.
     *
     * @return The Method returns a Pair of messages. The first message in the
     *         pair is a [CultivateMessage] the second a [MeditateMessage].
     *         One of the two will be null, the other the constructed message.
     */
    fun build(): Pair<CultivateMessage?, MeditateMessage?> {
        if (removedTilesAxialCoordinates == null) {
            removedTilesAxialCoordinates = mutableListOf()
        }
        if (playedTiles == null) {
            playedTiles = mutableListOf()
        }
        if (claimedGoals == null) {
            claimedGoals = mutableListOf()
        }
        if (renouncedGoals == null) {
            renouncedGoals = mutableListOf()
        }

        //Add checks to make kotlin compiler happy
        val checkedRemovedTilesAxialCoordinates = removedTilesAxialCoordinates
        val checkedPlayedTiles = playedTiles
        val checkedClaimedGoals = claimedGoals
        val checkedRenouncedGoals = renouncedGoals

        checkNotNull(checkedRemovedTilesAxialCoordinates) {errorMessage}
        checkNotNull(checkedPlayedTiles) {errorMessage}
        checkNotNull(checkedClaimedGoals) {errorMessage}
        checkNotNull(checkedRenouncedGoals) {errorMessage}

        if (chosenCardPosition == null) {
            val message = CultivateMessage(
                checkedRemovedTilesAxialCoordinates,
                checkedPlayedTiles,
                checkedClaimedGoals,
                checkedRenouncedGoals
            )
            reset()
            return Pair(message, null)
        } else {
            if (drawnTiles == null) {
                if (chosenCardPosition != 0) {
                    error("at least one tile should have been drawn")
                } else {
                    drawnTiles = mutableListOf()
                }
            }
            if (discardedTiles == null) {
                discardedTiles = mutableListOf()
            }

            //Add checks to make kotlin compiler happy
            val checkedDrawnTiles = drawnTiles
            val checkedChosenCardPosition = chosenCardPosition
            val checkedDiscardedTiles = discardedTiles
            checkNotNull(checkedChosenCardPosition) {errorMessage}
            checkNotNull(checkedDrawnTiles) {errorMessage}
            checkNotNull(checkedDiscardedTiles) {errorMessage}

            val message = MeditateMessage(
                checkedRemovedTilesAxialCoordinates,
                checkedChosenCardPosition,
                checkedPlayedTiles,
                checkedDrawnTiles,
                checkedClaimedGoals,
                checkedRenouncedGoals,
                checkedDiscardedTiles
            )
            reset()
            return Pair(null, message)
        }
    }

    fun reset() {
        removedTilesAxialCoordinates = null
        chosenCardPosition = null
        playedTiles = null
        drawnTiles = null
        claimedGoals = null
        renouncedGoals = null
        discardedTiles = null
    }
}
