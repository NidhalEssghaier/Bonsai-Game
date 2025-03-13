package entity


/**
 * Entity to represent the player type "Local"
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
class LocalPlayer private constructor(
    name: String,
    potColor: PotColor,

    bonsai: Bonsai,
    supplyTileLimit: Int = 5,
    treeTileLimit: MutableMap<TileType, Int> = mutableMapOf(),
    declinedGoals: MutableList<GoalCard> = mutableListOf(),
    acceptedGoals: MutableList<GoalCard> = mutableListOf(),
    forbiddenGoals: MutableList<GoalCard> = mutableListOf(),
    seishiTool: ArrayDeque<ZenCard> = ArrayDeque(),
    seishiGrowth: ArrayDeque<ZenCard> = ArrayDeque(),
    hiddenDeck: MutableList<ZenCard> = mutableListOf(),
    supply: MutableList<BonsaiTile> = mutableListOf()
): Player(
    name,
    potColor,
    bonsai,
    supplyTileLimit,
    treeTileLimit,
    declinedGoals,
    acceptedGoals,
    forbiddenGoals,
    seishiTool,
    seishiGrowth,
    hiddenDeck,
    supply
)
{
    /**
     * Secondary public constructor to create a player instance
     */
    constructor(name: String, potColor: PotColor): this(name, potColor, Bonsai())

    /**
     * Make a deep copy of the LocalPlayer instance
     * @return A deep copy of the LocalPlayer instance
     */
    override fun copy(): LocalPlayer {
        return LocalPlayer(
            name,
            potColor,
            bonsai.copy(),
            supplyTileLimit,
            treeTileLimit.toMutableMap(),
            declinedGoals.toMutableList(),
            acceptedGoals.toMutableList(),
            forbiddenGoals.toMutableList(),
            ArrayDeque(seishiTool),
            ArrayDeque(seishiGrowth),
            hiddenDeck.toMutableList(),
            supply.toMutableList()
        )
    }
}
