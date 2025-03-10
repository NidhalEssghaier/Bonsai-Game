package entity

/**
 * Entity to represent the card type "Master"
 *
 * @property tiles The tiles the player is allowed to take with this card
 */
class MasterCard(val tiles: List<TileType>) : ZenCard
