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
 *   // Get the list of neighbors of the given axial coordinates
 *   val neighbors = grid.getNeighbors(0, 0)
 *
 *   // Get the list of neighbors of the given BonsaiTile
 *   val neighbors = grid.getNeighbors(tileAt00)
 *
 *   // Remove the BonsaiTile from the grid
 *   grid.remove(tileAt00)
 *
 *   // Remove the BonsaiTile at the given coordinates
 *   grid.remove(0, 0)
 *
 *   // Deep copy the HexGrid
 *   val gridCopy = grid.copy()
 *   ```
 * @property size size of the grid, must be greater than or equal to 3
 * @property actualSize actual size of the internal array (Only for internal usage)
 * @property grid Store the [BonsaiTile] with coordinates (Only for internal usage)
 * @property map Mapping between [BonsaiTile] and its coordinates (Only for internal usage)
 * @property tilesList Returns the list of all [BonsaiTile] in the grid
 * @property axialRange Range of axial coordinates from -[size] to [size] (Only for internal usage)
 * @property axial2Raw Function to convert axial coordinates to raw internal coordinates (Only for internal usage)
 * @property raw2Axial Function to convert raw internal coordinates to axial coordinates (Only for internal usage)
 * @property maxIndex Maximum index of the internal array (Only for internal usage)
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

    private val maxIndex = actualSize - 1

    /**
     * Companion object to store the exceptions
     * @property invalidCoordinate Exception for no [BonsaiTile] at the given coordinates
     * @property invalidTile Exception for [BonsaiTile] not in the grid
     * @property IS_POT_MSG Exception for placing [BonsaiTile] in Pot area
     * @property outOfBounds Exception for coordinate out of bounds
     */
    private companion object {
        private val invalidCoordinate = NoSuchElementException("No BonsaiTile at the given coordinates")
        private val invalidTile = NoSuchElementException("This BonsaiTile isn't in the grid")
        private const val IS_POT_MSG = "Unable to place or get BonsaiTile in Pot area"
        private val outOfBounds = IndexOutOfBoundsException("Coordinate out of bounds")
    }

    /**
     * Secondary public constructor to create a [HexGrid] with coordinates ranging from -[size] to [size]
     * @param size size of the grid
     * @throws IllegalArgumentException if the size is less than 3
     */
    constructor(size: Int): this(
        size.also { require( it >= 3 ) },
        2*size + 1,
        Array(2*size + 1) { arrayOfNulls<BonsaiTile?>(2*size + 1) },
        mutableMapOf()
    )

    /**
     * Check if the given axial coordinates are in the Pot area
     * @param q q coordinate
     * @param r r coordinate
     * @return `true` if the coordinates are in the Pot area, `false` otherwise
     */
    fun isPot(q: Int, r: Int): Boolean {
        val potRangeQ = -2..3
        val potRangeR = 0..2

        val allowed = setOf(
            Pair(3,1),
            Pair(2,2),
            Pair(3,2)
        )

        return when {
            q to r in allowed -> false
            q in potRangeQ && r in potRangeR -> true
            else -> false
        }
    }

    /**
     * Check if the given axial coordinates are NOT in the Pot area
     * @param q q coordinate
     * @param r r coordinate
     * @return `true` if the coordinates are NOT in the Pot area, `false` otherwise
     */
    fun isNotPot(q: Int, r: Int) = !isPot(q, r)

    /**
     * Get the [BonsaiTile] at the given axial coordinates
     * @param q q coordinate
     * @param r r coordinate
     * @return [BonsaiTile] at the given axial coordinates
     * @throws IndexOutOfBoundsException if the coordinate is out of bounds
     * @throws NoSuchElementException if there is no [BonsaiTile] at the given coordinates
     * @throws IllegalArgumentException if the coordinates are in the Pot area
     */
    operator fun get(q: Int, r: Int): BonsaiTile {
        require( !isPot(q, r) ) { IS_POT_MSG }
        val tile = grid[axial2Raw(q)][axial2Raw(r)] ?: throw invalidCoordinate
        return tile
    }

    /**
     * Set the [BonsaiTile] at the given axial coordinates
     * @param q q coordinate
     * @param r r coordinate
     * @param tile [BonsaiTile] to set
     * @throws IndexOutOfBoundsException if the coordinate is out of bounds
     * @throws IllegalArgumentException if the coordinates are in the Pot area
     */
    operator fun set(q: Int, r: Int, tile: BonsaiTile) {
        require( !isPot(q, r) ) { IS_POT_MSG }
        val nq = axial2Raw(q)
        val nr = axial2Raw(r)
        grid[nq][nr] = tile
        map[tile] = Pair(nq, nr)
    }

    /**
     * Get the [BonsaiTile] at the given axial coordinates or `null` if the coordinates are empty or in the Pot area
     * @param q q coordinate
     * @param r r coordinate
     * @return [BonsaiTile] at the given axial coordinates or `null` if the coordinates are empty or in the Pot area
     * @throws IndexOutOfBoundsException if the coordinate is out of bounds
     */
    fun getOrNull(q: Int, r: Int) = grid[axial2Raw(q)][axial2Raw(r)]

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
     * Remove the [BonsaiTile] at the given axial coordinates
     * @param q q coordinate
     * @param r r coordinate
     * @return `true` if the [BonsaiTile] is removed successfully, `false` otherwise
     * @throws IndexOutOfBoundsException if the coordinate is out of bounds
     */
    fun remove(q: Int, r: Int): Boolean {
        if ( isPot(q, r) ) return false
        val nq = axial2Raw(q)
        val nr = axial2Raw(r)

        val tile = grid[nq][nr] ?: return false

        map.remove(tile)
        grid[nq][nr] = null

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
     * @throws IndexOutOfBoundsException if the coordinate is out of bounds
     */
    fun isEmpty(q:Int, r:Int): Boolean {
        if ( isPot(q, r) ) return false
        return grid[axial2Raw(q)][axial2Raw(r)] == null
    }

    /**
     * Check if the given axial coordinates are NOT empty
     * @param q q coordinate
     * @param r r coordinate
     * @return `true` if the coordinates are NOT empty, `false` otherwise
     * @throws IndexOutOfBoundsException if the coordinate is out of bounds
     */
    fun isNotEmpty(q:Int, r:Int): Boolean {
        if ( isPot(q ,r) ) return true
        return grid[axial2Raw(q)][axial2Raw(r)] != null
    }

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

        return HexGrid(size, actualSize, gridCopy, mapCopy.toMutableMap())
    }

    /**
     * Get the area around the given raw coordinates
     * @param rawQ raw internal q coordinate
     * @param rawR raw internal r coordinate
     * @return [Set] of coordinates in the area
     */
    private fun getArea(rawQ: Int, rawR: Int): Set<Pair<Int, Int>> {
        val nqPlusOne = rawQ + 1
        val nqMinusOne = rawQ - 1

        val nrPlusOne = rawR + 1
        val nrMinusOne = rawR - 1

        val maxNQ = if(nqPlusOne > maxIndex) rawQ else nqPlusOne
        val minNQ = if(nqMinusOne < 0) rawQ else nqMinusOne

        val maxNR = if(nrPlusOne > maxIndex) rawR else nrPlusOne
        val minNR = if(nrMinusOne < 0) rawR else nrMinusOne
        return setOf(
            rawQ to rawR,
            rawQ to minNR,
            maxNQ to minNR,
            maxNQ to rawR,
            rawQ to maxNR,
            minNQ to maxNR,
            minNQ to rawR
        )
    }

    /**
     * Get the list of empty spaces around the given raw coordinates
     * @param rawQ raw internal q coordinate
     * @param rawR raw internal r coordinate
     * @return [List] of empty spaces
     */
    private fun getAreaEmptySpace(rawQ: Int, rawR: Int): List<Pair<Int, Int>> {
        val coordinateSet = getArea(rawQ, rawR)

        val emptySpaceList = mutableListOf<Pair<Int, Int>>()
        for (coordinate in coordinateSet) {
            val qAxial = raw2Axial(coordinate.first)
            val rAxial = raw2Axial(coordinate.second)
            if( isPot(qAxial, rAxial) ) continue
            if (grid[coordinate.first][coordinate.second] == null) {
                emptySpaceList.add(qAxial to rAxial)
            }
        }

        return emptySpaceList.toList()
    }

    /**
     * Get the list of empty spaces around the given axial coordinates
     * @param q q coordinate
     * @param r r coordinate
     * @return [List] of empty spaces coordinates
     * @throws IndexOutOfBoundsException if the coordinates are out of bounds
     */
    fun getEmptySpace(q: Int, r: Int): List<Pair<Int, Int>> {
        if ( !(q in axialRange && r in axialRange) ) throw outOfBounds
        return getAreaEmptySpace(axial2Raw(q), axial2Raw(r))
    }

    /**
     * Get the list of empty spaces around the given [BonsaiTile]
     * @param tile [BonsaiTile]
     * @return [List] of empty spaces coordinates
     * @throws NoSuchElementException if the [BonsaiTile] isn't in the grid
     */
    fun getEmptySpace(tile: BonsaiTile): List<Pair<Int, Int>> {
        val coordinate = map[tile] ?: throw invalidTile
        return getAreaEmptySpace(coordinate.first, coordinate.second)
    }

    /**
     * Get the list of neighbors of the given raw internal coordinates
     * @param rawQ raw q coordinate
     * @param rawR raw r coordinate
     * @return [List] of neighbors
     */
    private fun getNeighborsWithRawCoordinate(rawQ: Int, rawR: Int): List<BonsaiTile> {
        val coordinateSet = getArea(rawQ, rawR)

        val neighborsList = mutableListOf<BonsaiTile>()
        for (coordinate in coordinateSet) {
            if (coordinate.first == rawQ && coordinate.second == rawR) continue
            val neighbor = grid[coordinate.first][coordinate.second]
            if (neighbor != null) {
                neighborsList.add(neighbor)
            }
        }
        return neighborsList.toList()
    }

    /**
     * Get the list of neighbors of the given axial coordinates
     * @param q q coordinate
     * @param r r coordinate
     * @return [List] of neighbors
     * @throws IndexOutOfBoundsException if the coordinate is out of bounds
     */
    fun getNeighbors(q: Int, r: Int): List<BonsaiTile> {
        if ( !(q in axialRange && r in axialRange) ) throw outOfBounds

        return getNeighborsWithRawCoordinate(axial2Raw(q), axial2Raw(r))
    }

    /**
     * Get the list of neighbors of the given [BonsaiTile]
     * @param tile [BonsaiTile]
     * @return [List] of neighbors
     * @throws NoSuchElementException if the [BonsaiTile] isn't in the grid
     */
    fun getNeighbors(tile: BonsaiTile): List<BonsaiTile> {
        val coordinate = map[tile] ?: throw invalidTile

        return getNeighborsWithRawCoordinate(coordinate.first, coordinate.second)
    }
}
