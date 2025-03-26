package gui

import entity.GoalColor
import entity.PotColor
import gui.utility.*
import service.ConnectionState
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual
import kotlin.random.Random

/**
 * [MenuScene] that gets displayed to configure a new online game including the goals, player order and player colors.
 *
 * @param rootService The [RootService] to access the other services
 * @param application The running application
 */
class OnlineGameScene(
    private val rootService: RootService,
    private val application: BonsaiApplication,
) : MenuScene(1920, 1080),
    Refreshable {
    private val itemImageLoader = ItemImageLoader()
    private var countGoals = 0
    private var localplayerMode = 0
    private var gameSpeed = 10
    private var player2Mode = 0
    private var player3Mode = 0
    private var player4Mode = 0
    private var countPlayer = 0
    private var potColor1 = PotColor.BLUE
    private var potColor2 = PotColor.GRAY
    private var potColor3 = PotColor.PURPLE
    private var potColor4 = PotColor.RED
    private var localPlayer = ""
    private val listGoalColor: MutableList<GoalColor> = mutableListOf()
    private val headlineLabel =
        Label(
            width = 570,
            height = 100,
            posX = 700,
            posY = 20,
            text = "Host Game",
            font = Font(size = 100, fontWeight = Font.FontWeight.BOLD),
        )
    private val lobbyLabel =
        Label(
            width = 300,
            height = 100,
            posX = 2,
            posY = 2,
            text = "Session ID:",
            font = Font(size = 30, fontWeight = Font.FontWeight.BOLD),
        )
    private val sessionNrLabel =
        Label(
            width = 300,
            height = 100,
            posX = 2,
            posY = 50,
            text = "No number set by now",
            font = Font(size = 30, fontWeight = Font.FontWeight.BOLD),
        )
    private val mainMenuButton =
        Button(width = 300, height = 80, posX = 1550, posY = 160, text = "Main Menu", font = Font(35))
            .apply {
                visual = ColorVisual(256, 107, 62)
            }.apply {
                onMouseClicked = {
                    rootService.networkService.disconnect()
                    application.showMenuScene(application.mainMenuScene)
                }
            }
    private val startGameButton =
        Button(width = 300, height = 80, posX = 1550, posY = 460, text = "StartGame", font = Font(35)).apply {
            visual = ColorVisual(256, 107, 62)
            onMouseClicked = {
                if (countPlayer < 2) throw IllegalStateException("More Players Needet")
                if (countGoals != 3) throw IllegalStateException("Select Goals")
                val playerList: MutableList<Triple<String, Int, PotColor>> = mutableListOf()
                var helpPotColor = potColor1
                var helpPlayermode = 1
                if (playerName1Label.text == playerAddField.text) helpPlayermode = localplayerMode
                playerList.add(Triple(playerName1Label.text, helpPlayermode, helpPotColor))
                helpPlayermode = 1

                helpPotColor = potColor2
                if (playerName2Label.text == playerAddField.text) helpPlayermode = localplayerMode
                playerList.add(Triple(playerName2Label.text, player2Mode, helpPotColor))
                helpPlayermode = 1

                helpPotColor = potColor3
                if (playerName3Label.text == playerAddField.text) helpPlayermode = localplayerMode
                if (playerName3Label.text.isNotBlank()) {
                    playerList.add(
                        Triple(
                            playerName3Label.text,
                            player3Mode,
                            helpPotColor,
                        ),
                    )
                }
                helpPlayermode = 1

                helpPotColor = potColor4
                if (playerName2Label.text == playerAddField.text) helpPlayermode = localplayerMode
                if (playerName4Label.text.isNotBlank()) {
                    playerList.add(
                        Triple(
                            playerName4Label.text,
                            player4Mode,
                            helpPotColor,
                        ),
                    )
                }
                rootService.networkService.startNewHostedGame(playerAddField.text, playerList, gameSpeed, listGoalColor)
            }
        }

    // GoalTiel Labels und Button um die 3 GoalTiles Farben auszusuchen
    private val chooseGoal =
        Label(
            width = 400,
            height = 40,
            posX = 50,
            posY = 180,
            text = "Choose 3 Goal Tiles",
            font = Font(size = 30, fontWeight = Font.FontWeight.BOLD),
        )
    private val bujinGoal =
        Label(
            width = 400,
            height = 40,
            posX = 50,
            posY = 220,
            text = "Bujin-ji Style",
            font = Font(size = 30),
        )
    private val chokkanGoal =
        Label(
            width = 400,
            height = 40,
            posX = 50,
            posY = 380,
            text = "Chokkan Style",
            font = Font(size = 30),
        )
    private val moyogiGoal =
        Label(
            width = 400,
            height = 40,
            posX = 50,
            posY = 540,
            text = "Moyogi Style",
            font = Font(size = 30),
        )
    private val shakanGoal =
        Label(
            width = 400,
            height = 40,
            posX = 50,
            posY = 700,
            text = "Shakan Style",
            font = Font(size = 30),
        )
    private val kengaiGoal =
        Label(
            width = 400,
            height = 40,
            posX = 50,
            posY = 860,
            text = "Kengai Style",
            font = Font(size = 30),
        )
    private val bunjayJpg =
        Label(
            width = 225,
            height = 100,
            visual = itemImageLoader.imageFor("goal_cards/goal_log_hard.jpg", 179, 80),
            posX = 130,
            posY = 270,
        )
    private val chokkanJpg =
        Label(
            width = 225,
            height = 100,
            visual = itemImageLoader.imageFor("goal_cards/goal_fruit_hard.jpg", 179, 80),
            posX = 130,
            posY = 430,
        )
    private val moyogiJpg =
        Label(
            width = 225,
            height = 100,
            visual = itemImageLoader.imageFor("goal_cards/goal_leaf_hard.jpg", 179, 80),
            posX = 130,
            posY = 590,
        )
    private val shakanJpg =
        Label(
            width = 225,
            height = 100,
            visual = itemImageLoader.imageFor("goal_cards/goal_flower_protruding_hard.jpg", 179, 80),
            posX = 130,
            posY = 750,
        )
    private val kengaiJpg =
        Label(
            width = 225,
            height = 100,
            visual = itemImageLoader.imageFor("goal_cards/goal_universal_protruding_hard.jpg", 179, 80),
            posX = 130,
            posY = 910,
        )

    private val bunjayBox = CheckBox(posX = 370, posY = 300).apply { onCheckedChanged = { (checkBoxKlicked(this)) } }
    private val chookanBox = CheckBox(posX = 370, posY = 460).apply { onCheckedChanged = { (checkBoxKlicked(this)) } }
    private val moyogiBox = CheckBox(posX = 370, posY = 620).apply { onCheckedChanged = { (checkBoxKlicked(this)) } }
    private val shakanBox = CheckBox(posX = 370, posY = 780).apply { onCheckedChanged = { (checkBoxKlicked(this)) } }
    private val kenegaiBox = CheckBox(posX = 370, posY = 940).apply { onCheckedChanged = { (checkBoxKlicked(this)) } }
    val goalRandomButton =
        Button(
            width = 80,
            height = 30,
            posX = 485,
            posY = 340,
            text = "Random",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                val list: MutableList<CheckBox> = mutableListOf()
                bunjayBox.isChecked = false
                chookanBox.isChecked = false
                moyogiBox.isChecked = false
                shakanBox.isChecked = false
                kenegaiBox.isChecked = false

                bunjayBox.isDisabled = false
                chookanBox.isDisabled = false
                moyogiBox.isDisabled = false
                shakanBox.isDisabled = false
                kenegaiBox.isDisabled = false
                countGoals = 0
                listGoalColor.clear()
                list.add(bunjayBox)
                list.add(chookanBox)
                list.add(moyogiBox)
                list.add(shakanBox)
                list.add(kenegaiBox)
                list.shuffle()
                var help = list.removeLast()
                help.isChecked = true
                help = list.removeLast()
                help.isChecked = true
                help = list.removeLast()
                help.isChecked = true
            }
        }
    private val playerName1Label: Label =
        Label(
            width = 500,
            height = 80,
            posX = 850,
            posY = 295,
            text = "",
            font = Font(30),
            visual = ColorVisual(255, 113, 113),
        )
    private val playerName2Label: Label =
        Label(
            width = 500,
            height = 80,
            posX = 850,
            posY = 445,
            text = "",
            font = Font(30),
            visual = ColorVisual(255, 113, 113),
        )
    private val playerName3Label: Label =
        Label(
            width = 500,
            height = 80,
            posX = 850,
            posY = 595,
            text = "",
            font = Font(30),
            visual = ColorVisual(255, 113, 113),
        )
    private val playerName4Label: Label =
        Label(
            width = 500,
            height = 80,
            posX = 850,
            posY = 745,
            text = "",
            font = Font(30),
            visual = ColorVisual(255, 113, 113),
        )
    private val playerAddField: TextField =
        TextField(
            width = 500,
            height = 80,
            posX = 850,
            posY = 925,
            text = "Masahiko Kimura",
            font = Font(30),
            visual = ColorVisual(255, 113, 113),
        )
    private val addPlayerButton =
        Button(
            width = 150,
            height = 80,
            posX = 1360,
            posY = 925,
            text = "Add",
            font = Font(50),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                localPlayer = playerAddField.text
                playerName1Label.text = localPlayer
                this.isVisible = false
                playerAddField.isVisible = false
                player1EasyBox.isDisabled = true
                player1HardBox.isDisabled = true
                countPlayer = 1
                val randomNumber = Random.nextInt(100000000, 1000000000)
                sessionNrLabel.text = randomNumber.toString()
                rootService.networkService.hostGame(localPlayer, localplayerMode, randomNumber.toString())
            }
        }

    // Buttons um die Spieler namen hoch und Runter zu schieben
    private val move1DownButton =
        Button(
            width = 80,
            height = 30,
            posX = 1360,
            posY = 340,
            text = "Down",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                if (playerName2Label.text.isBlank()) throw IllegalStateException("Need Second Player")
                val helpString: String = playerName1Label.text
                playerName1Label.text = playerName2Label.text
                playerName2Label.text = helpString
            }
        }
    private val move2DownButton =
        Button(
            width = 80,
            height = 30,
            posX = 1360,
            posY = 490,
            text = "Down",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                if (playerName3Label.text.isBlank()) throw IllegalStateException("Need Third Player")
                val helpString: String = playerName2Label.text
                playerName2Label.text = playerName3Label.text
                playerName3Label.text = helpString
            }
        }
    private val move3DownButton =
        Button(
            width = 80,
            height = 30,
            posX = 1360,
            posY = 640,
            text = "Down",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                if (playerName4Label.text.isBlank()) throw IllegalStateException("Need Fourt Player")
                val helpString: String = playerName3Label.text
                playerName3Label.text = playerName4Label.text
                playerName4Label.text = helpString
            }
        }
    private val move2UpButton =
        Button(
            width = 80,
            height = 30,
            posX = 1360,
            posY = 450,
            text = "Up",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                if (playerName2Label.text.isBlank()) throw IllegalStateException("Need Second Player")
                val helpString: String = playerName1Label.text
                playerName1Label.text = playerName2Label.text
                playerName2Label.text = helpString
            }
        }
    private val move3UpButton =
        Button(
            width = 80,
            height = 30,
            posX = 1360,
            posY = 600,
            text = "Up",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                if (playerName3Label.text.isBlank()) throw IllegalStateException("Need Thrid Player")
                val helpString: String = playerName2Label.text
                playerName2Label.text = playerName3Label.text
                playerName3Label.text = helpString
            }
        }
    private val move4UpButton =
        Button(
            width = 80,
            height = 30,
            posX = 1360,
            posY = 750,
            text = "Up",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                if (playerName4Label.text.isBlank()) throw IllegalStateException("Need Fourth Player")
                val helpString: String = playerName3Label.text
                playerName3Label.text = playerName4Label.text
                playerName4Label.text = helpString
            }
        }
    private val randomiceButton =
        Button(
            width = 80,
            height = 30,
            posX = 1360,
            posY = 790,
            text = "Random",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                val randomlist: MutableList<String> = mutableListOf()
                if (playerName1Label.text.isNotBlank()) randomlist.add(playerName1Label.text)
                if (playerName2Label.text.isNotBlank()) randomlist.add(playerName2Label.text)
                if (playerName3Label.text.isNotBlank()) randomlist.add(playerName3Label.text)
                if (playerName4Label.text.isNotBlank()) randomlist.add(playerName4Label.text)
                if (randomlist.isEmpty()) throw IllegalStateException("No Players Available")
                randomlist.shuffle()
                playerName1Label.text = randomlist.removeLast()
                if (randomlist.isNotEmpty()) playerName2Label.text = randomlist.removeLast()
                if (randomlist.isNotEmpty()) playerName3Label.text = randomlist.removeLast()
                if (randomlist.isNotEmpty()) playerName4Label.text = randomlist.removeLast()
            }
        }

    // Boxen zum auswählen der BotSärke wenn keine aktiviert, dann local player
    private var player1HardBox: CheckBox

    private val player1EasyBox: CheckBox
    private val firstPotJpg =
        Label(
            width = 230,
            height = 120,
            visual = itemImageLoader.imageFor("pots/pot_blue.png", 360, 240),
            posX = 590,
            posY = 240,
        )
    private val secondPotJpg =
        Label(
            width = 230,
            height = 120,
            visual = itemImageLoader.imageFor("pots/pot_grey.png", 360, 240),
            posX = 590,
            posY = 390,
        )
    private val thirdPotJpg =
        Label(
            width = 230,
            height = 120,
            visual = itemImageLoader.imageFor("pots/pot_purple.png", 360, 240),
            posX = 590,
            posY = 540,
        )
    private val fourthPotJpg =
        Label(
            width = 230,
            height = 120,
            visual = itemImageLoader.imageFor("pots/pot_red.png", 360, 240),
            posX = 590,
            posY = 690,
        )
    private val swap1Button =
        Button(
            width = 80,
            height = 30,
            posX = 685,
            posY = 390,
            text = "Swap",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                swap1()
            }
        }
    private val swap2Button =
        Button(
            width = 80,
            height = 30,
            posX = 685,
            posY = 540,
            text = "Swap",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                swap2()
            }
        }
    private val swap3Button =
        Button(
            width = 80,
            height = 30,
            posX = 685,
            posY = 690,
            text = "Swap",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                swap3()
            }
        }
    private val swapRandomButton =
        Button(
            width = 80,
            height = 30,
            posX = 685,
            posY = 840,
            text = "Random",
            font = Font(15),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                repeat(100) {
                    val randomNumber = listOf(1, 2, 3).random()
                    when (randomNumber) {
                        1 -> swap1()
                        2 -> swap2()
                        3 -> swap3()
                    }
                }
            }
        }
    private val gameSpeedLabel: Label =
        Label(
            width = 300,
            height = 80,
            posX = 1550,
            posY = 595,
            text = "Game Speed :$gameSpeed",
            font = Font(30),
            visual = ColorVisual(255, 113, 113),
        )
    private val addGameSpeedButton =
        Button(
            width = 80,
            height = 30,
            posX = 1560,
            posY = 700,
            text = "+",
            font = Font(30),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                if (gameSpeed == 100) throw IllegalStateException("Speed is at allready at 100")
                gameSpeed++
                gameSpeedLabel.text = "Game Speed : $gameSpeed"
            }
        }
    private val subGameSpeedButton =
        Button(
            width = 80,
            height = 30,
            posX = 1680,
            posY = 700,
            text = "-",
            font = Font(30),
            visual = ColorVisual(256, 107, 62),
        ).apply {
            onMouseClicked = {
                if (gameSpeed == 1) throw IllegalStateException("Speed is at allready at 1")
                gameSpeed--
                gameSpeedLabel.text = "Game Speed : $gameSpeed"
            }
        }

    init {
        player1EasyBox = CheckBox(posX = 870, posY = 900, text = "Easy Bot")
        player1HardBox =
            CheckBox(posX = 1050, posY = 900, text = "Hard Bot").apply {
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
        player1EasyBox.onCheckedChanged = {
            if (player1EasyBox.isChecked) {
                localplayerMode = 2
                player1HardBox.isDisabled = true
            } else {
                localplayerMode = 0
                player1HardBox.isDisabled = false
            }
        }

        addComponents(
            headlineLabel,
            mainMenuButton,
            startGameButton,
            chooseGoal,
            lobbyLabel,
            sessionNrLabel,
            bujinGoal,
            chokkanGoal,
            moyogiGoal,
            shakanGoal,
            kengaiGoal,
            bunjayJpg,
            chokkanJpg,
            moyogiJpg,
            shakanJpg,
            kengaiJpg,
            bunjayBox,
            chookanBox,
            moyogiBox,
            shakanBox,
            kenegaiBox,
            goalRandomButton,
            firstPotJpg,
            secondPotJpg,
            thirdPotJpg,
            fourthPotJpg,
            swap1Button,
            swap2Button,
            swap3Button,
            swapRandomButton,
            playerName1Label,
            playerName2Label,
            playerName3Label,
            playerName4Label,
            playerAddField,
            addPlayerButton,
            move1DownButton,
            move2DownButton,
            move3DownButton,
            move2UpButton,
            move3UpButton,
            move4UpButton,
            randomiceButton,
            player1EasyBox,
            player1HardBox,
            addGameSpeedButton,
            subGameSpeedButton,
            gameSpeedLabel,
        )
    }

    private fun checkBoxIsCheckedAction(thisBox: CheckBox) {
        println(countGoals)
        countGoals++
        val colorofCard: GoalColor
        when (thisBox) {
            bunjayBox -> colorofCard = GoalColor.BROWN
            chookanBox -> colorofCard = GoalColor.ORANGE
            moyogiBox -> colorofCard = GoalColor.GREEN
            shakanBox -> colorofCard = GoalColor.RED
            kenegaiBox -> colorofCard = GoalColor.BLUE
            else -> colorofCard = GoalColor.BROWN
        }
        listGoalColor.add(colorofCard)
        if (countGoals == 3) {
            if (!bunjayBox.isChecked) bunjayBox.isDisabled = true
            if (!chookanBox.isChecked) chookanBox.isDisabled = true
            if (!moyogiBox.isChecked) moyogiBox.isDisabled = true
            if (!shakanBox.isChecked) shakanBox.isDisabled = true
            if (!kenegaiBox.isChecked) kenegaiBox.isDisabled = true
        }
    }

    private fun checkBoxIsNotCheckedAction(thisBox: CheckBox) {
        countGoals--
        val colorofCard: GoalColor
        when (thisBox) {
            bunjayBox -> colorofCard = GoalColor.BROWN
            chookanBox -> colorofCard = GoalColor.ORANGE
            moyogiBox -> colorofCard = GoalColor.GREEN
            shakanBox -> colorofCard = GoalColor.RED
            kenegaiBox -> colorofCard = GoalColor.BLUE
            else -> colorofCard = GoalColor.BROWN
        }
        listGoalColor.remove(colorofCard)
        bunjayBox.isDisabled = false
        chookanBox.isDisabled = false
        moyogiBox.isDisabled = false
        shakanBox.isDisabled = false
        kenegaiBox.isDisabled = false
    }

    private fun checkBoxKlicked(thisBox: CheckBox) {
        // Prüft ob die Checkbox aktiviert oder deaktiviert wurde und
        // füght sie entweder der Liste Hinzu oder enzieht sie wieder.

        if (thisBox.isChecked) {
            checkBoxIsCheckedAction(thisBox)
        } else {
            checkBoxIsNotCheckedAction(thisBox)
        }
    }

    private fun swap1() {
        val helpPotColor: PotColor = potColor1
        potColor1 = potColor2
        potColor2 = helpPotColor
        val helpPlayerMode: Int = localplayerMode
        localplayerMode = player2Mode
        player2Mode = helpPlayerMode
        val helpVisual: Visual = firstPotJpg.visual
        firstPotJpg.visual = secondPotJpg.visual
        secondPotJpg.visual = helpVisual
    }

    private fun swap2() {
        val helpPotColor: PotColor = potColor2
        potColor2 = potColor3
        potColor3 = helpPotColor
        val helpPlayerMode: Int = player2Mode
        player2Mode = player3Mode
        player3Mode = helpPlayerMode
        val helpVisual: Visual = secondPotJpg.visual
        secondPotJpg.visual = thirdPotJpg.visual
        thirdPotJpg.visual = helpVisual
    }

    private fun swap3() {
        val helpPotColor: PotColor = potColor3
        potColor3 = potColor4
        potColor4 = helpPotColor
        val helpPlayerMode: Int = player3Mode
        player3Mode = player4Mode
        player4Mode = helpPlayerMode
        val helpVisual: Visual = thirdPotJpg.visual
        thirdPotJpg.visual = fourthPotJpg.visual
        fourthPotJpg.visual = helpVisual
    }

    override fun refreshConnectionState(
        newState: ConnectionState,
        string: String?,
        list: List<String>?,
    ) {
        if (newState == ConnectionState.WAITING_FOR_GUEST && string != null) {
            newPlayerJoined(string)
        }
    }

    private fun newPlayerJoined(newPlayerName: String) {
        if (countPlayer == 4) throw IllegalStateException("Too many Players")
        countPlayer++
        when (countPlayer) {
            2 -> {
                playerName2Label.text = newPlayerName
                player2Mode = 1
            }

            3 -> {
                playerName3Label.text = newPlayerName
                player3Mode = 1
            }

            4 -> {
                playerName4Label.text = newPlayerName
                player4Mode = 1
            }

            else -> throw IllegalStateException("Error in newPlayerJoined Funktion")
        }
    }
}
