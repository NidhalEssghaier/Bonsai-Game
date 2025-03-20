package service

import tools.aqua.bgw.net.common.response.*
import messages.*

/**
 * Enum to distinguish the different states that occur in networked games, in particular
 * during connection and game setup. Used in [NetworkService].
 */
enum class ConnectionState {

    /**
    * no connection active. initial state at the start of the program and after
    * an active connection was closed.
    */
    DISCONNECTED,

    /**
     * connected to server, but no game started or joined yet
     */
    CONNECTED,

    /**
     * hostGame request sent to server. waiting for confirmation (i.e. [CreateGameResponse])
     */
    WAITING_FOR_HOST_CONFIRMATION,

    /**
     * joinGame request sent to server. waiting for confirmation (i.e. [JoinGameResponse])
     */
    WAITING_FOR_JOIN_CONFIRMATION,

    /**
     * host game started. waiting for guest players to join
     */
    WAITING_FOR_GUEST,

    /**
     * joined game as a guest and waiting for host to send init message (i.e. [StartGameMessage])
     */
    WAITING_FOR_INIT,

    /**
     * Game is running. It is my turn.
     */
    PLAYING_MY_TURN,

    /**
     * Game is running. Waiting for all opponents to send their turns.
     */
    WAITING_FOR_OPPONENT

}
