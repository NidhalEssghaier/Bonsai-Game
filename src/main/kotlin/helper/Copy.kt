package helper

import entity.BonsaiTile
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Stack

/**
 * Copy a BidirectionalMap
 * @return a copy of the original BidirectionalMap
 */
fun <T: Any, R: Any> BidirectionalMap<T, R>.copy(): BidirectionalMap<T, R> {
    val copy = BidirectionalMap<T, R>()
    val keys = this.keysForward
    for (key in keys) {
        copy.put(key, this.forward(key))
    }
    return copy
}

/**
 * Create a copy of a BidirectionalMap that contains BonsaiTile as value
 * @return a copy of the original BidirectionalMap with deep copied BonsaiTile as value
 */
fun <T: Any> BidirectionalMap<T, BonsaiTile>.copy(): BidirectionalMap<T, BonsaiTile> {
    val copy = BidirectionalMap<T, BonsaiTile>()
    val keys = this.keysForward
    for (key in keys) {
        copy.put(key, this.forward(key).copy())
    }
    return copy
}

/**
 * Copy a HexagonGrid
 * @return a copy of the original HexagonGrid
 */
fun <T : HexagonView> HexagonGrid<T>.copy(): HexagonGrid<T> {
    val copy = HexagonGrid<T>()
    val map = this.getCoordinateMap()
    for (coordinate in map.keys) {
        val value = map[coordinate]
        if (value != null) {
            copy[coordinate.first, coordinate.second] = value
        }
    }
    return copy
}

/**
 * Copy a Stack
 * @return a copy of the original Stack
 */
fun <T> Stack<T>.copy(): Stack<T> {
    return Stack(this.peekAll().asReversed())
}