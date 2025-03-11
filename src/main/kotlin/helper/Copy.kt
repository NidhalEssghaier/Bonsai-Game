package helper

import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.util.BidirectionalMap

/**
 * Copy a BidirectionalMap
 * @return a copy of the original BidirectionalMap
 */
fun <T : Any, R: Any> BidirectionalMap<T, R>.copy(): BidirectionalMap<T, R> {
    val copy = BidirectionalMap<T, R>()
    val keys = this.keysForward
    for (key in keys) {
        copy.put(key, this.forward(key))
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