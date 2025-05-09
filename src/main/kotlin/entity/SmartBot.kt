package entity

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import serializer.ArrayDequeZenCardSerializer

/**
 * Entity to represent a bot, based on a smart algorithm
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
class SmartBot private constructor(
    override val name: String,
    override var potColor: PotColor,
    override var bonsai: Bonsai,
    override val forbiddenGoals: MutableList<GoalCard> = mutableListOf()
): Player
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
     * Make a deep copy of the [SmartBot] instance
     * @return The deep copy of the [SmartBot] instance
     */
    override fun copy(): SmartBot {
        return SmartBot(
            name,
            potColor,
            bonsai.copy(),
            forbiddenGoals.toMutableList()
        ).apply {
            this.supplyTileLimit = this@SmartBot.supplyTileLimit
            this.treeTileLimit = this@SmartBot.treeTileLimit.toMutableMap()
            this.declinedGoals = this@SmartBot.declinedGoals.toMutableList()
            this.acceptedGoals = this@SmartBot.acceptedGoals.toMutableList()
            this.seishiTool = ArrayDeque(this@SmartBot.seishiTool)
            this.seishiGrowth = ArrayDeque(this@SmartBot.seishiGrowth)
            this.hiddenDeck = this@SmartBot.hiddenDeck.toMutableList()
            this.supply = this@SmartBot.supply.toMutableList()
            this.usedHelperTiles = this@SmartBot.usedHelperTiles.toMutableList()
            this.usedHelperCards = this@SmartBot.usedHelperCards.toMutableList()
            this.hasDrawnCard = this@SmartBot.hasDrawnCard
            this.hasCultivated = this@SmartBot.hasCultivated
        }
    }
}