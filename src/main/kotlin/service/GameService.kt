package service

import entity.*
import helper.*

class GameService(private val rootService:RootService):AbstractRefreshingService() {

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
     * @throws IllegalArgumentException If a player name or type is missing.
     * @throws IllegalArgumentException If the simulation speed is not set.
     * @throws IllegalArgumentException If game type and player types are not matching
     * (e.g. local players in remote).
     *
     * @sample startNewGame(mutableListOf(Pair("Max Mustermann",0)),3,false)
     */
    fun startNewGame(players: List<Triple<String, Int, PotColor>>, speed: Int, goalCards: List<GoalCard>) {
        val drawStack = prepareCards(players.size)
        val openCards = drawStack.popAll(4).toMutableList()
        val playerList = mutableListOf<Player>()
        for(triple in players) {
            when(triple.second) {
                0 -> playerList.add(LocalPlayer(triple.first, triple.third))
                1 -> playerList.add(NetworkPlayer(triple.first, triple.third))
                2 -> playerList.add(RandomBot(triple.first, triple.third))
                3 -> playerList.add(SmartBot(triple.first, triple.third))
            }
        }

        rootService.currentGame = BonsaiGame(speed,playerList,goalCards,drawStack,openCards)
        onAllRefreshables { refreshAfterStartNewGame() }
    }

    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    private fun prepareCards(playerCount: Int) : ArrayDeque<ZenCard> {
        val cardStack = ArrayDeque<ZenCard>()

        //growth cards
        repeat(2) {
            cardStack.push(GrowthCard(TileType.WOOD))
            cardStack.push(GrowthCard(TileType.LEAF))
            cardStack.push(GrowthCard(TileType.FLOWER))
            cardStack.push(GrowthCard(TileType.FRUIT))
        }
        if(playerCount>2) {
            cardStack.push(GrowthCard(TileType.WOOD))
            cardStack.push(GrowthCard(TileType.LEAF))
            cardStack.push(GrowthCard(TileType.LEAF))
            cardStack.push(GrowthCard(TileType.FLOWER))
        }
        if (playerCount>3) {
            cardStack.push(GrowthCard(TileType.FLOWER))
            cardStack.push(GrowthCard(TileType.FRUIT))
        }

        //tool cards
        val toolCardCount = when(playerCount) {
            2 -> 3
            3 -> 5
            else -> 6
        }
        repeat(toolCardCount) {
            cardStack.push(ToolCard())
        }

        //master cards
        cardStack.push(MasterCard(listOf(TileType.WOOD,TileType.WOOD)))
        cardStack.push(MasterCard(listOf(TileType.LEAF,TileType.LEAF)))
        cardStack.push(MasterCard(listOf(TileType.WOOD,TileType.LEAF)))
        cardStack.push(MasterCard(listOf(TileType.GENERIC)))
        cardStack.push(MasterCard(listOf(TileType.GENERIC)))
        cardStack.push(MasterCard(listOf(TileType.LEAF,TileType.LEAF)))
        cardStack.push(MasterCard(listOf(TileType.LEAF,TileType.FRUIT)))
        if(playerCount>2) {
            cardStack.push(MasterCard(listOf(TileType.GENERIC)))
            cardStack.push(MasterCard(listOf(TileType.WOOD,TileType.LEAF)))
            cardStack.push(MasterCard(listOf(TileType.WOOD,TileType.LEAF)))
            cardStack.push(MasterCard(listOf(TileType.WOOD,TileType.LEAF,TileType.FLOWER)))
            cardStack.push(MasterCard(listOf(TileType.WOOD,TileType.LEAF,TileType.FRUIT)))
        }
        if(playerCount>3) {
            cardStack.push((MasterCard(listOf(TileType.LEAF,TileType.FLOWER,TileType.FLOWER))))
        }

        //helper cards
        repeat(3) {cardStack.push(HelperCard(listOf(TileType.GENERIC,TileType.WOOD)))}
        repeat(2) {cardStack.push(HelperCard(listOf(TileType.GENERIC,TileType.LEAF)))}
        cardStack.push(HelperCard(listOf(TileType.GENERIC,TileType.FLOWER)))
        cardStack.push(HelperCard(listOf(TileType.GENERIC,TileType.FRUIT)))

        //parchment cards
        cardStack.push(ParchmentCard(2,ParchmentCardType.MASTER,34))
        cardStack.push(ParchmentCard(2,ParchmentCardType.GROWTH,35))
        cardStack.push(ParchmentCard(2,ParchmentCardType.HELPER,36))
        cardStack.push(ParchmentCard(2,ParchmentCardType.FLOWER,37))
        cardStack.push(ParchmentCard(2,ParchmentCardType.FRUIT,38))
        cardStack.push(ParchmentCard(1,ParchmentCardType.LEAF,39))
        cardStack.push(ParchmentCard(1,ParchmentCardType.WOOD,40))

        cardStack.shuffle()

        return cardStack
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

    fun endGame() : List<Pair<Player,Int>> {
        val game = rootService.currentGame
        checkNotNull(game)
        val playerList = game.currentState.players

        val pointsPerPlayer = mutableMapOf<Player,Int>()
        for(player in playerList) {
            val bonsai = player.bonsai
            /*val numberOfLeafTiles = bonsai.tileCount[TileType.LEAF]
            checkNotNull(numberOfLeafTiles)
            val numberOfFruitTiles = bonsai.tileCount[TileType.FRUIT]
            checkNotNull(numberOfFruitTiles)*/

            var numberOfWoodTiles = 0
            var numberOfLeafTiles = 0
            var numberOfFlowerTiles = 0
            var numberOfFruitTiles = 0
            var sumOfFlowerPoints = 0

            for(tile in bonsai.grid.getInternalMap().keys) {
                when(tile.type) {
                    TileType.WOOD -> numberOfWoodTiles += 1
                    TileType.LEAF -> numberOfLeafTiles += 1
                    TileType.FLOWER -> {
                        numberOfFlowerTiles += 1
                        sumOfFlowerPoints += 6 - bonsai.grid.getNeighbors(tile).size
                    }
                    TileType.FRUIT -> numberOfFruitTiles += 1
                    else -> {}
                }
            }

            val cardPoints = mutableMapOf<ParchmentCardType,Int>()
            for(type in ParchmentCardType.entries) {
                cardPoints[type] = 0
            }

            for(card in player.hiddenDeck) {
                if(card is ParchmentCard) {
                    checkNotNull(cardPoints[card.type])
                    val newValue = cardPoints[card.type]?.plus(card.points)
                    checkNotNull(newValue)
                    cardPoints[card.type] = newValue
                }
            }

            val finalWoodPoints = numberOfWoodTiles * cardPoints.getValue(ParchmentCardType.WOOD)
            val finalLeafPoints = numberOfLeafTiles * (3 + cardPoints.getValue(ParchmentCardType.LEAF))
            val finalFruitPoints = numberOfFruitTiles * (7 + cardPoints.getValue(ParchmentCardType.FRUIT))
            val finalFlowerPoints = sumOfFlowerPoints + numberOfFlowerTiles * cardPoints.getValue(ParchmentCardType.FLOWER)
            val tilePoints = finalWoodPoints + finalLeafPoints + finalFruitPoints + finalFlowerPoints

            var goalPoints = 0
            for(goal in player.acceptedGoals) {
                goalPoints += goal.points
            }

            val points = tilePoints + goalPoints

            pointsPerPlayer[player] = points
        }
        val scoreList = pointsPerPlayer.toList().sortedByDescending { pair -> pair.second }
        //a tie situation is already handled via sortedByDescending, because equal values stay in the same order

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
    fun loadGame() {}

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
    fun saveGame() {}
}
