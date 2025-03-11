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
fun <T: Any> BidirectionalMap<T, BonsaiTile>.deepBonsaiTileCopy(): BidirectionalMap<T, BonsaiTile> {
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
    val copy = HexagonGrid<T>(
        this.posX,
        this.posY,
        this.width,
        this.height,
        this.visual,
        this.coordinateSystem,
        this.orientation
    ).apply {
        dropAcceptor = this@copy.dropAcceptor
        isDisabled = this@copy.isDisabled
        isDraggable = this@copy.isDraggable
        isFocusable = this@copy.isFocusable
        isVisible = this@copy.isVisible
        opacity = this@copy.opacity
        rotation = this@copy.rotation
        scale = this@copy.scale
        scaleX = this@copy.scaleX
        scaleY = this@copy.scaleY
        zIndex = this@copy.zIndex
    }
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