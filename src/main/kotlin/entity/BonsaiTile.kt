package entity

/**
 * Entity to represent a bonsai tile
 *
 * @property type The type of the tile (e.g. wood)
 */
class BonsaiTile(
    val type: TileType,
){
    //used to track neighbors to help in remove tiles and place fruit and get flower score
    val neighbors: MutableList<Bonsai> = mutableListOf()

}
