package entity

/**
 * Entity to represent the hex grid of the bonsai bowl
 * @property size An odd number that determines the size of the grid
 * @property offset Offset to calculate the coordinates from the grid index (Only for internal usage)
 * @property grid Store the [BonsaiTile] with coordinates (Only for internal usage)
 * @property map Mapping between [BonsaiTile] and its coordinates (Only for internal usage)
 * @property tilesList Returns the list of all [BonsaiTile] in the grid
 * @constructor Create a [HexGrid] of [size] x [size] with coordinates ranging from -[size]/2 to [size]/2
 */
class HexGrid {
    val size: Int
    private val offset: Int
    private val grid: Array<Array<BonsaiTile?>>
    private val map: MutableMap<BonsaiTile, Pair<Int, Int>>
    val tilesList: () -> List<BonsaiTile>

    /**
     * Create a [HexGrid] of [size] x [size] with coordinates ranging from -[size]/2 to [size]/2
     * @param size An odd number that determines the size of the grid
     * @throws IllegalArgumentException if the size isn't odd
     */
    constructor(size: Int) {
        require(size % 2 == 1) {"Size must be odd"}
        this.size = size
        offset = this.size / 2
        grid = Array(size) { arrayOfNulls(size) }
        map = mutableMapOf()
        tilesList = {map.keys.toList()}
    }

    /**
     * Create a [HexGrid] of [size] x [size] with coordinates ranging from -[size]/2 to [size]/2
     * This constructor is only for internal deep copy usage
     * @param size An odd number that determines the size of the grid
     * @param grid Store the [BonsaiTile] with coordinates
     * @param map Mapping between [BonsaiTile] and its coordinates
     * @throws IllegalArgumentException if the size isn't odd
     */
    private constructor(size: Int, grid: Array<Array<BonsaiTile?>>, map: MutableMap<BonsaiTile, Pair<Int, Int>>) {
        require(size % 2 == 1) {"Size must be odd"}
        this.size = size
        offset = this.size / 2
        this.grid = grid
        this.map = map
        tilesList = {this.map.keys.toList()}
    }

    /**
     * Convert axial coordinates to internal array representation
     * @param axialCoordinate Axial coordinate
     * @return Converted coordinate
     */
    private fun axial2Raw(axialCoordinate: Int) = axialCoordinate + offset

    /**
     * Convert internal array representation to axial coordinates
     * @param rawCoordinate Raw coordinate
     * @return Converted coordinate
     */
    private fun raw2Axial(rawCoordinate: Int) = rawCoordinate - offset

    /**
     * Get the [BonsaiTile] at the given axial coordinates
     * @param q q coordinate
     * @param r r coordinate
     * @return [BonsaiTile] at the given axial coordinates
     * @throws IllegalArgumentException if the coordinate is out of bounds
     * @throws IllegalStateException if there is no [BonsaiTile] at the given coordinates
     */
    operator fun get(q: Int, r: Int): BonsaiTile {
        val nq = axial2Raw(q)
        val nr = axial2Raw(r)
        require(nq < size && nr < size) {"Coordinate out of bounds"}
        val tile = grid[nq][nr]
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
        val nq = axial2Raw(q)
        val nr = axial2Raw(r)
        require(nq < size && nr < size) {"Coordinate out of bounds"}
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
        val gridCopy = Array(size) { arrayOfNulls<BonsaiTile?>(size) }
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

        return HexGrid(size, gridCopy, mapCopy.toMutableMap())
    }
}