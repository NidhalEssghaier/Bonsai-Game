package service.network

import entity.*
import messages.CardTypeMessage
import messages.ColorTypeMessage
import messages.StartGameMessage
import org.junit.jupiter.api.BeforeEach
import service.ConnectionState
import service.MessageConverter
import service.NetworkService
import service.RootService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Class for testing the NetworkService and NetworkClient classes
 */
class NetworkServiceTest {

    private lateinit var rootServiceHost: RootService
    private lateinit var rootServiceGuest: RootService
    private lateinit var converter: MessageConverter

    /**
     * busy waiting for the game represented by this [RootService] to reach the desired network [state].
     * Polls the desired state every 100 ms until the [timeout] is reached.
     *
     * This is a simplification hack for testing purposes, so that tests can be linearized on
     * a single thread.
     *
     * @param state the desired network state to reach
     * @param timeout maximum milliseconds to wait (default: 15000)
     *
     * @throws IllegalStateException if desired state is not reached within the [timeout]
     */
    private fun RootService.waitForState(state: ConnectionState, timeout: Int = 15000) {
        var timePassed = 0
        while (timePassed < timeout) {
            if (networkService.connectionState == state) {
                return
            } else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Did not arrive at state $state after waiting $timePassed ms")
    }

    /**
     * Acts like startNewHostedGame but without random elements
     */
    private fun NetworkService.startDummyGame() {
        rootServiceHost.gameService.startNewGame(
            players = listOf(
                Triple("Test Guest", 1, PotColor.BLUE),
                Triple("Test Host", 0, PotColor.RED)
            ),
            speed = 1,
            goalColors = listOf(
                GoalColor.RED,
                GoalColor.GREEN,
                GoalColor.ORANGE
            )
        )

        val game = rootServiceHost.currentGame
        checkNotNull(game) { "game should not be null right after starting it." }

        val indices = listOf(0, 2, 1, 3, 4, 6, 7, 8 , 9, 10, 11, 12, 13, 14, 15,
                             16, 18, 19, 20, 21, 22, 23, 24, 26, 27, 28, 29, 30,
                             31, 32, 33, 34, 35, 36, 37, 39, 40, 41, 42, 43, 44,
                             45, 46)
        game.currentState.drawStack.clear()
        for (i in indices) {
            game.currentState.drawStack.add(when (i) {
                0, 1, 8, 12 -> GrowthCard(TileType.WOOD, i)
                2, 3, 9, 10 -> GrowthCard(TileType.LEAF, i)
                4, 5, 11 -> GrowthCard(TileType.FLOWER, i)
                6, 7, 13 -> GrowthCard(TileType.FRUIT, i)
                14, 15, 16 -> HelperCard(listOf(TileType.GENERIC, TileType.WOOD), i)
                17, 18 -> HelperCard(listOf(TileType.GENERIC, TileType.LEAF), i)
                19 -> HelperCard(listOf(TileType.GENERIC, TileType.FLOWER), i)
                20 -> HelperCard(listOf(TileType.GENERIC, TileType.FRUIT), i)
                21 -> MasterCard(listOf(TileType.WOOD, TileType.WOOD), i)
                22, 26 -> MasterCard(listOf(TileType.LEAF, TileType.LEAF), i)
                23, 29, 30 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF), i)
                24, 25, 28 -> MasterCard(listOf(TileType.GENERIC), i)
                27 -> MasterCard(listOf(TileType.LEAF, TileType.FRUIT), i)
                31 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF, TileType.FLOWER), i)
                32 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF, TileType.FRUIT), i)
                33 -> MasterCard(listOf(TileType.LEAF, TileType.FLOWER, TileType.FLOWER), i)
                34 -> ParchmentCard(2, ParchmentCardType.MASTER, i)
                35 -> ParchmentCard(2, ParchmentCardType.GROWTH, i)
                36 -> ParchmentCard(2, ParchmentCardType.HELPER, i)
                37 -> ParchmentCard(2, ParchmentCardType.FLOWER, i)
                38 -> ParchmentCard(2, ParchmentCardType.FRUIT, i)
                39 -> ParchmentCard(1, ParchmentCardType.LEAF, i)
                40 -> ParchmentCard(1, ParchmentCardType.WOOD, i)
                in (41..46) -> ToolCard(i)
                else -> error("Invalid index")
            })
        }

        game.currentState.openCards.clear()
        game.currentState.openCards.addAll(listOf(
            GrowthCard(TileType.FLOWER, 5),
            MasterCard(listOf(TileType.GENERIC), 25),
            ParchmentCard(2, ParchmentCardType.FRUIT, 38),
            HelperCard(listOf(TileType.GENERIC, TileType.LEAF), 17)
        ))

        val netPlayers: MutableList<Pair<String, ColorTypeMessage>> = mutableListOf()
        for (i in game.currentState.players.indices) {
            netPlayers.add(Pair(
                game.currentState.players[i].name,
                converter.fromPotColor(game.currentState.players[i].potColor)
            ))
        }

        val netGoals = converter.fromGoalList(game.currentState.goalCards)

        val netCards: MutableList<Pair<CardTypeMessage, Int>> = mutableListOf()
        for (i in game.currentState.drawStack) {
            netCards.add(0, converter.fromCard(i))
        }
        for (i in game.currentState.openCards) {
            netCards.add(converter.fromCard(i))
        }

        val message = StartGameMessage(
            netPlayers,
            netGoals,
            netCards
        )

        if (rootServiceHost.currentGame!!.currentState.players[0] is LocalPlayer) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }

        client?.sendGameActionMessage(message)
    }

    /**
     * Creates bonsai trees on host and guest to test with
     *
     * @param hostState game state of the host game
     * @param guestState game state of the guest game
     */
    private fun initDummyTree(hostState: GameState, guestState: GameState) {
        hostState.players[0].bonsai.grid[-1, -1] = BonsaiTile(TileType.LEAF)
        hostState.players[0].bonsai.grid[0, -1] = BonsaiTile(TileType.WOOD)
        hostState.players[0].bonsai.grid[1, -1] = BonsaiTile(TileType.LEAF)
        hostState.players[0].bonsai.grid[2, -1] = BonsaiTile(TileType.FRUIT)
        hostState.players[0].bonsai.grid[0, -2] = BonsaiTile(TileType.LEAF)
        hostState.players[0].bonsai.grid[1, -2] = BonsaiTile(TileType.LEAF)
        hostState.players[0].bonsai.grid[2, -2] = BonsaiTile(TileType.FLOWER)
        hostState.players[0].bonsai.grid[1, -3] = BonsaiTile(TileType.FLOWER)
        hostState.players[0].bonsai.grid[2, -3] = BonsaiTile(TileType.FRUIT)
        hostState.players[0].bonsai.tileCount[TileType.WOOD] =
            hostState.players[0].bonsai.tileCount.getOrDefault(TileType.WOOD, 0) + 1
        hostState.players[0].bonsai.tileCount[TileType.LEAF] =
            hostState.players[0].bonsai.tileCount.getOrDefault(TileType.LEAF, 0) + 4
        hostState.players[0].bonsai.tileCount[TileType.FLOWER] =
            hostState.players[0].bonsai.tileCount.getOrDefault(TileType.FLOWER, 0) + 2
        hostState.players[0].bonsai.tileCount[TileType.FRUIT] =
            hostState.players[0].bonsai.tileCount.getOrDefault(TileType.FRUIT, 0) + 2

        guestState.players[0].bonsai.grid[-1, -1] = BonsaiTile(TileType.LEAF)
        guestState.players[0].bonsai.grid[0, -1] = BonsaiTile(TileType.WOOD)
        guestState.players[0].bonsai.grid[1, -1] = BonsaiTile(TileType.LEAF)
        guestState.players[0].bonsai.grid[2, -1] = BonsaiTile(TileType.FRUIT)
        guestState.players[0].bonsai.grid[0, -2] = BonsaiTile(TileType.LEAF)
        guestState.players[0].bonsai.grid[1, -2] = BonsaiTile(TileType.LEAF)
        guestState.players[0].bonsai.grid[2, -2] = BonsaiTile(TileType.FLOWER)
        guestState.players[0].bonsai.grid[1, -3] = BonsaiTile(TileType.FLOWER)
        guestState.players[0].bonsai.grid[2, -3] = BonsaiTile(TileType.FRUIT)
        guestState.players[0].bonsai.tileCount[TileType.WOOD] =
            guestState.players[0].bonsai.tileCount.getOrDefault(TileType.WOOD, 0) + 1
        guestState.players[0].bonsai.tileCount[TileType.LEAF] =
            guestState.players[0].bonsai.tileCount.getOrDefault(TileType.LEAF, 0) + 4
        guestState.players[0].bonsai.tileCount[TileType.FLOWER] =
            guestState.players[0].bonsai.tileCount.getOrDefault(TileType.FLOWER, 0) + 2
        guestState.players[0].bonsai.tileCount[TileType.FRUIT] =
            guestState.players[0].bonsai.tileCount.getOrDefault(TileType.FRUIT, 0) + 2

        hostState.players[0].supply.add(BonsaiTile(TileType.WOOD))
        hostState.players[0].supply.add(BonsaiTile(TileType.LEAF))
        hostState.players[0].supply.add(BonsaiTile(TileType.LEAF))
        hostState.players[0].supply.add(BonsaiTile(TileType.FRUIT))

        guestState.players[0].supply.add(BonsaiTile(TileType.WOOD))
        guestState.players[0].supply.add(BonsaiTile(TileType.LEAF))
        guestState.players[0].supply.add(BonsaiTile(TileType.LEAF))
        guestState.players[0].supply.add(BonsaiTile(TileType.FRUIT))

        hostState.players[1].bonsai.grid[0, -1] = BonsaiTile(TileType.LEAF)
        hostState.players[1].bonsai.grid[1, -1] = BonsaiTile(TileType.LEAF)
        hostState.players[1].bonsai.tileCount[TileType.LEAF] =
            hostState.players[1].bonsai.tileCount.getOrDefault(TileType.LEAF, 0) + 2

        guestState.players[1].bonsai.grid[0, -1] = BonsaiTile(TileType.LEAF)
        guestState.players[1].bonsai.grid[1, -1] = BonsaiTile(TileType.LEAF)
        guestState.players[1].bonsai.tileCount[TileType.LEAF] =
            guestState.players[1].bonsai.tileCount.getOrDefault(TileType.LEAF, 0) + 2

        rootServiceHost.playerActionService.allowedTiles[TileType.GENERIC] = 5
        rootServiceGuest.playerActionService.allowedTiles[TileType.GENERIC] = 5
    }

    /**
     * Set up objects used in multiple tests and establish server connections
     */
    @BeforeEach
    fun initConnections() {
        converter = MessageConverter()
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        rootServiceHost.networkService.hostGame("Test Host", 0, null)
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_GUEST)
        val sessionID = rootServiceHost.networkService.client?.sessionID
        assertNotNull(sessionID)
        rootServiceGuest.networkService.joinGame("Test Guest", 0, 1, sessionID)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_INIT)
        Thread.sleep(5000)
    }

    /**
     * Tests hosting and joining games, as well as skipping turns
     */
    @Test
    fun hostAndJoinGameTest() {
        rootServiceHost.networkService.startNewHostedGame(
            hostName = "Test Host",
            players = listOf(
                Triple("Test Host", 0, PotColor.RED),
                Triple("Test Guest", 1, PotColor.BLUE)
            ),
            speed = 1,
            goalCards = listOf(
                GoalColor.RED,
                GoalColor.BROWN,
                GoalColor.GREEN
            )
        )

        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_OPPONENT)

        assertNotNull(rootServiceHost.currentGame)
        assertNotNull(rootServiceGuest.currentGame)
        val hostState = rootServiceHost.currentGame!!.currentState
        val guestState = rootServiceGuest.currentGame!!.currentState

        assertEquals(hostState.players[0].name, guestState.players[0].name)
        assertEquals(hostState.players[1].name, guestState.players[1].name)
        assertEquals(hostState.players[0].potColor, guestState.players[0].potColor)
        assertEquals(hostState.players[1].potColor, guestState.players[1].potColor)

        assertEquals(hostState.currentPlayer, guestState.currentPlayer)
        assertEquals(hostState.goalCards, guestState.goalCards)
        assertEquals(hostState.drawStack, guestState.drawStack)
        assertEquals(hostState.openCards, guestState.openCards)

        rootServiceHost.playerActionService.endTurn()
        rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)

        assertEquals(hostState.players[0].name, guestState.players[0].name)
        assertEquals(hostState.players[1].name, guestState.players[1].name)
        assertEquals(hostState.players[0].potColor, guestState.players[0].potColor)
        assertEquals(hostState.players[1].potColor, guestState.players[1].potColor)

        assertEquals(hostState.currentPlayer, guestState.currentPlayer)
        assertEquals(hostState.goalCards, guestState.goalCards)
        assertEquals(hostState.drawStack, guestState.drawStack)
        assertEquals(hostState.openCards, guestState.openCards)

        rootServiceHost.networkService.disconnect()
        rootServiceGuest.networkService.disconnect()
    }

    /**
     * Tests sending and receiving a cultivate turn message with all parameters set
     */
    @Test
    fun cultivateTurnTest() {
        rootServiceHost.networkService.startDummyGame()

        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_OPPONENT)
        rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)

        assertNotNull(rootServiceHost.currentGame)
        assertNotNull(rootServiceGuest.currentGame)
        val hostState = rootServiceHost.currentGame!!.currentState
        val guestState = rootServiceGuest.currentGame!!.currentState

        initDummyTree(hostState, guestState)

        rootServiceGuest.playerActionService.removeTile(
            guestState.players[0].bonsai.grid[-1, -1]
        )

        for (i in (1..2)) {
            for (j in (1..2)) {
                rootServiceGuest.playerActionService.cultivate(
                    guestState.players[0].supply[0],
                    -i, -j
                )
            }
        }

        guestState.goalCards.find {
            it?.color == GoalColor.ORANGE && it.difficulty == GoalDifficulty.LOW
        }?.let {
            rootServiceGuest.playerActionService.decideGoalClaim(
                it,
                true
            )
        }

        guestState.goalCards.find {
            it?.color == GoalColor.GREEN && it.difficulty == GoalDifficulty.LOW
        }?.let {
            rootServiceGuest.playerActionService.decideGoalClaim(
                it,
                false
            )
        }

        rootServiceGuest.playerActionService.endTurn()
        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)

        assertEquals(
            hostState.players[0].bonsai.grid[-1, -1].type,
            guestState.players[0].bonsai.grid[-1 , -1].type
        )
        assertEquals(
            hostState.players[0].bonsai.grid[-1, -2].type,
            guestState.players[0].bonsai.grid[-1 , -2].type
        )
        assertEquals(
            hostState.players[0].bonsai.grid[-2, -1].type,
            guestState.players[0].bonsai.grid[-2 , -1].type
        )
        assertEquals(
            hostState.players[0].bonsai.grid[-2, -2].type,
            guestState.players[0].bonsai.grid[-2 , -2].type
        )

        assertEquals(hostState.players[0].supply.size, guestState.players[0].supply.size)
        for (i in hostState.players[0].supply.indices) {
            assertEquals(hostState.players[0].supply[i].type , guestState.players[0].supply[i].type)
        }

        assertEquals(hostState.goalCards, guestState.goalCards)
        assertEquals(hostState.players[0].acceptedGoals, guestState.players[0].acceptedGoals)
        assertEquals(hostState.players[0].declinedGoals, guestState.players[0].declinedGoals)

        rootServiceHost.networkService.disconnect()
        rootServiceGuest.networkService.disconnect()
    }

    /**
     * Test sending and receiving three meditate turn messages one after
     * the other. Together they are covering all parameters
     */
    @Test
    fun meditateTurnTest() {
        rootServiceHost.networkService.startDummyGame()

        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_OPPONENT)
        rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)

        assertNotNull(rootServiceHost.currentGame)
        assertNotNull(rootServiceGuest.currentGame)
        val hostState = rootServiceHost.currentGame!!.currentState
        val guestState = rootServiceGuest.currentGame!!.currentState

        initDummyTree(hostState, guestState)

        rootServiceGuest.playerActionService.meditate(
            guestState.openCards[1]
        )
        rootServiceGuest.playerActionService.applyTileChoice(TileType.WOOD)
        rootServiceGuest.playerActionService.applyTileChoice(TileType.FRUIT, true)

        rootServiceGuest.playerActionService.discardTile(guestState.players[0].supply[1])
        rootServiceGuest.playerActionService.discardTile(guestState.players[0].supply[5])

        rootServiceGuest.playerActionService.endTurn()
        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_OPPONENT)

        assertEquals(hostState.players[0].supply.size, guestState.players[0].supply.size)

        assertEquals(
            hostState.players[0].hiddenDeck.size,
            guestState.players[0].hiddenDeck.size
        )
        for (i in hostState.players[0].hiddenDeck.indices) {
            assertEquals(
                hostState.players[0].hiddenDeck[i].javaClass,
                guestState.players[0].hiddenDeck[i].javaClass
            )
        }

        rootServiceHost.playerActionService.removeTile(
            hostState.players[1].bonsai.grid[0, -1]
        )

        rootServiceHost.playerActionService.meditate(
            hostState.openCards[2]
        )

        rootServiceHost.playerActionService.endTurn()
        rootServiceHost.waitForState(ConnectionState.WAITING_FOR_OPPONENT)
        rootServiceGuest.waitForState(ConnectionState.PLAYING_MY_TURN)

        assertEquals(hostState.players[1].supply.size, guestState.players[1].supply.size)
        for (i in hostState.players[1].supply.indices) {
            assertEquals(hostState.players[1].supply[i].type , guestState.players[1].supply[i].type)
        }

        assertEquals(
            hostState.players[1].hiddenDeck.size,
            guestState.players[1].hiddenDeck.size
        )
        for (i in hostState.players[1].hiddenDeck.indices) {
            assertEquals(
                hostState.players[1].hiddenDeck[i].javaClass,
                guestState.players[1].hiddenDeck[i].javaClass
            )
        }

        rootServiceGuest.playerActionService.meditate(
            guestState.openCards[3]
        )

        rootServiceGuest.playerActionService.cultivate(
            guestState.players[0].supply[3],
            -1, -2
        )

        guestState.goalCards.find {
            it?.color == GoalColor.ORANGE && it.difficulty == GoalDifficulty.LOW
        }?.let {
            rootServiceGuest.playerActionService.decideGoalClaim(
                it,
                true
            )
        }

        guestState.goalCards.find {
            it?.color == GoalColor.GREEN && it.difficulty == GoalDifficulty.LOW
        }?.let {
            rootServiceGuest.playerActionService.decideGoalClaim(
                it,
                false
            )
        }

        rootServiceGuest.playerActionService.discardTile(guestState.players[0].supply[1])

        rootServiceGuest.playerActionService.endTurn()
        rootServiceHost.waitForState(ConnectionState.PLAYING_MY_TURN)
        rootServiceGuest.waitForState(ConnectionState.WAITING_FOR_OPPONENT)

        assertEquals(hostState.players[0].supply.size, guestState.players[0].supply.size)

        assertEquals(
            hostState.players[0].hiddenDeck.size,
            guestState.players[0].hiddenDeck.size
        )
        for (i in hostState.players[0].hiddenDeck.indices) {
            assertEquals(
                hostState.players[0].hiddenDeck[i].javaClass,
                guestState.players[0].hiddenDeck[i].javaClass
            )
        }

        assertEquals(
            hostState.players[0].bonsai.grid[-1, -2].type,
            guestState.players[0].bonsai.grid[-1 , -2].type
        )

        rootServiceHost.networkService.disconnect()
        rootServiceGuest.networkService.disconnect()
    }

}
