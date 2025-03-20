package service

import gui.Refreshable
import entity.BonsaiGame
import service.bot.BotService

/**
 * Main class of the service layer for the War card game. Provides access
 * to all other service classes and holds the [currentGame] state for these
 * services to access.
 */
class RootService {

    val gameService = GameService(this)
    val playerActionService = PlayerActionService(this)
    val networkService = NetworkService(this)
    val botService = BotService(this)

    /**
     * The currently active game. Can be `null`, if no game has started yet.
     */
    var currentGame : BonsaiGame? = null

    /**
     * Adds the provided [newRefreshable] to all services connected
     * to this root service
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
        networkService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the provided [newRefreshables] to all services
     * connected to this root service
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }

}
