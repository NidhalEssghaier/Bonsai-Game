package gui

import entity.CardSuit
import entity.CardValue

/**
 * The full raster image containing the suits as rows (plus one special row for blank/back)
 * and values as columns (starting with the ace). As the ordering in the image is not the same
 * as the order in which the suits are declared in [CardSuit], mappings via [row] and [column]
 * are required.
 */
private const val CARDS_FILE = "card_deck.png"

private const val IMG_HEIGHT = 200
private const val IMG_WIDTH = 130

/**
 * As the [CARDS_FILE] does not have the same ordering of suits
 * as they are in [CardSuit], this extension property provides
 * a corresponding mapping to be used when addressing the row.
 *
 */
private val CardSuit.row get() = when (this) {
    CardSuit.CLUBS -> 0
    CardSuit.DIAMONDS -> 1
    CardSuit.HEARTS -> 2
    CardSuit.SPADES -> 3
}


/**
 * As the [CARDS_FILE] does not have the same ordering of values
 * as they are in [CardValue], this extension property provides
 * a corresponding mapping to be used when addressing the column.
 */
private val CardValue.column get() = when (this) {
    CardValue.ACE -> 0
    CardValue.TWO -> 1
    CardValue.THREE -> 2
    CardValue.FOUR -> 3
    CardValue.FIVE -> 4
    CardValue.SIX -> 5
    CardValue.SEVEN -> 6
    CardValue.EIGHT -> 7
    CardValue.NINE -> 8
    CardValue.TEN -> 9
    CardValue.JACK -> 10
    CardValue.QUEEN -> 11
    CardValue.KING -> 12
}
