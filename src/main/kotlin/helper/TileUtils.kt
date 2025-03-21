package helper
import entity.BonsaiTile
import entity.HexGrid
import entity.TileType

/**
 * Object to provide helper functions for tiles
 */
object TileUtils {
    /**
     * Returns the minimum List of tiles that need to be removed from the Bonsai to maintain the rules
     * @param tiles The list of [BonsaiTile] to be checked
     * @param grid The [HexGrid] to check the tiles against
     * @return The list of [BonsaiTile] that need to be removed
     */
    fun leastGroupOfTilesToBeRemoved(tiles: List<BonsaiTile>, grid: HexGrid?): List<BonsaiTile> {
        checkNotNull(grid) { "Grid cannot be null" }

        return tiles.filter { tile ->
            val neighbors = grid.getNeighbors(tile)

            if (!neighbors.any { neighbor -> neighbor.type == TileType.WOOD }) return@filter false
            if (tile.type == TileType.WOOD) return@filter false
            if (neighbors.size == 6) return@filter false
            if (tile.type == TileType.FLOWER || tile.type == TileType.FRUIT) return@filter true

            if (tile.type == TileType.LEAF) {
                val neighborFruits = neighbors.filter { it.type == TileType.FRUIT }
                val neighborFlowers = neighbors.filter { it.type == TileType.FLOWER }

                if (neighborFlowers.isEmpty() && neighborFruits.isEmpty()) return@filter true

                if (neighborFlowers.any { flower ->
                        grid.getNeighbors(flower).count { it.type == TileType.LEAF } < 2
                    }) return@filter false

                for (fruit in neighborFruits) {
                    val fruitLeafNeighbors = grid.getNeighbors(fruit)
                        .filter { it.type == TileType.LEAF && it != tile }
                    if (!hasAdjacentPair(fruitLeafNeighbors, grid)) return@filter false
                }
                return@filter true
            }
            false
        }
    }

    /**
     * Checks if the give list of leaf tiles has an adjacent pair
     * @param leafTiles The list of leaf [BonsaiTile] to check
     * @param grid The [HexGrid] to check the tiles against
     * @return True if the list of leaf tiles has at least an adjacent pair, false otherwise
     */
    fun hasAdjacentPair(leafTiles: List<BonsaiTile>, grid: HexGrid?): Boolean {
        checkNotNull(grid) { "Grid cannot be null" }

        return leafTiles.any { leaf ->
            grid.getNeighbors(leaf).any { it in leafTiles }
        }
    }
}