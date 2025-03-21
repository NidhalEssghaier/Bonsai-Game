package service

import messages.*
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.*

/**
 * [BoardGameClient] implementation for network communication.
 *
 * @property botStatus 0 if the player is a local player, 1 if the local player is a random bot
 *        and 2 if the player is a smart bot
 * @property gameSpeed delay of bot and network player game actions, is null if hosting
 * @property networkService the [NetworkService] to potentially forward received messages to.
 */
class BonsaiNetworkClient(
    secret: String,
    playerName: String,
    val botStatus: Int,
    val gameSpeed: Int?,
    host: String,
    var networkService: NetworkService
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {

    /** the identifier of this game session; can be null if no session started yet. */
    var sessionID: String? = null

    /** the name of the opponent player; can be null if no message from the opponent received yet */
    private var otherPlayerNames: MutableList<String> = mutableListOf()

    /**
     * Handle a [CreateGameResponse] sent by the server. Will await the guest player when its
     * status is [CreateGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in Bonsai, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @param response [CreateGameResponse] received from the server
     *
     * @throws IllegalStateException if status != success or currently not waiting for a game creation response.
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
        { "unexpected CreateGameResponse" }

        when (response.status) {
            CreateGameResponseStatus.SUCCESS -> {
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUEST)
                sessionID = response.sessionID
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Handle a [JoinGameResponse] sent by the server. Will await the init message when its
     * status is [JoinGameResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in Bonsai, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @param response [JoinGameResponse] received from the server
     *
     * @throws IllegalStateException if status != success or currently not waiting for a join game response.
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
        { "unexpected JoinGameResponse" }

        when (response.status) {
            JoinGameResponseStatus.SUCCESS -> {
                check(response.opponents.size < 4) { "too many players." }
                otherPlayerNames = response.opponents.toMutableList()
                sessionID = response.sessionID
                networkService.updateConnectionState(
                    ConnectionState.WAITING_FOR_INIT,
                    playerName,
                    otherPlayerNames
                )
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Handle a [PlayerJoinedNotification] sent by the server.
     *
     * @param notification [PlayerJoinedNotification] received from the server
     *
     * @throws IllegalStateException if not currently expecting any guests to join.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        check(
            networkService.connectionState == ConnectionState.WAITING_FOR_GUEST ||
            networkService.connectionState == ConnectionState.WAITING_FOR_INIT
        ) { "not awaiting any guests." }

        check(otherPlayerNames.size < 4) { "too many players." }
        otherPlayerNames.add(notification.sender)

        when (networkService.connectionState) {
            ConnectionState.WAITING_FOR_GUEST ->
                networkService.updateConnectionState(
                    ConnectionState.WAITING_FOR_GUEST,
                    notification.sender
                )
            ConnectionState.WAITING_FOR_INIT ->
                networkService.updateConnectionState(
                    ConnectionState.WAITING_FOR_INIT,
                    notification.sender
                )
            else -> error("not awaiting any guests.")
        }
    }

    /**
     * Handle a [GameActionResponse] sent by the server. Does nothing when its
     * status is [GameActionResponseStatus.SUCCESS]. As recovery from network problems is not
     * implemented in Bonsai, the method disconnects from the server and throws an
     * [IllegalStateException] otherwise.
     *
     * @param response [GameActionResponse] received from the server
     *
     * @throws IllegalStateException if no [GameActionResponse] is expected
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        check(networkService.connectionState == ConnectionState.PLAYING_MY_TURN ||
                networkService.connectionState == ConnectionState.WAITING_FOR_OPPONENT)
        { "unexpected GameActionResponse"}

        when (response.status) {
            GameActionResponseStatus.SUCCESS -> {} // do nothing in this case
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * handle a [StartGameMessage] sent by the server
     *
     * @param message [StartGameMessage] received from the server
     * @param sender name of the player who send the message
     *
     * @throws IllegalStateException if the client was not given a game speed
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onStartGameMessageReceived(message: StartGameMessage, sender: String) {
        checkNotNull(gameSpeed)
        networkService.startNewJoinedGame(playerName, botStatus, gameSpeed, message)
    }

    /**
     * handle a [CultivateMessage] sent by the server
     *
     * @param message [CultivateMessage] received from the server
     * @param sender name of the player who send the message
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onCultivateMessageReceived(message: CultivateMessage, sender: String) {
        networkService.receiveCultivateMessage(message, sender)
    }

    /**
     * handle a [MeditateMessage] sent by the server
     *
     * @param message [MeditateMessage] received from the server
     * @param sender name of the player who send the message
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onMeditateMessageReceived(message: MeditateMessage, sender: String) {
        networkService.receiveMeditateMessage(message, sender)
    }

    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        error(message)
    }

}
