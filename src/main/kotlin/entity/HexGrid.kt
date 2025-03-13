package entity

/**
 * Entity to represent the hex grid of the bonsai bowl
 * - Example:
 *   ```kotlin
 *   // Create a HexGrid with size 20 (coordinates ranging from -20 to 20)
 *   val grid = HexGrid(20)
 *
 *   // Place the BonsaiTile at coordinates (0, 0)
 *   grid[0, 0] = BonsaiTile(TileType.WOOD)
 *
 *   // Get the BonsaiTile at coordinates (0, 0)
 *   val tileAt00 = grid[0, 0]
 *
 *   // Get the axial coordinates of the BonsaiTile
 *   val coordinate = grid.getCoordinate(tileAt00)
 *
 *   // Remove the BonsaiTile from the grid
 *   grid.remove(tileAt00)
 *
 *   //Get the used tile list
 *   val tileList = grid.tilesList()
 *
 *   // Check if the grid is empty
 *   if (grid.isEmpty()) {// Do something}
 *
 *   // Check if the grid is NOT empty
 *   if (grid.isNotEmpty()) {// Do something}
 *
 *   // Check if the coordinates are empty
 *   if (grid.isEmpty(0, 0)) {// Do something}
 *
 *   // Check if the coordinates are NOT empty
 *   if (grid.isNotEmpty(0, 0)) {// Do something}
 *
 *   // Get the mapping of BonsaiTile to its axial coordinates
 *   val tileToCoordinateMap = grid.getTileToCoordinateMap()
 *
 *   // Get the mapping of axial coordinates to BonsaiTile
 *   val coordinateToTileMap = grid.getCoordinateToTileMap()
 *
 *   // Deep copy the HexGrid
 *   val gridCopy = grid.copy()
 *   ```
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
     * Companion object to store the exceptions
     */
    private companion object {
        private val invalidCoordinate = NoSuchElementException("No BonsaiTile at the given coordinates")
        private val invalidTile = NoSuchElementException("This BonsaiTile isn't in the grid")
    }

    /**
     * Secondary public constructor to create a [HexGrid] with coordinates ranging from -[size] to [size]
     * @param size size of the grid
     * @throws IllegalArgumentException if the size is less than 1
     */
    constructor(size: Int): this(
        size.also { require( it > 0) },
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
     * @throws NoSuchElementException if there is no [BonsaiTile] at the given coordinates
     */
    operator fun get(q: Int, r: Int): BonsaiTile {
        require(q in axialRange && r in axialRange) {"Coordinate out of bounds"}
        val tile = grid[axial2Raw(q)][axial2Raw(r)] ?: throw invalidCoordinate
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
     * Get the [BonsaiTile] at the given axial coordinates or `null` if the coordinates are empty
     * @param q q coordinate
     * @param r r coordinate
     * @return [BonsaiTile] at the given axial coordinates or `null` if the coordinates are empty
     */
    fun getOrNull(q: Int, r: Int): BonsaiTile? {
        return grid[axial2Raw(q)][axial2Raw(r)]
    }

    /**
     * Get the axial coordinate of the given [BonsaiTile]
     * @param tile [BonsaiTile]
     * @return [Pair] of axial coordinates
     * @throws NoSuchElementException if the [BonsaiTile] isn't in the grid
     */
    fun getCoordinate(tile: BonsaiTile): Pair<Int, Int> {
        val coordinate = map[tile] ?: throw invalidTile
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
     * Check if the grid is empty
     * @return `true` if the grid is empty, `false` otherwise
     */
    fun isEmpty() = map.isEmpty()

    /**
     * Check if the grid is NOT empty
     * @return `true` if the grid is NOT empty, `false` otherwise
     */
    fun isNotEmpty() = map.isNotEmpty()

    /**
     * Check if the given axial coordinates are empty
     * @param q q coordinate
     * @param r r coordinate
     * @return `true` if the coordinates are empty, `false` otherwise
     */
    fun isEmpty(q:Int, r:Int) = grid[axial2Raw(q)][axial2Raw(r)] == null

    /**
     * Check if the given axial coordinates are NOT empty
     * @param q q coordinate
     * @param r r coordinate
     * @return `true` if the coordinates are NOT empty, `false` otherwise
     */
    fun isNotEmpty(q:Int, r:Int) = grid[axial2Raw(q)][axial2Raw(r)] != null

    /**
     * Get the mapping of [BonsaiTile] to its axial coordinates
     * @return [Map] of [BonsaiTile] to its axial coordinates
     */
    fun getTileToCoordinateMap() = map.mapValues { Pair(raw2Axial(it.value.first), raw2Axial(it.value.second)) }

    /**
     * Get the mapping of axial coordinates to [BonsaiTile]
     * @return [Map] of axial coordinates to [BonsaiTile]
     */
    fun getCoordinateToTileMap() = map.entries.associate {(k, v) -> Pair(raw2Axial(v.first), raw2Axial(v.second)) to k}

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