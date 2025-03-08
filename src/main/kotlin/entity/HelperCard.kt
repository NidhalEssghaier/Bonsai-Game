package entity

import java.util.Dictionary

/**
 * Entity to represent the card type "Helper"
 *
 * @property tiles Saves, which tiles can be used for the action of the card.
 */
class HelperCard(val tiles: Dictionary<TileType, Int>) : ZenCard
