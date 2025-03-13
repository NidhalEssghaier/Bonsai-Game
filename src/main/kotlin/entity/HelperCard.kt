package entity

/**
 * Entity to represent the card type "Helper"
 *
 * @property tiles Saves, which tiles can be used for the action of the card.
 * * @property hasPlacedChosenTile Indicates whether the player has placed a tile of their choice.
 *  * @property hasPlacedShownTile Indicates whether the player has placed the tile shown on the card.
 */
class HelperCard(val tiles: List<TileType>) : ZenCard {
    var hasPlacedChosenTile: Boolean = false
    var hasPlacedShownTile: Boolean = false
}
