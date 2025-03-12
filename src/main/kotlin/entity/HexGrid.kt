package entity

/**
 * Entity to represent the hex grid of the bonsai bowl
 * @property size size of the grid
 * @property actualSize actual size of the internal array (Only for internal usage)
 * @property grid Store the [BonsaiTile] with coordinates (Only for internal usage)
 * @property map Mapping between [BonsaiTile] and its coordinates (Only for internal usage)
 * @property tilesList Returns the list of all [BonsaiTile] in the grid
 * @property axialRange Range of axial coordinates
 * @property axial2Raw Function to convert axial coordinates to raw internal coordinates (Only for internal usage)
 * @property raw2Axial Function to convert raw internal coordinates to axial coordinates (Only for internal usage)
 * @constructor Create a [HexGrid] with coordinates ranging from -[size] to [size]
 */
class HexGrid private constructor(
    val size: Int,
    private val actualSize: Int,
    private val grid: Array<Array<BonsaiTile?>>,
    private val map: MutableMap<BonsaiTile, Pair<Int, Int>>
) {
    val tilesList = { map.keys.toList() }
    private val axialRange = -size..size

    private val axial2Raw: (Int) -> (Int) = { it + size }
    private val raw2Axial: (Int) -> (Int) = { it - size }

    /**
     * Secondary public constructor to create a [HexGrid] with coordinates ranging from -[size] to [size]
     */
    constructor(size: Int): this(
        size,
        2*size + 1,
        Array(2*size + 1) { arrayOfNulls<BonsaiTile?>(2*size + 1) },
        mutableMapOf()
    )

    /**
     * Get the [BonsaiTile] at the given axial coordinates
     * @param q q coordinate
     * @param r r coordinate
     * @return [BonsaiTile] at the given axial coordinates
     * @throws IllegalArgumentException if the coordinate is out of bounds
     * @throws IllegalStateException if there is no [BonsaiTile] at the given coordinates
     */
    operator fun get(q: Int, r: Int): BonsaiTile {
        require(q in axialRange && r in axialRange) {"Coordinate out of bounds"}
        val tile = grid[axial2Raw(q)][axial2Raw(r)]
        checkNotNull(tile) {"No BonsaiTile at this coordinate"}
        return tile
    }

    /**
     * Set the [BonsaiTile] at the given axial coordinates
     * @param q q coordinate
     * @param r r coordinate
     * @param tile [BonsaiTile] to set
     * @throws IllegalArgumentException if the coordinate is out of bounds
     */
    operator fun set(q: Int, r: Int, tile: BonsaiTile) {
        require(q in axialRange && r in axialRange) {"Coordinate out of bounds"}
        val nq = axial2Raw(q)
        val nr = axial2Raw(r)
        grid[nq][nr] = tile
        map[tile] = Pair(nq, nr)
    }

    /**
     * Get the axial coordinate of the given [BonsaiTile]
     * @param tile [BonsaiTile]
     * @return [Pair] of axial coordinates
     * @throws IllegalStateException if the [BonsaiTile] isn't in the grid
     */
    fun getCoordinate(tile: BonsaiTile): Pair<Int, Int> {
        val coordinate = map[tile]
        checkNotNull(coordinate) {"This BonsaiTile isn't in the grid"}
        return Pair(raw2Axial(coordinate.first), raw2Axial(coordinate.second))
    }

    /**
     * Remove the given [BonsaiTile] from the grid
     * @param tile [BonsaiTile] to remove
     * @return `true` if the [BonsaiTile] is removed successfully, `false` otherwise
     */
    fun remove(tile: BonsaiTile): Boolean {
        val coordinate = map[tile] ?: return false
        map.remove(tile)
        grid[coordinate.first][coordinate.second] = null
        return true
    }

    /**
     * Deep copy the [HexGrid]
     * @return [HexGrid] a deep copy of the original [HexGrid]
     */
    fun copy(): HexGrid {
        val gridCopy = Array(actualSize) { arrayOfNulls<BonsaiTile?>(actualSize) }
        val mapOfNewTiles = mutableMapOf<BonsaiTile, BonsaiTile>()

        val mapCopy = map.mapKeys {
            val newTile = it.key.copy()
            gridCopy[it.value.first][it.value.second] = newTile
            mapOfNewTiles[it.key] = newTile
            newTile
        }

        // Fix neighbors references
        for (tile in mapCopy.keys){
            val newNeighborsList = tile.neighbors.map {
                val newNeighbor = mapOfNewTiles[it]
                checkNotNull(newNeighbor) {"Internal Error! No mapping information for this BonsaiTile"}
                newNeighbor
            }

            tile.neighbors.clear()
            tile.neighbors.addAll(newNeighborsList)
        }

        return HexGrid(size, actualSize, gridCopy, mapCopy.toMutableMap())
    }
}