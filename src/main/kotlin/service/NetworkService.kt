package service

import entity.*
import gui.Refreshable
import helper.push
import messages.*
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games. Bridges between the [BonsaiNetworkClient] and the other services.
 */
class NetworkService(
    private val rootService: RootService,
) : AbstractRefreshingService() {

    /** Network client. Nullable for offline games. */
    var client: BonsaiNetworkClient? = null

    /**
     * current state of the connection in a network game.
     */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /**
     * Message builder to create [CultivateMessage] or [MeditateMessage].
     */
    val messageBuilder = MessageBuilder()

    /** Used to convert between entity classes and ntf classes. */
    private val converter: MessageConverter = MessageConverter()

    /**
     * Connects to server and creates a new game session.
     *
     * @param name Player name.
     * @param botStatus 0 if the player is a local player, 2 if the player is a random bot
     *                  and 3 if the player is a smart bot
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun hostGame(
        name: String,
        botStatus: Int,
        sessionID: String?,
    ) {
        if (!connect(name, botStatus)) {
            error("Connection failed")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        if (sessionID.isNullOrBlank()) {
            client?.createGame(GAME_ID, "Welcome!")
        } else {
            client?.createGame(GAME_ID, sessionID, "Welcome!")
        }
        updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION, client?.sessionID)
    }

    /**
     * Connects to server and joins a game session as guest player.
     *
     * @param name Player name.
     * @param botStatus 0 if the player is a local player, 2 if the player is a random bot
     *                  and 3 if the player is a smart bot
     * @param gameSpeed delay of bot and network player game actions
     * @param sessionID identifier of the joined session (as defined by host on create)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun joinGame(
        name: String,
        botStatus: Int,
        gameSpeed: Int,
        sessionID: String,
    ) {
        if (!connect(name, botStatus, gameSpeed)) {
            error("Connection failed")
        }
        updateConnectionState(ConnectionState.CONNECTED)

        client?.joinGame(sessionID, "Hello!")
        updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }

    /**
     * set up the game using [GameService.startNewGame] and send the game init message
     * to the guest player. [connectionState] needs to be [ConnectionState.WAITING_FOR_GUEST].
     *
     * @param hostName name of the host player
     * @param players list of the players and their type as number
     * @param speed speed of the delay on network player and bot actions
     * @param goalCards list of selected goal cards
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.WAITING_FOR_GUEST]
     *         or if the game didn't start.
     */
    fun startNewHostedGame(
        hostName: String,
        players: List<Triple<String, Int, PotColor>>,
        speed: Int,
        goalCards: List<GoalColor>,
    ) {
        check(
            connectionState == ConnectionState.WAITING_FOR_GUEST,
        ) { "unexpected game start attempt" }

        rootService.gameService.startNewGame(players, speed, goalCards)
        val game = rootService.currentGame
        checkNotNull(game) { "game should not be null right after starting it." }

        val netPlayers: MutableList<Pair<String, ColorTypeMessage>> = mutableListOf()
        for (i in game.currentState.players.indices) {
            netPlayers.add(
                Pair(
                    game.currentState.players[i].name,
                    converter.fromPotColor(game.currentState.players[i].potColor),
                ),
            )
        }

        val netGoals = converter.fromGoalList(game.currentState.goalCards)

        val netCards: MutableList<Pair<CardTypeMessage, Int>> = mutableListOf()
        for (i in game.currentState.drawStack) {
            netCards.add(0, converter.fromCard(i))
        }
        for (i in game.currentState.openCards) {
            netCards.add(converter.fromCard(i))
        }

        val message =
            StartGameMessage(
                netPlayers,
                netGoals,
                netCards,
            )

        if (game.currentState.players[0].name == hostName) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }

        client?.sendGameActionMessage(message)
    }

    /**
     * Initializes the entity structure with the data given by the
     * [StartGameMessage] sent by the host.
     * [connectionState] needs to be [ConnectionState.WAITING_FOR_INIT].
     * This method should be called from the [BonsaiNetworkClient] when
     * the host sends the init message.
     * See [BonsaiNetworkClient.onStartGameMessageReceived].
     *
     * @param playerName name of the local player
     * @param botStatus 0 if the player is a local player, 2 if the player is a random bot
     *                  and 3 if the player is a smart bot
     * @param gameSpeed delay of bot and network player game actions
     * @param message received from the server
     *
     * @throws IllegalStateException if not currently waiting for an init message
     */
    fun startNewJoinedGame(
        playerName: String,
        botStatus: Int,
        gameSpeed: Int,
        message: StartGameMessage,
    ) {
        check(connectionState == ConnectionState.WAITING_FOR_INIT) { "not waiting for game init message. " }

        val players: MutableList<Triple<String, Int, PotColor>> = mutableListOf()
        for (i in message.orderedPlayerNames.indices) {
            if (message.orderedPlayerNames[i].first == playerName) {
                players.add(
                    Triple(
                        message.orderedPlayerNames[i].first,
                        botStatus,
                        converter.toPotColor(message.orderedPlayerNames[i].second),
                    ),
                )
            } else {
                players.add(
                    Triple(
                        message.orderedPlayerNames[i].first,
                        1,
                        converter.toPotColor(message.orderedPlayerNames[i].second),
                    ),
                )
            }
        }

        val goalCards: MutableList<GoalColor> = mutableListOf()
        for (i in message.chosenGoalTiles) {
            goalCards.add(converter.toGoalColor(i))
        }

        try {
            rootService.gameService.startNewGame(
                players,
                gameSpeed,
                goalCards,
            )
            val game = rootService.currentGame
            checkNotNull(game) { "game should not be null right after starting it." }

            game.currentState.drawStack.clear()
            for (i in (0..message.orderedCards.size - 5)) {
                game.currentState.drawStack.push(converter.toCard(message.orderedCards[i]))
            }

            game.currentState.openCards.clear()
            for (i in (message.orderedCards.size - 4..<message.orderedCards.size)) {
                game.currentState.openCards.add(converter.toCard(message.orderedCards[i]))
            }

            onAllRefreshables { refreshAfterStartNewGame() }
        } catch (e: IllegalArgumentException) {
            disconnect()
            error("Caught an IllegalArgumentException: ${e.message}")
        } catch (e: IllegalStateException) {
            disconnect()
            error("Caught an IllegalStateException: ${e.message}")
        }

        val game = rootService.currentGame
        checkNotNull(game) { "game should not be null right after starting it." }

        if (game.currentState.players[0].name == playerName) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
        }
    }

    /**
     * Send either a [CultivateMessage] or [MeditateMessage] to the server
     *
     * @throws IllegalStateException if it's the opponents turn, or the message is invalid
     */
    fun sendTurn() {
        val game = rootService.currentGame
        checkNotNull(game) { "no active game" }

        val nextPlayer = (game.currentState.currentPlayer + 1) % game.currentState.players.size
        if (connectionState != ConnectionState.PLAYING_MY_TURN) {
            if (game.currentState.players[nextPlayer] !is NetworkPlayer) {
                updateConnectionState(ConnectionState.PLAYING_MY_TURN)
            } else {
                updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
            }
            return
        }

        try {
            val build = messageBuilder.build()
            val message =
                when {
                    build.first == null && build.second != null -> build.second
                    build.second == null && build.first != null -> build.first
                    else -> null
                }

            checkNotNull(message) { "no message created" }

            updateConnectionState(ConnectionState.WAITING_FOR_OPPONENT)
            client?.sendGameActionMessage(message)
            messageBuilder.reset()
        } catch (e: IllegalStateException) {
            disconnect()
            error("Caught an IllegalStateException: ${e.message}")
        }
    }

    /**
     * Plays a cultivate turn with actions taken from the [CultivateMessage]
     * sent by the active player. [connectionState] needs to be
     * [ConnectionState.WAITING_FOR_OPPONENT].
     *
     * @param message received from the server
     * @param sender name of the sender of the message
     *
     * @throws IllegalStateException if currently not waiting for a game action message
     *                               or if it's not the senders turn
     */
    fun receiveCultivateMessage(
        message: CultivateMessage,
        sender: String,
    ) {
        check(
            connectionState == ConnectionState.WAITING_FOR_OPPONENT,
        ) { "unexpected game action message" }

        val game = rootService.currentGame
        checkNotNull(game) { "no active game" }

        check(
            game.currentState.players[game.currentState.currentPlayer].name == sender,
        ) { "not $sender's turn" }

        try {
            // remove tiles
            receiveRemoveTiles(game, message.removedTilesAxialCoordinates)

            // played tiles
            receivePlayTiles(game, message.playedTiles)

            // decide goals
            receiveDecideGoals(game, message.claimedGoals, message.renouncedGoals)

            // end turn
            messageBuilder.reset()
            val timer = Timer()
            timer.schedule(timerTask { rootService.playerActionService.endTurn() }, 10000)
        } catch (e: IllegalArgumentException) {
            disconnect()
            error("Caught an IllegalArgumentException: ${e.message}")
        } catch (e: IllegalStateException) {
            disconnect()
            error("Caught an IllegalStateException: ${e.message}")
        }
    }

    /**
     * Plays a meditate turn with actions taken from the [MeditateMessage]
     * sent by the active player. [connectionState] needs to be
     * [ConnectionState.WAITING_FOR_OPPONENT].
     *
     * @param message received from the server
     * @param sender name of the sender of the message
     *
     * @throws IllegalStateException if currently not waiting for a game action message
     *                               or if it's not the senders turn
     */
    fun receiveMeditateMessage(
        message: MeditateMessage,
        sender: String,
    ) {
        check(
            connectionState == ConnectionState.WAITING_FOR_OPPONENT,
        ) { "unexpected game action message" }

        val game = rootService.currentGame
        checkNotNull(game) { "no active game" }

        check(
            game.currentState.players[game.currentState.currentPlayer].name == sender,
        ) { "not $sender's turn" }

        try {
            // remove tiles
            receiveRemoveTiles(game, message.removedTilesAxialCoordinates)

            // draw card
            val drawnCard = game.currentState.openCards[message.chosenCardPosition]
            rootService.playerActionService.meditate(
                game.currentState.openCards[message.chosenCardPosition],
            )

            // draw tiles
            val drawnTiles: MutableList<TileTypeMessage> = message.drawnTiles.toMutableList()

            // tiles drawn from open cards
            if (drawnTiles.isNotEmpty()) {
                when (message.chosenCardPosition) {
                    1 -> rootService.playerActionService.applyTileChoice(
                            converter.toTileType(drawnTiles.removeFirst()),
                         )
                    2 -> drawnTiles.removeAll(listOf(TileTypeMessage.WOOD, TileTypeMessage.FLOWER))
                    3 -> drawnTiles.removeAll(listOf(TileTypeMessage.LEAF, TileTypeMessage.FRUIT))
                }
            }

            // tiles drawn from master cards
            if (drawnTiles.isNotEmpty() &&
                drawnCard is MasterCard &&
                drawnCard.id in listOf(24, 25, 28)
            ) {
                rootService.playerActionService.applyTileChoice(
                    converter.toTileType(drawnTiles.removeFirst()),
                    true,
                )
            }

            // played tiles
            receivePlayTiles(game, message.playedTiles)

            // decide goals
            receiveDecideGoals(game, message.claimedGoals, message.renouncedGoals)

            // discard tiles
            receiveDiscardTiles(game, message.discardedTiles)

            // end turn
            messageBuilder.reset()
            val timer = Timer()
            timer.schedule(timerTask { rootService.playerActionService.endTurn() }, 10000)
        } catch (e: IllegalArgumentException) {
            disconnect()
            error("Caught an IllegalArgumentException: ${e.message}")
        } catch (e: IllegalStateException) {
            disconnect()
            error("Caught an IllegalStateException: ${e.message}")
        }
    }

    private fun receiveRemoveTiles(
        game: BonsaiGame,
        removedTilesAxialCoordinates: List<Pair<Int, Int>>
    ) {
        if (removedTilesAxialCoordinates.isNotEmpty()) {
            for (i in removedTilesAxialCoordinates.indices) {
                rootService.playerActionService.removeTile(
                    game.currentState.players[game.currentState.currentPlayer]
                        .bonsai.grid[
                        removedTilesAxialCoordinates[i].first,
                        removedTilesAxialCoordinates[i].second,
                    ],
                )
            }
        }
    }

    private fun receivePlayTiles(
        game: BonsaiGame,
        playedTiles: List<Pair<TileTypeMessage, Pair<Int, Int>>>
    ) {
        if (playedTiles.isNotEmpty()) {
            for (i in playedTiles.indices) {
                game.currentState.players[
                    game.currentState.currentPlayer,
                ].supply.find { it.type == converter.toTileType(playedTiles[i].first) }?.let {
                    rootService.playerActionService.cultivate(
                        it,
                        playedTiles[i].second.first,
                        playedTiles[i].second.second,
                    )
                }
            }
        }
    }

    private fun receiveDecideGoals(
        game: BonsaiGame,
        claimedGoals: List<Pair<GoalTileTypeMessage, Int>>,
        renouncedGoals: List<Pair<GoalTileTypeMessage, Int>>
    ) {
        if (claimedGoals.isNotEmpty()) {
            for (i in claimedGoals.indices) {
                game.currentState.goalCards.find {
                    it?.color == converter.toGoal(claimedGoals[i]).color &&
                    it.difficulty == converter.toGoal(claimedGoals[i]).difficulty
                }?.let {
                    rootService.playerActionService.decideGoalClaim(
                        it,
                        true,
                    )
                }
            }
        }
        if (renouncedGoals.isNotEmpty()) {
            for (i in renouncedGoals.indices) {
                game.currentState.goalCards.find {
                    it?.color == converter.toGoal(renouncedGoals[i]).color &&
                    it.difficulty == converter.toGoal(renouncedGoals[i]).difficulty
                }?.let {
                    rootService.playerActionService.decideGoalClaim(
                        it,
                        false,
                    )
                }
            }
        }
    }

    private fun receiveDiscardTiles(
        game: BonsaiGame,
        discardedTiles: List<TileTypeMessage>,
    ) {
        if (discardedTiles.isNotEmpty()) {
            for (i in discardedTiles.indices) {
                game.currentState.players[game.currentState.currentPlayer]
                    .supply.find { it.type == converter.toTileType(discardedTiles[i]) }
                    ?.let {
                        rootService.playerActionService.discardTile(it)
                    }
            }
        }
    }

    /**
     * Updates the [connectionState] to [newState] and notifies
     * all refreshables via [Refreshable.refreshConnectionState]
     */
    fun updateConnectionState(
        newState: ConnectionState,
        string: String? = null,
        list: List<String>? = null,
    ) {
        this.connectionState = newState
        onAllRefreshables {
            refreshConnectionState(newState, string, list)
        }
    }

    /**
     * Disconnects the [client] from the server, nulls it and updates the
     * [connectionState] to [ConnectionState.DISCONNECTED]. Can safely be called
     * even if no connection is currently active.
     */
    fun disconnect() {
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * Connects to server, sets the [NetworkService.client] if successful and returns `true` on success.
     *
     * @param name Player name. Must not be blank
     * @param botStatus 0 if the player is a local player, 2 if the player is a random bot
     *                  and 3 if the player is a smart bot
     * @param gameSpeed delay of bot and network player game actions
     *
     * @throws IllegalArgumentException if name is blank or if botStatus is out of bounds
     * @throws IllegalStateException if already connected to another game
     */
    private fun connect(
        name: String,
        botStatus: Int,
        gameSpeed: Int? = null,
    ): Boolean {
        check(connectionState == ConnectionState.DISCONNECTED && client == null) { "already connected to another game" }
        require(name.isNotBlank()) { "player name must be given" }
        require(botStatus in (0..3)) { "invalid player type" }

        val newClient =
            BonsaiNetworkClient(
                playerName = name,
                botStatus = botStatus,
                gameSpeed = gameSpeed,
                host = SERVER_ADDRESS,
                secret = SERVER_SECRET,
                networkService = this,
            )

        return if (newClient.connect()) {
            this.client = newClient
            true
        } else {
            false
        }
    }

    /** Stores static information needed to connect to the server */
    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** Secret of the BGW net server hosted for SoPra participants */
        const val SERVER_SECRET = "baum25"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Bonsai"
    }

}
