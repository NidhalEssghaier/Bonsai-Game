package gui

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * [MenuScene] that gets displayed when the player wants to join an online game. To join, a player name and the
 * Session-ID can be entered.
 *
 * @property rootService The [RootService] to access the other services
 * @property application The running application
 */
class JoinGameScene(
    rootService: RootService,
    application: BonsaiApplication,
) : MenuScene(1920, 1080),
    Refreshable {
    private var localplayerMode = 0
    private val headlineLabel =
        Label(
            width = 550,
            height = 100,
            posX = 720,
            posY = 120,
            text = "Join Game",
            font = Font(size = 100, fontWeight = Font.FontWeight.BOLD),
        )
    private val playerNameLabel =
        Label(width = 300, height = 50, posX = 850, posY = 250, text = "Player Name", font = Font(20))
    private val sessionIDLabel =
        Label(width = 300, height = 50, posX = 850, posY = 410, text = "Session ID", font = Font(20))
    private val playerNameInput: TextField =
        TextField(
            width = 300,
            height = 80,
            posX = 850,
            posY = 295,
            text = "Masahiko Kimura",
            font = Font(30),
        )
    private val sessionIDInput: TextField =
        TextField(
            width = 300,
            height = 80,
            posX = 850,
            posY = 455,
            font = Font(30),
        )
    private val joinGameButton =
        Button(width = 300, height = 80, posX = 850, posY = 600, text = "Join Game", font = Font(35)).apply {
            visual = ColorVisual(256, 107, 62)
            onMouseClicked = {
                if (playerNameInput.text.isBlank()) throw IllegalStateException("Gib einen Namen ein")
                if (sessionIDInput.text.isBlank()) throw IllegalStateException("Gib eine gültige Session Id an")
                rootService.networkService.joinGame(playerNameInput.text, localplayerMode, 10, sessionIDInput.text)
            }
        }
    private val mainMenuButton =
        Button(width = 300, height = 80, posX = 850, posY = 860, text = "Main Menu", font = Font(35))
            .apply {
                visual = ColorVisual(256, 107, 62)
            }.apply {
                onMouseClicked = { application.showMenuScene(application.mainMenuScene) }
            }
    private lateinit var player1HardBox: CheckBox

    private val player1EasyBox =
        CheckBox(posX = 1170, posY = 300, text = "Easy Bot").apply {
            onCheckedChanged = {
                if (this.isChecked) {
                    localplayerMode = 2
                    player1HardBox.isDisabled = true
                } else {
                    localplayerMode = 0
                    player1HardBox.isDisabled = false
                }
            }
        }

    init {
        player1HardBox =
            CheckBox(posX = 1170, posY = 345, text = "Hard Bot").apply {
                onCheckedChanged = {
                    if (this.isChecked) {
                        localplayerMode = 3
                        player1EasyBox.isDisabled = true
                    } else {
                        localplayerMode = 0
                        player1EasyBox.isDisabled = false
                    }
                }
            }
        addComponents(
            headlineLabel,
            joinGameButton,
            mainMenuButton,
            sessionIDLabel,
            playerNameLabel,
            playerNameInput,
            sessionIDInput,
            player1HardBox,
            player1EasyBox,
        )
    }
}
