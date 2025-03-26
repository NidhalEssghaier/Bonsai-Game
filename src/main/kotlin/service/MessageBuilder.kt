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
    private val removedTilesAxialCoordinates: MutableList<Pair<Int, Int>> = mutableListOf()
    private var chosenCardPosition: Int = -1
    private val playedTiles: MutableList<Pair<TileTypeMessage, Pair<Int, Int>>> = mutableListOf()
    private val drawnTiles: MutableList<TileTypeMessage> = mutableListOf()
    private val claimedGoals: MutableList<Pair<GoalTileTypeMessage, Int>> = mutableListOf()
    private val renouncedGoals: MutableList<Pair<GoalTileTypeMessage, Int>> = mutableListOf()
    private val discardedTiles: MutableList<TileTypeMessage> = mutableListOf()

    /** Used to convert between entity classes and ntf classes. */
    private val converter: MessageConverter = MessageConverter()

    /**
     * Add a tile that was removed from the bonsai to the message.
     *
     * @param coordinates The position the tile was removed from.
     */
    fun addRemovedTile(coordinates: Pair<Int, Int>) {
        removedTilesAxialCoordinates.add(coordinates)
    }

    /**
     * Add a tile that was placed into the bonsai to the message.
     *
     * @param tileType The type the placed tile has.
     * @param coordinates The position the tile was placed.
     */
    fun addPlacedTile(tileType: TileType, coordinates: Pair<Int, Int>) {
        playedTiles.add(Pair(converter.fromTileType(tileType), coordinates))
    }

    /**
     * Add a goal card that was claimed to the message.
     *
     * @param goalCard The goal card that was claimed.
     */
    fun addClaimedGoal(goalCard: GoalCard) {
        claimedGoals.add(converter.fromGoal(goalCard))
    }

    /**
     * Add a goal card that was renounced to the message.
     *
     * @param goalCard The goal card that was renounced.
     */
    fun addRenouncedGoal(goalCard: GoalCard) {
        renouncedGoals.add(converter.fromGoal(goalCard))
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
        drawnTiles.add(converter.fromTileType(tileType))
    }

    /**
     * Add the [TileType] of the discarded tile to the message.
     *
     * @param tileType The [TileType] of the discarded tile.
     */
    fun addDiscardedTile(tileType: TileType) {
        discardedTiles.add(converter.fromTileType(tileType))
    }

    /**
     * Construct either a [CultivateMessage] or [MeditateMessage] from class variables.
     *
     * @return The Method returns a Pair of messages. The first message in the
     *         pair is a [CultivateMessage] the second a [MeditateMessage].
     *         One of the two will be null, the other the constructed message.
     */
    fun build(): Pair<CultivateMessage?, MeditateMessage?> {
        if (chosenCardPosition == -1) {
            return Pair(
                CultivateMessage(
                    removedTilesAxialCoordinates,
                    playedTiles,
                    claimedGoals,
                    renouncedGoals
                ),
                null
            )
        } else {
            if (chosenCardPosition != 0) {
                check(drawnTiles.isNotEmpty()) { "at least one tile should have been drawn" }
            }

            return Pair(
                null,
                MeditateMessage(
                    removedTilesAxialCoordinates,
                    chosenCardPosition,
                    playedTiles,
                    drawnTiles,
                    claimedGoals,
                    renouncedGoals,
                    discardedTiles
                )
            )
        }
    }

    /**
     * Resets all parameters of the message builder.
     */
    fun reset() {
        removedTilesAxialCoordinates.clear()
        chosenCardPosition = -1
        playedTiles.clear()
        drawnTiles.clear()
        claimedGoals.clear()
        renouncedGoals.clear()
        discardedTiles.clear()
    }

}
