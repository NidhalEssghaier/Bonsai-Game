package gui
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
class MainMenuScene : MenuScene(1920,1040){
    private val headlineLabel = Label(width =450 ,height = 100, posX= 780, posY = 150, text="Bonsai",
        font = Font(size = 100, fontWeight = Font.FontWeight.BOLD))
    val localGameButton = Button(width= 300, height = 80, posX = 680, posY= 340,text="Local Game", font = Font(35)).apply{
        visual = ColorVisual(256, 107, 62)}
    val hostGameButton = Button(width= 300, height = 80, posX = 1030, posY= 340,text="Host Online Game", font = Font(35)).apply{
        visual = ColorVisual(256, 107, 62)}
    val ResumeGameButton = Button(width= 300, height = 80, posX = 850, posY= 470,text="Resume Game", font = Font(35)).apply{
        visual = ColorVisual(256, 107, 62)}
    val joinGameButton = Button(width= 300, height = 80, posX = 850, posY= 600,text="Join Game", font = Font(35)).apply{
        visual = ColorVisual(256, 107, 62)}
    val quitGameButton = Button(width= 300, height = 80, posX = 850, posY= 730,text="Quit Game", font = Font(35)).apply{
        visual = ColorVisual(256, 107, 62)}

    init {
        addComponents(headlineLabel, localGameButton, hostGameButton, ResumeGameButton, joinGameButton, quitGameButton)
    }
}