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

    /**
     * Add a tile that was removed from the bonsai to the message.
     *
     * @param coordinates The position the tile was removed from.
     */
    fun addRemovedTile(coordinates: Pair<Int, Int>) {
        if (removedTilesAxialCoordinates == null) {
            removedTilesAxialCoordinates = mutableListOf()
            removedTilesAxialCoordinates?.add(coordinates)
        } else {
            removedTilesAxialCoordinates?.add(coordinates)
        }
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
            playedTiles?.add(Pair(converter.fromTileType(tileType), coordinates))
        } else {
            playedTiles?.add(Pair(converter.fromTileType(tileType), coordinates))
        }
    }

    /**
     * Add a goal card that was claimed to the message.
     *
     * @param goalCard The goal card that was claimed.
     */
    fun addClaimedGoal(goalCard: GoalCard) {
        if (claimedGoals == null) {
            claimedGoals = mutableListOf()
            claimedGoals?.add(converter.fromGoal(goalCard))
        } else {
            claimedGoals?.add(converter.fromGoal(goalCard))
        }
    }

    /**
     * Add a goal card that was renounced to the message.
     *
     * @param goalCard The goal card that was renounced.
     */
    fun addRenouncedGoal(goalCard: GoalCard) {
        if (renouncedGoals == null) {
            renouncedGoals = mutableListOf()
            renouncedGoals?.add(converter.fromGoal(goalCard))
        } else {
            renouncedGoals?.add(converter.fromGoal(goalCard))
        }
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
            drawnTiles?.add(converter.fromTileType(tileType))
        } else {
            drawnTiles?.add(converter.fromTileType(tileType))
        }
    }

    /**
     * Add the [TileType] of the discarded tile to the message.
     *
     * @param tileType The [TileType] of the discarded tile.
     */
    fun addDiscardedTile(tileType: TileType) {
        if (discardedTiles == null) {
            discardedTiles = mutableListOf()
            discardedTiles?.add(converter.fromTileType(tileType))
        } else {
            discardedTiles?.add(converter.fromTileType(tileType))
        }
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

        if (chosenCardPosition == null) {
            val message = CultivateMessage(
                removedTilesAxialCoordinates!!,
                playedTiles!!,
                claimedGoals!!,
                renouncedGoals!!
            )
            nullAllVars()
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

            val message = MeditateMessage(
                removedTilesAxialCoordinates!!,
                chosenCardPosition!!,
                playedTiles!!,
                drawnTiles!!,
                claimedGoals!!,
                renouncedGoals!!,
                discardedTiles!!
            )
            nullAllVars()
            return Pair(null, message)
        }
    }

    private fun nullAllVars() {
        removedTilesAxialCoordinates = null
        chosenCardPosition = null
        playedTiles = null
        drawnTiles = null
        claimedGoals = null
        renouncedGoals = null
        discardedTiles = null
    }

}
