package gui

import tools.aqua.bgw.core.BoardGameApplication

class BonsaiApplication : BoardGameApplication("SoPra Game") {
    private val gameScene = GameScene()

    init {
        this.showGameScene(gameScene)
//        gameScene.playArea1.toFront()
//        show()
    }
}
