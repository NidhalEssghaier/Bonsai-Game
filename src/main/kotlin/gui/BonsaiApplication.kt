package gui

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
            gameScene,
        )

        this.showMenuScene(mainMenuScene)

//        this.showGameScene(gameScene)
//        gameScene.playArea1.toFront()
//        show()
    }

    override fun refreshAfterStartNewGame() {
        this.hideMenuScene()
        this.showGameScene(gameScene)
    }
}
