package gui

import entity.GoalCard
import entity.Player
import service.ConnectionState
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

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
    val lobbyScene = LobbyScene()
    var chooseTileScene = ChooseTileScene(rootService, this, chooseByBoard = false, chooseByCard = false)
    private var playerCount = 0

    private var gameScene = GameScene(rootService, this)

    init {
        // all scenes and the application itself need too
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

    override fun refreshToPromptTileChoice(
        chooseByBoard: Boolean,
        chooseByCard: Boolean,
    ) {
        chooseTileScene = ChooseTileScene(rootService, this, chooseByBoard, chooseByCard)
        this.showMenuScene(chooseTileScene)
    }

    override fun refreshAfterReachGoals(reachedGoals: List<GoalCard>) {
        val claimGoalScene = ClaimGoalScene(rootService, this, reachedGoals)
        this.showMenuScene(claimGoalScene)
    }

    override fun refreshAfterEndGame(scoreList: Map<Player, MutableList<Int>>) {
        val resultScene = ResultScene(scoreList)
        resultScene.mainMenuButton.apply {
            onMouseClicked = {
                this@BonsaiApplication.showMenuScene(mainMenuScene)
                rootService.currentGame = null
            }
        }
        this.showMenuScene(resultScene)
    }

    override fun refreshConnectionState(
        newState: ConnectionState,
        string: String?,
        list: List<String>?,
    ) {
        when (newState) {
            ConnectionState.WAITING_FOR_INIT -> {
                lobbyScene.playerName1Label.text = string!!
                if (!list.isNullOrEmpty()) lobbyScene.playerName2Label.text = list[0]
                if (list != null && list.size > 1) lobbyScene.playerName3Label.text = list[1]
                if (list != null && list.size > 2) lobbyScene.playerName4Label.text = list[2]
                this.showMenuScene(lobbyScene)
                if (list != null) playerCount = 1 + list.size
            }

            ConnectionState.WAITING_FOR_GUEST -> {
                when (playerCount) {
                    1 -> lobbyScene.playerName2Label.text = string!!
                    2 -> lobbyScene.playerName3Label.text = string!!
                    3 -> lobbyScene.playerName4Label.text = string!!
                }
                playerCount++
            }

            else -> {}
        }
    }
}
