package gui

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * [MenuScene] that gets displayed when starting the [BonsaiApplication]. Allows selecting the game mode, loading a save
 * game or exiting the application.
 *
 * @param rootService The [RootService] to access the other services
 * @param application The running application
 */
class MainMenuScene(
    rootService: RootService,
    application: BonsaiApplication,
) : MenuScene(1920, 1080),
    Refreshable {
    private val headlineLabel =
        Label(
            width = 450,
            height = 100,
            posX = 780,
            posY = 150,
            text = "Bonsai",
            font = Font(size = 100, fontWeight = Font.FontWeight.BOLD),
        )
    private val localGameButton =
        Button(width = 300, height = 80, posX = 680, posY = 340, text = "Local Game", font = Font(35))
            .apply {
                visual = ColorVisual(256, 107, 62)
            }.apply {
                onMouseClicked = { application.showMenuScene(application.localGameScene) }
            }
    private val hostGameButton =
        Button(width = 300, height = 80, posX = 1030, posY = 340, text = "Host Online Game", font = Font(35))
            .apply {
                visual = ColorVisual(256, 107, 62)
            }.apply {
                onMouseClicked = { application.showMenuScene(application.onlineGameScene) }
            }
    private val resumeGameButton =
        Button(width = 300, height = 80, posX = 850, posY = 470, text = "Resume Game", font = Font(35))
            .apply {
                visual = ColorVisual(256, 107, 62)
            }.apply {
                onMouseClicked = { rootService.gameService.loadGame() }
            }
    private val joinGameButton =
        Button(width = 300, height = 80, posX = 850, posY = 600, text = "Join Game", font = Font(35))
            .apply {
                visual = ColorVisual(256, 107, 62)
            }.apply {
                onMouseClicked = { application.showMenuScene(application.joinGameScene) }
            }
    private val quitGameButton =
        Button(width = 300, height = 80, posX = 850, posY = 730, text = "Quit Game", font = Font(35))
            .apply {
                visual = ColorVisual(256, 107, 62)
            }.apply {
                onMouseClicked = { application.exit() }
            }

    init {
        addComponents(headlineLabel, localGameButton, hostGameButton, resumeGameButton, joinGameButton, quitGameButton)
    }
}
