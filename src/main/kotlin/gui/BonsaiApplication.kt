package gui

import entity.GoalCard
import entity.Player
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

class BonsaiApplication :
    BoardGameApplication("Bonsai"),
    Refreshable {
    // Central service from which all others are created/accessed
    // also holds the currently active game
    private val rootService = RootService()

    val mainMenuScene = MainMenuScene(rootService, this)
    val localGameScene = LocalGameScene(rootService, this)
    val onlineGameScene = OnlineGameScene(rootService, this)
    val joinGameScene = JoinGameScene(rootService, this)
    var chooseTileScene = ChooseTileScene(rootService, this, false, false)

    var gameScene = GameScene(rootService, this)

    init {
        // all scenes and the application itself need too
        // react to changes done in the service layer
        rootService.addRefreshables(
            this,
            mainMenuScene,
            localGameScene,
            onlineGameScene,
            joinGameScene,
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

    override fun refreshAfterEndGame(scoreList: Map<Player, List<Int>>) {
        val resultScene = ResultScene(scoreList)
        resultScene.mainMenuButton.apply {
            this@BonsaiApplication.showMenuScene(mainMenuScene)
            rootService.currentGame = null
        }

        this.showMenuScene(resultScene)
    }
}
