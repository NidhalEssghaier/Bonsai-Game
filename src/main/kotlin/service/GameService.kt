package service

import entity.*
import helper.*
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.io.IOException

/**
 * @property dirPath The path to the directory where the save file is located.
 * @property saveFilePath The path to the save file.
 * @property jsonSerializer The [Json] object that contains the polymorphic serializer.
 */
class GameService(
    private val rootService: RootService,
) : AbstractRefreshingService() {

    val dirPath = File("data").apply {
        if (!exists()) mkdirs() // Ensure the directory exists
    }
    val saveFilePath = File(dirPath, "save.json")

    private val jsonSerializer = Json {
        serializersModule = SerializersModule {
            allowStructuredMapKeys = true
            polymorphic(ZenCard::class) {
                subclass(ToolCard::class)
                subclass(MasterCard::class)
                subclass(HelperCard::class)
                subclass(GrowthCard::class)
                subclass(ParchmentCard::class)
                subclass(PlaceholderCard::class)
            }

            polymorphic(Player::class) {
                subclass(LocalPlayer::class)
                subclass(NetworkPlayer::class)
                subclass(RandomBot::class)
                subclass(SmartBot::class)
            }
        }
    }

    /**
     * Starts a new game and prepares different game elements
     * @param players A list of pairs that represent a player.
     * The pair consists of the player name and the type of the player as number:
     * local player (0), remote player (1), random bot (2), smart bot (3)
     * @param speed The simulation speed of the game.
     * @param remote If the game is played in remote or in hotseat mode.
     *
     * Preconditions:
     * - The application is running.
     * - The sorting order of the players has been defined.
     * - The type of the players has been defined.
     * - The simulation speed has been set.
     * - The game type has been set.
     *
     * Postconditions:
     * - A game was created.
     * - The goal plates have been configured.
     * - The card shop has been prepared.
     * - The tile storage has been prepared.
     * - Every player has a plant pot and a seishi plate.
     * - The inventories of the players have been filled with tiles.
     *
     * @return This method does not return anything ('Unit').
     *
     * @sample startNewGame(mutableListOf(Pair("Max Mustermann",0)),3,false)
     */
    fun startNewGame(
        players: List<Triple<String, Int, PotColor>>,
        speed: Int,
        goalColors: List<GoalColor>,
    ) {
        require(players.size in 2..4) { "Need 2-4 players to play the game" }
        require(
            goalColors.size == 3 &&
                goalColors.size == goalColors.toSet().size,
        ) { "Must select 3 different goal colors" }

        val drawStack = prepareCards(players.size)
        val openCards = drawStack.popAll(4).toMutableList()
        val playerList = mutableListOf<Player>()
        for (triple in players) {
            when (triple.second) {
                0 -> playerList.add(LocalPlayer(triple.first, triple.third))
                1 -> playerList.add(NetworkPlayer(triple.first, triple.third))
                2 -> playerList.add(RandomBot(triple.first, triple.third))
                3 -> playerList.add(SmartBot(triple.first, triple.third))
                else -> throw IllegalArgumentException()
            }
        }

        val goalCards = prepareGoals(playerList.size, goalColors)

        rootService.currentGame = BonsaiGame(speed, playerList, goalCards, drawStack, openCards)

        val currentGame = rootService.currentGame
        checkNotNull(currentGame) { "Internal error! currentGame is null, it shouldn't happened here." }

        // Push the initial state to the undo stack
        currentGame.undoStack.push(currentGame.currentState.copy())

        onAllRefreshables { refreshAfterStartNewGame() }
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    private fun prepareCards(playerCount: Int): ArrayDeque<ZenCard> {
        val cardStack = ArrayDeque<ZenCard>()

        // growth cards
        cardStack.push(GrowthCard(TileType.WOOD, 0))
        cardStack.push(GrowthCard(TileType.WOOD, 1))
        cardStack.push(GrowthCard(TileType.LEAF, 2))
        cardStack.push(GrowthCard(TileType.LEAF, 3))
        cardStack.push(GrowthCard(TileType.FLOWER, 4))
        cardStack.push(GrowthCard(TileType.FLOWER, 5))
        cardStack.push(GrowthCard(TileType.FRUIT, 6))
        cardStack.push(GrowthCard(TileType.FRUIT, 7))

        if (playerCount > 2) {
            cardStack.push(GrowthCard(TileType.WOOD, 8))
            cardStack.push(GrowthCard(TileType.LEAF, 9))
            cardStack.push(GrowthCard(TileType.LEAF, 10))
            cardStack.push(GrowthCard(TileType.FLOWER, 11))
        }
        if (playerCount > 3) {
            cardStack.push(GrowthCard(TileType.FLOWER, 12))
            cardStack.push(GrowthCard(TileType.FRUIT, 13))
        }

        // tool cards
        val toolCardCount =
            when (playerCount) {
                2 -> 3
                3 -> 5
                else -> 6
            }
        repeat(toolCardCount) {
            cardStack.push(ToolCard(it + 41))
        }

        // master cards
        cardStack.push(MasterCard(listOf(TileType.WOOD, TileType.WOOD), 21))
        cardStack.push(MasterCard(listOf(TileType.LEAF, TileType.LEAF), 22))
        cardStack.push(MasterCard(listOf(TileType.WOOD, TileType.LEAF), 23))
        cardStack.push(MasterCard(listOf(TileType.GENERIC), 24))
        cardStack.push(MasterCard(listOf(TileType.GENERIC), 25))
        cardStack.push(MasterCard(listOf(TileType.LEAF, TileType.LEAF), 26))
        cardStack.push(MasterCard(listOf(TileType.LEAF, TileType.FRUIT), 27))
        if (playerCount > 2) {
            cardStack.push(MasterCard(listOf(TileType.GENERIC), 28))
            cardStack.push(MasterCard(listOf(TileType.WOOD, TileType.LEAF), 29))
            cardStack.push(MasterCard(listOf(TileType.WOOD, TileType.LEAF), 30))
            cardStack.push(MasterCard(listOf(TileType.WOOD, TileType.LEAF, TileType.FLOWER), 31))
            cardStack.push(MasterCard(listOf(TileType.WOOD, TileType.LEAF, TileType.FRUIT), 32))
        }
        if (playerCount > 3) {
            cardStack.push((MasterCard(listOf(TileType.LEAF, TileType.FLOWER, TileType.FLOWER), 33)))
        }

        // helper cards
        repeat(3) { cardStack.push(HelperCard(listOf(TileType.GENERIC, TileType.WOOD), 14 + it)) }
        repeat(2) { cardStack.push(HelperCard(listOf(TileType.GENERIC, TileType.LEAF), 17 + it)) }
        cardStack.push(HelperCard(listOf(TileType.GENERIC, TileType.FLOWER), 19))
        cardStack.push(HelperCard(listOf(TileType.GENERIC, TileType.FRUIT), 20))

        // parchment cards
        cardStack.push(ParchmentCard(2, ParchmentCardType.MASTER, 34))
        cardStack.push(ParchmentCard(2, ParchmentCardType.GROWTH, 35))
        cardStack.push(ParchmentCard(2, ParchmentCardType.HELPER, 36))
        cardStack.push(ParchmentCard(2, ParchmentCardType.FLOWER, 37))
        cardStack.push(ParchmentCard(2, ParchmentCardType.FRUIT, 38))
        cardStack.push(ParchmentCard(1, ParchmentCardType.LEAF, 39))
        cardStack.push(ParchmentCard(1, ParchmentCardType.WOOD, 40))

        cardStack.shuffle()

        return cardStack
    }

    private fun prepareGoals(
        playerCount: Int,
        goalColors: List<GoalColor>,
    ): MutableList<GoalCard?> {
        val goals: MutableList<GoalCard?> = mutableListOf()

        goalColors.forEach { goalColor ->
            when (goalColor) {
                GoalColor.BROWN -> {
                    goals.add(GoalCard(5, goalColor, GoalDifficulty.LOW))
                    if (playerCount > 2) goals.add(GoalCard(10, goalColor, GoalDifficulty.INTERMEDIATE)) else goals.add(null)
                    goals.add(GoalCard(15, goalColor, GoalDifficulty.HARD))
                }
                GoalColor.ORANGE -> {
                    goals.add(GoalCard(9, goalColor, GoalDifficulty.LOW))
                    if (playerCount > 2) goals.add(GoalCard(11, goalColor, GoalDifficulty.INTERMEDIATE)) else goals.add(null)
                    goals.add(GoalCard(13, goalColor, GoalDifficulty.HARD))
                }
                GoalColor.GREEN -> {
                    goals.add(GoalCard(6, goalColor, GoalDifficulty.LOW))
                    if (playerCount > 2) goals.add(GoalCard(9, goalColor, GoalDifficulty.INTERMEDIATE)) else goals.add(null)
                    goals.add(GoalCard(12, goalColor, GoalDifficulty.HARD))
                }
                GoalColor.RED -> {
                    goals.add(GoalCard(8, goalColor, GoalDifficulty.LOW))
                    if (playerCount > 2) goals.add(GoalCard(12, goalColor, GoalDifficulty.INTERMEDIATE)) else goals.add(null)
                    goals.add(GoalCard(16, goalColor, GoalDifficulty.HARD))
                }
                GoalColor.BLUE -> {
                    goals.add(GoalCard(7, goalColor, GoalDifficulty.LOW))
                    if (playerCount > 2) goals.add(GoalCard(10, goalColor, GoalDifficulty.INTERMEDIATE)) else goals.add(null)
                    goals.add(GoalCard(14, goalColor, GoalDifficulty.HARD))
                }
            }
        }
        return goals
    }

    /**
     * Ends the game and evaluates which player won.
     *
     * This method does not accept parameters.
     *
     * Preconditions:
     * - A game was started and is running.
     * - All cards have been revealed.
     *
     * Postconditions:
     * - All players are evaluated based on their goal tiles, as well as on the basic multipliers
     *   and parchment card multipliers applied to their bonsai and hidden cards.
     * - The player with the highest number of points is declared the winner.
     *
     * @returns This method does not return anything (`Unit`).
     *
     * @throws IllegalStateException If there is no started and running game,
     *                               or if at least one card has not been revealed.
     *
     * @sample endGame()
     */

    fun endGame(): Map<Player, List<Int>> {
        val game = rootService.currentGame
        checkNotNull(game)
        val playerList = game.currentState.players

        check(game.currentState.drawStack.isEmpty())

        val pointsPerPlayer = mutableMapOf<Player, MutableList<Int>>()
        for (player in playerList) {
            val bonsai = player.bonsai

            var numberOfWoodTiles = 0
            var numberOfLeafTiles = 0
            var numberOfFlowerTiles = 0
            var numberOfFruitTiles = 0
            var sumOfFlowerPoints = 0

            for (tile in bonsai.grid.getInternalMap().keys) {
                when (tile.type) {
                    TileType.WOOD -> numberOfWoodTiles += 1
                    TileType.LEAF -> numberOfLeafTiles += 1
                    TileType.FLOWER -> {
                        numberOfFlowerTiles += 1
                        sumOfFlowerPoints += 6 - bonsai.grid.getNeighbors(tile)
                            .filter { neighbor->neighbor.type!=TileType.UNPLAYABLE  }.size
                    }
                    TileType.FRUIT -> numberOfFruitTiles += 1
                    else -> {}
                }
            }

            val cardPoints = mutableMapOf<ParchmentCardType, Int>()
            for (type in ParchmentCardType.entries) {
                cardPoints[type] = 0
            }

            for (card in player.hiddenDeck) {
                if (card is ParchmentCard) {
                    checkNotNull(cardPoints[card.type])
                    val newValue = cardPoints[card.type]?.plus(card.points)
                    checkNotNull(newValue)
                    cardPoints[card.type] = newValue
                }
            }

            val cardWoodPoints = numberOfWoodTiles * cardPoints.getValue(ParchmentCardType.WOOD)
            val leafPoints = numberOfLeafTiles * 3
            val cardLeafPoints = numberOfLeafTiles * cardPoints.getValue(ParchmentCardType.LEAF)
            val fruitPoints = numberOfFruitTiles * 7
            val cardFruitPoints = numberOfFruitTiles * cardPoints.getValue(ParchmentCardType.FRUIT)
            //sumOfFlowerPoints
            val cardFlowerPoints = numberOfFlowerTiles * cardPoints.getValue(ParchmentCardType.FLOWER)

            val sumCardPoints = cardWoodPoints + cardLeafPoints + cardFruitPoints + cardFlowerPoints
            //val tilePoints = finalWoodPoints + finalLeafPoints + finalFruitPoints + finalFlowerPoints

            var goalPoints = 0
            for (goal in player.acceptedGoals) {
                goalPoints += goal.points
            }

            val sumOfPoints = leafPoints + sumOfFlowerPoints + fruitPoints + sumCardPoints + goalPoints

            pointsPerPlayer[player] = mutableListOf(leafPoints,sumOfFlowerPoints,fruitPoints,sumCardPoints,goalPoints,sumOfPoints)
        }
        val scoreList = pointsPerPlayer.toList().sortedByDescending { pair -> pair.second.last() }.toMap()
        // a tie situation is already handled via sortedByDescending, because equal values stay in the same order

        onAllRefreshables { refreshAfterEndGame(scoreList) }

        return scoreList
    }

    /**
     * Loads a game from save file
     *
     * This method loads and unserializes game state from a save file and
     * notifies the UI layer to update the game sense.
     *
     * Preconditions:
     * - The application is running.
     * - The save file exists.
     * - The save file is readable.
     *
     * Postconditions:
     * - The game was created.
     * - The goal plates have returned to the recorded state.
     * - The card shop has returned to the recorded state.
     * - The tile storage has returned to the recorded state.
     * - All player's plan pots and cards return to the recorded state.
     * - The undo/redo Stack has returned to the recorded state
     *
     * @throws IllegalStateException if the save file doesn't exist
     * @throws IOException if the save file can't be read
     */
    fun loadGame() {
        check(saveFilePath.exists()) { "Save file doesn't exist." }

        rootService.currentGame = jsonSerializer.decodeFromString(BonsaiGame.serializer(), saveFilePath.readText())
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    /**
     * Saves the current game state to a save file
     *
     * This method serializes the current game state and writes it to a save file.
     * If the save file in the default path doesn't exist, it will be created, otherwise
     * it will be overwritten.
     *
     * Preconditions:
     * - The game was started and is running.
     * - When the save file already exists, it is writable.
     * - The save file path is writable.
     *
     * Postconditions:
     * - If the save file didn't exist, it was created.
     * - Otherwise, it was overwritten by the newest serialized game state.
     *
     * @throws IllegalStateException if the game hasn't started yet
     * @throws IOException if the save file can't be written to
     */
    fun saveGame() {
        val game = rootService.currentGame
        checkNotNull(game) { "No game has been started yet." }

        saveFilePath.writeText(
            jsonSerializer.encodeToString(BonsaiGame.serializer(), game)
        )
    }
}
