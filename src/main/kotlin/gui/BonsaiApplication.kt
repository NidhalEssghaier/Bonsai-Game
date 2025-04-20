package gui

import entity.GoalCard
import entity.Player
import service.ConnectionState
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

/** Implementation of the BGW [BoardGameApplication] for the game "Bonsai" */
class BonsaiApplication :
    BoardGameApplication("Bonsai"),
    Refreshable {
    // Central service from which all others are created/accessed
    // also holds the currently active game
    private val rootService = RootService()

    val mainMenuScene = MainMenuScene(rootService, this)
    var localGameScene = LocalGameScene(rootService, this)
    val onlineGameScene = OnlineGameScene(rootService, this)
    val joinGameScene = JoinGameScene(rootService, this)
    private val lobbyScene = LobbyScene()
    var chooseTileScene = ChooseTileScene(rootService, this, chooseByBoard = false, chooseByCard = false)
    private var playerCount = 0

    private val gameScene = GameScene(rootService, this)

    init {
        // bind disconnect & return to main menu function
        lobbyScene.mainMenuButton.onMouseClicked =
            {
                rootService.networkService.disconnect()
                this@BonsaiApplication.showMenuScene(mainMenuScene)
            }
        // all scenes and the application itself need to
        // react to changes done in the service layer
        rootService.addRefreshables(
            this,
            mainMenuScene,
            localGameScene,
            onlineGameScene,
            joinGameScene,
            lobbyScene,
            gameScene,
            chooseTileScene,
        )

        this.showMenuScene(mainMenuScene)

//        show()
    }

    override fun refreshAfterStartNewGame() {
        this.hideMenuScene()
        this.showGameScene(gameScene)
    }

    override fun refreshAfterReachGoals(reachedGoals: List<GoalCard>) {
        val claimGoalScene = ClaimGoalScene(rootService, this, reachedGoals)
        this.showMenuScene(claimGoalScene)
    }

    override fun refreshAfterEndGame(scoreList: Map<Player, MutableList<Int>>) {
        val resultScene = ResultScene(scoreList)
        resultScene.mainMenuButton.onMouseClicked = {
            this@BonsaiApplication.showMenuScene(mainMenuScene)
            rootService.currentGame = null
        }
        this.showMenuScene(resultScene)
    }

    private fun waitingForInitAction(string: String?, list: List<String>?) {
        lobbyScene.playerName1Label.text = string ?: ""
        if (!list.isNullOrEmpty()) lobbyScene.playerName2Label.text = list[0]
        if (list != null && list.size > 1) lobbyScene.playerName3Label.text = list[1]
        if (list != null && list.size > 2) lobbyScene.playerName4Label.text = list[2]
        this.showMenuScene(lobbyScene)
        if (list != null) playerCount = 1 + list.size
    }

    private fun waitingForGuestAction(string: String?) {
        when (playerCount) {
            1 -> lobbyScene.playerName2Label.text = string ?: ""
            2 -> lobbyScene.playerName3Label.text = string ?: ""
            3 -> lobbyScene.playerName4Label.text = string ?: ""
        }
        playerCount++
    }

    override fun refreshConnectionState(
        newState: ConnectionState,
        string: String?,
        list: List<String>?,
    ) {
        when (newState) {
            ConnectionState.WAITING_FOR_INIT -> waitingForInitAction(string, list)

            ConnectionState.WAITING_FOR_GUEST -> waitingForGuestAction(string)

            else -> {}
        }
    }
}
