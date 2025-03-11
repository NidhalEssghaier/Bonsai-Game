/**
 * Copy a list
 * @return a copy of the original list
 */
fun <T> List<T>.copy(): List<T> {
    return this.map { it }
}