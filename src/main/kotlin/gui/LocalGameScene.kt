package gui

import entity.GoalColor
import entity.PotColor
import gui.utility.*
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual

/**
 * [MenuScene] that gets displayed to configure a new local game including the goals, players, player order and player
 * colors.
 *
 * @param rootService The [RootService] to access the other services
 * @param application The running application
 */
class LocalGameScene(
    rootService: RootService,
    application: BonsaiApplication,
) : MenuScene(1920, 1080),
    Refreshable {
    private val itemImageLoader = ItemImageLoader()
    private var countGoals = 0
    private var countPlayer = 0
    private var gameSpeed = 10
    private var player1Mode = 0
    private var player2Mode = 0
    private var player3Mode = 0
    private var player4Mode = 0
    private var potColor1 = PotColor.BLUE
    private var potColor2 = PotColor.GRAY
    private var potColor3 = PotColor.PURPLE
    private var potColor4 = PotColor.RED
    private val listGoalColor: MutableList<GoalColor> = mutableListOf()
    private val headlineLabel =
        Label(
            width = 570,
            height = 100,
            posX = 700,
            posY = 20,
            text = "Local Game",
            font = Font(size = 100, fontWeight = Font.FontWeight.BOLD),
        )
    private val mainMenuButton =
        Button(width = 300, height = 80, posX = 1550, posY = 160, text = "Main Menu", font = Font(35)).apply {
            visual = ColorVisual(256, 107, 62)
            onMouseClicked = {
                application.showMenuScene(application.mainMenuScene)
                // new local game scene to reset selections
                application.localGameScene = LocalGameScene(rootService, application)
            }
        }
    private val startGameButton =
        Button(width = 300, height = 80, posX = 1550, posY = 460, text = "StartGame", font = Font(35)).apply {
            visual = ColorVisual(256, 107, 62)
            onMouseClicked = {
                if (countPlayer < 2) throw IllegalStateException("More Players Needet To play Game")
                if (countGoals != 3) throw IllegalStateException("Choose Goals")
                val playerList: MutableList<Triple<String, Int, PotColor>> = mutableListOf()
                var helpPotColor = potColor1
                playerList.add(Triple(playerName1Label.text, player1Mode, helpPotColor))
                helpPotColor = potColor2
                playerList.add(Triple(playerName2Label.text, player2Mode, helpPotColor))
                helpPotColor = potColor3
                if (playerName3Label.text.isNotBlank()) {
                    playerList.add(
                        Triple(playerName3Label.text, player3Mode, helpPotColor),
                    )
                }
                helpPotColor = potColor4
                if (playerName4Label.text.isNotBlank()) {
                    playerList.add(
                        Triple(playerName4Label.text, player4Mode, helpPotColor),
                    )
                }

                rootService.gameService.startNewGame(playerList, gameSpeed, listGoalColor)
                // new local game scene to reset selections
                application.localGameScene = LocalGameScene(rootService, application)
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
                when (countPlayer) {
                    0 -> playerName1Label.text = playerAddField.text
                    1 -> playerName2Label.text = playerAddField.text
                    2 -> playerName3Label.text = playerAddField.text
                    3 -> playerName4Label.text = playerAddField.text
                    else -> throw IllegalStateException("Too Many Players")
                }
                countPlayer++
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
    private var player2HardBox: CheckBox
    private var player3HardBox: CheckBox
    private var player4HardBox: CheckBox
    private val player1EasyBox: CheckBox
    private val player2EasyBox: CheckBox
    private val player3EasyBox: CheckBox
    private val player4EasyBox: CheckBox
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
        player1EasyBox = CheckBox(posX = 870, posY = 265, text = "Easy Bot")
        player1HardBox =
            CheckBox(posX = 1050, posY = 265, text = "Hard Bot").apply {
                onCheckedChanged = {
                    if (this.isChecked) {
                        player1Mode = 3
                        player1EasyBox.isDisabled = true
                    } else {
                        player1Mode = 0
                        player1EasyBox.isDisabled = false
                    }
                }
            }
        player1EasyBox.onCheckedChanged = {
            if (player1EasyBox.isChecked) {
                player1Mode = 2
                player1HardBox.isDisabled = true
            } else {
                player1Mode = 0
                player1HardBox.isDisabled = false
            }
        }
        player2EasyBox = CheckBox(posX = 870, posY = 415, text = "Easy Bot")
        player2HardBox =
            CheckBox(posX = 1050, posY = 415, text = "Hard Bot").apply {
                onCheckedChanged = {
                    if (this.isChecked) {
                        player2Mode = 3
                        player2EasyBox.isDisabled = true
                    } else {
                        player2Mode = 0
                        player2EasyBox.isDisabled = false
                    }
                }
            }
        player2EasyBox.onCheckedChanged = {
            if (player2EasyBox.isChecked) {
                player2Mode = 2
                player2HardBox.isDisabled = true
            } else {
                player1Mode = 0
                player2HardBox.isDisabled = false
            }
        }
        player3EasyBox = CheckBox(posX = 870, posY = 565, text = "Easy Bot")
        player3HardBox =
            CheckBox(posX = 1050, posY = 565, text = "Hard Bot").apply {
                onCheckedChanged = {
                    if (this.isChecked) {
                        player3Mode = 3
                        player3EasyBox.isDisabled = true
                    } else {
                        player3Mode = 0
                        player3EasyBox.isDisabled = false
                    }
                }
            }
        player3EasyBox.onCheckedChanged = {
            if (player3EasyBox.isChecked) {
                player3Mode = 2
                player3HardBox.isDisabled = true
            } else {
                player3Mode = 0
                player3HardBox.isDisabled = false
            }
        }
        player4EasyBox = CheckBox(posX = 870, posY = 715, text = "Easy Bot")
        player4HardBox =
            CheckBox(posX = 1050, posY = 715, text = "Hard Bot").apply {
                onCheckedChanged = {
                    if (this.isChecked) {
                        player4Mode = 3
                        player4EasyBox.isDisabled = true
                    } else {
                        player4Mode = 0
                        player4EasyBox.isDisabled = false
                    }
                }
            }
        player4EasyBox.onCheckedChanged = {
            if (player4EasyBox.isChecked) {
                player4Mode = 2
                player4HardBox.isDisabled = true
            } else {
                player4Mode = 0
                player4HardBox.isDisabled = false
            }
        }
        addComponents(
            headlineLabel,
            mainMenuButton,
            startGameButton,
            chooseGoal,
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
            player2EasyBox,
            player2HardBox,
            player3EasyBox,
            player3HardBox,
            player4EasyBox,
            player4HardBox,
            gameSpeedLabel,
            addGameSpeedButton,
            subGameSpeedButton,
        )
    }

    private fun checkBoxIsCheckedAction(thisBox: CheckBox){
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

    private fun checkBoxIsNotCheckedAction(thisBox: CheckBox){
        countGoals--
        val colorofCard: GoalColor =
            when (thisBox) {
                bunjayBox -> GoalColor.BROWN
                chookanBox -> GoalColor.ORANGE
                moyogiBox -> GoalColor.GREEN
                shakanBox -> GoalColor.RED
                kenegaiBox -> GoalColor.BLUE
                else -> GoalColor.BROWN
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
        val helpVisual: Visual = firstPotJpg.visual
        firstPotJpg.visual = secondPotJpg.visual
        secondPotJpg.visual = helpVisual
    }

    private fun swap2() {
        val helpPotColor: PotColor = potColor2
        potColor2 = potColor3
        potColor3 = helpPotColor
        val helpVisual: Visual = secondPotJpg.visual
        secondPotJpg.visual = thirdPotJpg.visual
        thirdPotJpg.visual = helpVisual
    }

    private fun swap3() {
        val helpPotColor: PotColor = potColor3
        potColor3 = potColor4
        potColor4 = helpPotColor
        val helpVisual: Visual = thirdPotJpg.visual
        thirdPotJpg.visual = fourthPotJpg.visual
        fourthPotJpg.visual = helpVisual
    }
}
