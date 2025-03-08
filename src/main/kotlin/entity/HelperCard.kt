package entity

/**
 * Entity to represent the card type "Helper"
 *
 * @property tiles Saves, which tiles can be used for the action of the card.
 */
class HelperCard(
    val tiles: Map<TileType, Int>,
) : ZenCard
