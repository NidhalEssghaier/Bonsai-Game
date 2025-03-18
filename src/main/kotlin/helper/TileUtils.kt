package helper
import entity.BonsaiTile
import entity.HexGrid
import entity.TileType

object TileUtils {
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

    fun hasAdjacentPair(leafTiles: List<BonsaiTile>, grid: HexGrid?): Boolean {
        checkNotNull(grid) { "Grid cannot be null" }

        return leafTiles.any { leaf ->
            grid.getNeighbors(leaf).any { it in leafTiles }
        }
    }
}