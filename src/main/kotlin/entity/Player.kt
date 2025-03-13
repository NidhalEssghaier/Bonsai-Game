package entity

/**
 * Abstract Entity to represent the player
 *
 * @property name The name of the player
 * @property supplyTileLimit The maximum number of tiles allowed in the personal inventory
 * @property treeTileLimit The maximum number of tiles allowed to be placed in the bonsai
 * @property declinedGoals The goals the player declined
 * @property acceptedGoals The goals the player accepted
 * @property seishiTool The tool cards the player has acquired
 * @property seishiGrowth The growth cards the player has acquired
 * @property hiddenDeck The cards the player has already used
 * @property supply The tile inventory of the player
 * @property bonsai The bonsai of the player
 */
interface Player
{
    val name: String
    var potColor: PotColor

    var bonsai: Bonsai
    var supplyTileLimit: Int
    var treeTileLimit: MutableMap<TileType, Int>
    var declinedGoals: MutableList<GoalCard>
    val acceptedGoals: MutableList<GoalCard>
    val forbiddenGoals: MutableList<GoalCard>
    var seishiTool: ArrayDeque<ZenCard>
    var seishiGrowth: ArrayDeque<ZenCard>
    var hiddenDeck: MutableList<ZenCard>
    var supply: MutableList<BonsaiTile>

    /**
     * Abstract method to deep copy the player instance
     * @return a deep copy of the player instance
     */
     fun copy(): Player
}