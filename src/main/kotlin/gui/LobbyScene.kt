package gui
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.components.uicomponents.TextField

class LobbyScene : MenuScene(1920,1040) {
    private val headlineLabel = Label(
        width = 570, height = 100, posX = 700, posY = 20, text = "Lobby",
        font = Font(size = 100, fontWeight = Font.FontWeight.BOLD)
    )
    val mainMenuButton = Button(width= 300, height = 80, posX = 1550, posY= 160,text="Main Menu", font = Font(35)).apply{
        visual = ColorVisual(256, 107, 62)}

    // GoalTiel Labels und Button um die 3 GoalTiles Farben auszusuchen

    var playerName1Label : Label = Label(width= 500, height = 80, posX = 850, posY = 295,
        text = "", font = Font(30), visual = ColorVisual(255,113,113))
    val playerName2Label : Label = Label(width= 500, height = 80, posX = 850, posY = 445,
        text = "", font = Font(30), visual = ColorVisual(255,113,113))
    val playerName3Label : Label = Label(width= 500, height = 80, posX = 850, posY = 595,
        text = "", font = Font(30), visual = ColorVisual(255,113,113))
    val playerName4Label : Label = Label(width= 500, height = 80, posX = 850, posY = 745,
        text = "", font = Font(30), visual = ColorVisual(255,113,113))



    init {

        addComponents(
            headlineLabel,mainMenuButton,
            playerName1Label, playerName2Label, playerName3Label, playerName4Label,
        )
    }


}