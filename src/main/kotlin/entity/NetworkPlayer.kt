package entity

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import serializer.ArrayDequeZenCardSerializer

/**
 * Entity to represent the player type "Network"
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
@Serializable
class NetworkPlayer private constructor(
    override val name: String,
    override var potColor: PotColor,
    override var bonsai: Bonsai,
    override val forbiddenGoals: MutableList<GoalCard> = mutableListOf()
) : Player
{
    override var supplyTileLimit: Int = 5
    override var treeTileLimit: MutableMap<TileType, Int> = mutableMapOf(
        TileType.GENERIC to 1,
        TileType.WOOD to 1,
        TileType.LEAF to 1
    )
    override var declinedGoals: MutableList<GoalCard> = mutableListOf()
    override var acceptedGoals: MutableList<GoalCard> = mutableListOf()
    @Serializable(with = ArrayDequeZenCardSerializer::class)
    override var seishiTool: ArrayDeque<ZenCard> = ArrayDeque()
    @Serializable(with = ArrayDequeZenCardSerializer::class)
    override var seishiGrowth: ArrayDeque<ZenCard> = ArrayDeque()
    override var hiddenDeck: MutableList<@Polymorphic ZenCard> = mutableListOf()
    override var supply: MutableList<BonsaiTile> = mutableListOf()
    override var usedHelperTiles: MutableList<TileType> = mutableListOf()
    override var usedHelperCards: MutableList<HelperCard> = mutableListOf()
    override var hasDrawnCard: Boolean = false
    override var hasCultivated: Boolean =false

    /**
     * Secondary public constructor to create a player instance
     */
    constructor(name: String, potColor: PotColor): this(name, potColor, Bonsai())

    /**
     * Make a deep copy of the NetworkPlayer instance
     * @return A deep copy of the NetworkPlayer instance
     */
    override fun copy(): NetworkPlayer {
        return NetworkPlayer(
            name,
            potColor,
            bonsai.copy(),
            forbiddenGoals.toMutableList()
        ).apply {
            this.supplyTileLimit = this@NetworkPlayer.supplyTileLimit
            this.treeTileLimit = this@NetworkPlayer.treeTileLimit.toMutableMap()
            this.declinedGoals = this@NetworkPlayer.declinedGoals.toMutableList()
            this.acceptedGoals = this@NetworkPlayer.acceptedGoals.toMutableList()
            this.seishiTool = ArrayDeque(this@NetworkPlayer.seishiTool)
            this.seishiGrowth = ArrayDeque(this@NetworkPlayer.seishiGrowth)
            this.hiddenDeck = this@NetworkPlayer.hiddenDeck.toMutableList()
            this.supply = this@NetworkPlayer.supply.toMutableList()
            this.usedHelperTiles = this@NetworkPlayer.usedHelperTiles.toMutableList()
            this.usedHelperCards = this@NetworkPlayer.usedHelperCards.toMutableList()
            this.hasDrawnCard = this@NetworkPlayer.hasDrawnCard
            this.hasCultivated = this@NetworkPlayer.hasCultivated
        }
    }
}
