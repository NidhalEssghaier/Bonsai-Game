import tools.aqua.bgw.util.BidirectionalMap

/**
 * Copy a list
 * @return a copy of the original list
 */
fun <T> List<T>.copy(): List<T> {
    return this.map { it }
}

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