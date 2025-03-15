package gui

import entity.*
import gui.utility.CardImageLoader
import gui.utility.ItemImageLoader
import service.RootService
import tools.aqua.bgw.animation.Animation
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.animation.SequentialAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.container.HexagonGrid
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.components.layoutviews.CameraPane
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.LabeledUIComponent
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*

// TODO add private val rootService: RootService,
class GameScene(
    val rootService: RootService,
    val application: BonsaiApplication,
) : BoardGameScene(1920, 1080),
    Refreshable {
    private val cardImageLoader = CardImageLoader()
    private val itemImageLoader = ItemImageLoader()
    private val cardMap: BidirectionalMap<ZenCard, CardView> = BidirectionalMap()
    private val goalMap: BidirectionalMap<GoalCard, Label> = BidirectionalMap()
    private val tileMap: BidirectionalMap<BonsaiTile, HexagonView> = BidirectionalMap()
    private var currentPlayer = -1 // The currently active player
    private var shownPlayer = -1; // The player that is currently visible on the screen TODO use rootService.currentGame.currentPlayer
    private val playerColors = listOf("#a2aca6", "#cc8c7e", "#409cab", "#9a92b8")
    private val tilesToRemove = mutableListOf<BonsaiTile>()
    private var cardStacks: List<CardStack<CardView>> = listOf()

    // drop area to remove bonsai tiles
    private val thrashArea =
        Area<HexagonView>(150, 500, 100, 100, visual = ColorVisual.ORANGE)
//            .apply {
//            dropAcceptor = { dragEvent ->
//                when (dragEvent.draggedComponent) {
//                    is HexagonView -> true
//                    else -> false
//                }
//            }
//            onDragDropped = { dragEvent ->
//                val view = dragEvent.draggedComponent as HexagonView
//                view.removeFromParent()
//                // TODO call service removeTile
//                bonsaiTiles.remove(tileMap.backward(view))
//
//                tileMap.removeBackward(view)
//            }
//        }

    // elements of the game scene that are equal for everyone
    // grid pane to arrange goal cards
    private val goalCardsPane =
        GridPane<Button>(30, 30, 3, 3, spacing = 20, layoutFromCenter = false)

    // pane to arrange Zen cards on play table
    private val boardPane =
        Pane<CardStack<CardView>>(
            627,
            30,
            854,
            300,
            visual =
                itemImageLoader.imageFor("board.jpg", 854, 301).apply {
                    style.borderRadius =
                        BorderRadius.MEDIUM
                },
        )

    // grid pane to arrange buttons for game options undo, redo, save, exit
    private val gameOptionsGridPane =
        GridPane<Button>(1630, 30, 4, 1, spacing = 20, layoutFromCenter = false)

    // holds playArea to keep centering when expanding
    private val gridPanePlayArea =
        GridPane<HexagonGrid<HexagonView>>(
            posX = 1000,
            posY = 1000,
            layoutFromCenter = true,
            rows = 1,
            columns = 1,
            spacing = 10,
        )

    // holds pot of shownPlayer to keep centered relative to gridPanePlayArea
    private val gridPanePot =
        GridPane<Label>(
            posX = 1000,
            posY = 1000,
            layoutFromCenter = true,
            rows = 1,
            columns = 1,
            spacing = 10,
        )

    // pane holding the elements of the player's bonsai
    private val bonsaiLayout =
        Pane<ComponentView>(
            width = 2000,
            height = 2000,
            visual = Visual.EMPTY,
        ).apply { addAll(gridPanePlayArea, gridPanePot) }

    // camera pane showing the bonsaiLayout
    private val playAreaCameraPane =
        CameraPane(
            posX = 830,
            posY = 390,
            width = 800,
            height = 600,
            target = bonsaiLayout,
            limitBounds = true,
            visual = ColorVisual(255, 255, 255, 100),
        )

    private val playerNamesGridPane =
        GridPane<Pane<LabeledUIComponent>>(830, 1007, 3, 1, 50, layoutFromCenter = false)

    private val endTurnButton =
        Button(1712, 970, 178, 80, text = "End Turn").apply {
            onMouseClicked = {}
            visual =
                ColorVisual(200, 150, 120, 255).apply {
                    style.borderRadius =
                        BorderRadius.MEDIUM
                }
            font = Font(size = 24, fontWeight = Font.FontWeight.BOLD)
        }

    // elements of the players
    // bonsai tiles
    private val bonsaiTilesView1 =
        LinearLayout<HexagonView>(30, 658, 530, 60, visual = ColorVisual(255, 255, 255, 100), spacing = 10).apply {
            alignment =
                Alignment.CENTER
        }
    private val bonsaiTilesView2 =
        LinearLayout<HexagonView>(30, 718, 530, 60, visual = ColorVisual(255, 255, 255, 100), spacing = 10).apply {
            alignment =
                Alignment.CENTER
        }

    // tool cards
    private val toolCardsView = CardStack<CardView>(30, 808, 143, 200, visual = Visual.EMPTY)

    // tool cards multiplier
    private val toolCardsMultiplierLabel =
        Label(136, 888, 37, 30, visual = Visual.EMPTY, font = Font(size = 20, fontWeight = Font.FontWeight.BOLD))

    // seishi tiles, visual is set later based on shownPlayer
    private val seishiTile = Label(154, 808, 160, 242)

    // growth cards
    private val growthCardsView = LinearLayout<CardView>(295, 808, 650, 200, spacing = -106)

    // bonsai-pots
    private val potGrey =
        Label(width = 260, height = 174, visual = itemImageLoader.imageFor("pots/pot_grey.png", 360, 240))
    private val potRed =
        Label(width = 260, height = 174, visual = itemImageLoader.imageFor("pots/pot_red.png", 360, 240))
    private val potBlue =
        Label(width = 260, height = 174, visual = itemImageLoader.imageFor("pots/pot_blue.png", 360, 240))
    private val potPurple =
        Label(width = 260, height = 174, visual = itemImageLoader.imageFor("pots/pot_purple.png", 360, 240))

    // bonsai placing area
    private var playAreaGrey = createPlayArea(0)
    private var playAreaRed = createPlayArea(1)
    private var playAreaBlue = createPlayArea(2)
    private var playAreaPurple = createPlayArea(3)

    private fun createPlayArea(player: Int): HexagonGrid<HexagonView> {
        val playArea = HexagonGrid<HexagonView>(coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL)
        val size = 20
        // Create three rings of hexagons
        for (row in -size..size) {
            for (col in -size..size) {
                // Only add hexagons that would fit in a circle
                if (row + col in -size..size) {
                    val hexagon =
                        HexagonView(
                            visual =
                                CompoundVisual(
                                    ColorVisual(Color(playerColors[player])).apply {
                                        style.borderRadius =
                                            BorderRadius.MEDIUM
                                    },
                                    TextVisual(
                                        text = "$col, $row",
                                        font = Font(10.0, Color(0x0f141f)),
                                    ),
                                ),
                            size = 25,
                        ).apply {
                            dropAcceptor = { dragEvent ->
                                when (dragEvent.draggedComponent) {
                                    is HexagonView -> true
                                    else -> false
                                }
                            }
                            onDragDropped = { dragEvent ->
                                val view = dragEvent.draggedComponent as HexagonView
                                view.removeFromParent()
                                playArea[col, row] = view
                            }
                        }
                    playArea[col, row] = hexagon
                }
            }
        }
        val logPiece = HexagonView(visual = itemImageLoader.imageFor(BonsaiTile(TileType.WOOD)), size = 25)
        playArea[0, 0] = logPiece
        return playArea
    }

    // claimed goal cards
    private val claimedGoalCardsGridPane =
        GridPane<LabeledUIComponent>(
            1712,
            440,
            1,
            4,
            spacing = 20,
            layoutFromCenter = false,
        )

    // drawm cards
    private val drawnCardsStack =
        CardStack<CardView>(
            1729,
            740,
            143,
            200,
            visual = Visual.EMPTY,
        )

    init {
        background = ColorVisual(Color("#D9D9D9"))

        addComponents(
            goalCardsPane,
            boardPane,
            gameOptionsGridPane,
            thrashArea,
            bonsaiTilesView1,
            bonsaiTilesView2,
            toolCardsView,
            toolCardsMultiplierLabel,
            seishiTile,
            growthCardsView,
            playAreaCameraPane,
            playerNamesGridPane,
            claimedGoalCardsGridPane,
            drawnCardsStack,
            endTurnButton,
        )
        playAreaCameraPane.interactive = true
    }

    override fun refreshAfterStartNewGame() {
        val game = rootService.currentGame
        checkNotNull(game) { "No started game found." }

        cardMap.clear()
        tileMap.clear()

        currentPlayer = game.currentState.currentPlayer
        shownPlayer = currentPlayer

        initializeGameElements(game)
        initializePlayerView(game)
    }

    private fun initializeGameElements(game: BonsaiGame) {
        initializeGoalCardGridPane(game)
        initializeBoardPane(game)
        initializeGameOptionsGridPane()
    }

    private fun initializePlayerView(game: BonsaiGame) {
        // TODO use elements of given player
        initializePlayArea(game.currentState)
        initializeBonsaiTiles(game.currentState)
        initializeSeishiTile(game.currentState)
        initializeSeishiCards(game.currentState)
        initializePlayerNamesGridPane(game)
        initializeClaimedGoalsGridPane(game.currentState)
        initializeDrawnCardsStack(game.currentState)
    }

    private fun initializeBonsaiTiles(state: GameState) {
        // TODO use bonsai tiles in players supply

        // clear views & map to include only tiles of shownPlayer
        bonsaiTilesView1.clear()
        bonsaiTilesView2.clear()
        tileMap.clear()

        // create tileViews based on player supply
        for ((index, tile) in state.players[shownPlayer].supply.withIndex()) {
            val tileView =
                HexagonView(visual = itemImageLoader.imageFor(tile), size = 25).apply {
                    isDraggable = true
                    onDragGestureEnded = { _, success ->
                        if (success) {
                            this.isDraggable = false
                        }
                    }
                }
            if (index < 9) {
                bonsaiTilesView1.add(tileView)
            } else {
                bonsaiTilesView2.add(tileView)
            }
            tileMap.add(tile, tileView)
        }
    }

    private fun initializePlayArea(state: GameState) {
        when (state.players[shownPlayer].potColor) {
            PotColor.GRAY -> {
                gridPanePlayArea[0, 0] = playAreaGrey
                gridPanePot[0, 0] = potGrey
            }
            PotColor.RED -> {
                gridPanePlayArea[0, 0] = playAreaRed
                gridPanePot[0, 0] = potRed
            }
            PotColor.BLUE -> {
                gridPanePlayArea[0, 0] = playAreaBlue
                gridPanePot[0, 0] = potBlue
            }
            PotColor.PURPLE -> {
                gridPanePlayArea[0, 0] = playAreaPurple
                gridPanePot[0, 0] = potPurple
            }
        }
    }

    private fun initializeGoalCardGridPane(game: BonsaiGame) {
        // TODO use goalCard-entity & fill [goalMap] with goald-entites/view
        val gameGoalCards = game.currentState.goalCards

        val targetWidths = listOf(125, 139, 179)

        for ((idx, goalCard) in gameGoalCards.withIndex()) {
            if (goalCard != null) {
                val goalCardView =
                    Button(
                        width = targetWidths[goalCard.difficulty.ordinal],
                        height = 80,
                        visual =
                            itemImageLoader.imageFor(goalCard).apply {
                                style.borderRadius = BorderRadius.MEDIUM
                            },
                    )
                goalCardsPane[goalCard.difficulty.ordinal, idx / 3] = goalCardView
            }
        }
    }

    private fun initializeBoardPane(game: BonsaiGame) {
        val drawStackView = createDrawStackView(32, 33, game.currentState.drawStack, 0, false)
        val cardStackView1 = createOpenCardView(216, 33, game.currentState.openCards[0], 1, true)
        val cardStackView2 = createOpenCardView(371, 33, game.currentState.openCards[1], 2, true)
        val cardStackView3 = createOpenCardView(525, 33, game.currentState.openCards[2], 3, true)
        val cardStackView4 = createOpenCardView(679, 33, game.currentState.openCards[3], 4, true)

        cardStacks = listOf(drawStackView, cardStackView1, cardStackView2, cardStackView3, cardStackView4)

        boardPane.addAll(
            drawStackView,
            cardStackView1,
            cardStackView2,
            cardStackView3,
            cardStackView4,
        )
    }

    private fun createDrawStackView(
        posX: Int = 0,
        posY: Int = 0,
        cardStack: ArrayDeque<ZenCard>,
        number: Int = 0,
        showFront: Boolean = true,
    ): CardStack<CardView> {
        val cardStackView =
            CardStack<CardView>(
                posX = posX,
                posY = posY,
                width = 143,
                height = 200,
                visual = Visual.EMPTY,
            ).apply {
                if (number > 0) {
                    onMouseClicked = {
                        playDrawCardAnimation(number)
                    }
                }
            }

        for (card in cardStack.reversed()) {
            val cardView =
                CardView(
                    0,
                    0,
                    143,
                    200,
                    front = cardImageLoader.frontImageFor(card),
                    back = cardImageLoader.backImage,
                ).apply { if (showFront) showFront() }
            cardStackView.push(cardView)

            cardMap.add(card, cardView)
        }
        return cardStackView
    }

    private fun createOpenCardView(
        posX: Int = 0,
        posY: Int = 0,
        card: ZenCard,
        number: Int = 0,
        showFront: Boolean = true,
    ): CardStack<CardView> {
        val cardStackView =
            CardStack<CardView>(
                posX = posX,
                posY = posY,
                width = 143,
                height = 200,
                visual = Visual.EMPTY,
            ).apply {
                if (number > 0) {
                    onMouseClicked = {
                        playDrawCardAnimation(number)
                    }
                }
            }

        val cardView =
            CardView(
                0,
                0,
                143,
                200,
                front = cardImageLoader.frontImageFor(card),
                back = cardImageLoader.backImage,
            ).apply { if (showFront) showFront() }
        cardStackView.push(cardView)

        cardMap.add(card, cardView)

        return cardStackView
    }

    private fun initializeGameOptionsGridPane() {
        val iconPaths =
            listOf(
                "icons/undo_icon.png",
                "icons/redo_icon.png",
                "icons/save_icon.png",
                "icons/close_icon.png",
            )

        for ((idx, iconPath) in iconPaths.withIndex()) {
            val menuOptionButton =
                Button(
                    width = 50,
                    height = 50,
                    visual = itemImageLoader.imageFor(iconPath, 96, 96),
                ).apply {
                    onMouseClicked = {
                        when (idx) {
                            0 -> rootService.playerActionService.undo()
                            1 -> rootService.playerActionService.redo()
                            2 -> rootService.gameService.saveGame()
                            3 -> application.showMenuScene(application.mainMenuScene)
                        }
                    }
                }
            gameOptionsGridPane[idx, 0] = menuOptionButton
        }
    }

    private fun initializeSeishiCards(state: GameState) {
        toolCardsView.clear()
        for (card in state.players[shownPlayer].seishiTool.reversed()) {
            toolCardsView.add(
                CardView(
                    0,
                    0,
                    143,
                    200,
                    front = cardImageLoader.frontImageFor(card),
                    back = cardImageLoader.backImage,
                ).apply { showFront() },
            )
        }

        growthCardsView.clear()
        for (card in state.players[shownPlayer].seishiGrowth.reversed()) {
            growthCardsView.add(
                CardView(
                    0,
                    0,
                    143,
                    200,
                    front = cardImageLoader.frontImageFor(card),
                    back = cardImageLoader.backImage,
                ).apply { showFront() },
            )
        }
    }

    private fun initializeSeishiTile(state: GameState) {
        // TODO get visual by function in ItemImageLoader
        when (state.players[shownPlayer].potColor) {
            PotColor.GRAY -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_grey.png", 160, 242)
            PotColor.RED -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_red.png", 160, 242)
            PotColor.BLUE -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_blue.png", 160, 242)
            PotColor.PURPLE -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_purple.png", 160, 242)
        }
    }

    private fun initializePlayerNamesGridPane(game: BonsaiGame) {
        val players = game.currentState.players
        val prevPlayer = (shownPlayer - 1 + players.size) % players.size
        val nextPlayer = (shownPlayer + 1) % players.size
        val prevPlayerPane = Pane<LabeledUIComponent>(0, 0, 150, 40)
        val prevPlayerButton =
            Button(0, 0, 150, 40, text = players[prevPlayer].name).apply {
                alignment = Alignment.CENTER_RIGHT
                visual =
                    ColorVisual(Color(playerColors[prevPlayer])).apply {
                        style.borderRadius =
                            BorderRadius.MEDIUM
                    }
                font = Font(size = 15, fontWeight = Font.FontWeight.BOLD)
                onMouseClicked = {
                    shownPlayer = prevPlayer
                    initializePlayerView(game)
                }
            }
        val leftArrow =
            Label(0, 0, 40, 40, visual = itemImageLoader.imageFor("icons/arrow_back.png", 96, 96))
        prevPlayerPane.addAll(prevPlayerButton, leftArrow)

        val currentPlayerPane = Pane<LabeledUIComponent>(0, 0, 400, 40)
        val currentPlayerLabel =
            Label(0, 0, 400, 40, text = players[shownPlayer].name).apply {
                visual =
                    ColorVisual(Color(playerColors[shownPlayer])).apply {
                        style.borderRadius =
                            BorderRadius.MEDIUM
                    }
                font = Font(size = 24, fontWeight = Font.FontWeight.BOLD)
            }
        currentPlayerPane.add(currentPlayerLabel)

        val nextPlayerPane = Pane<LabeledUIComponent>(0, 0, 150, 40)
        val nextPlayerButton =
            Button(0, 0, 150, 40, text = players[nextPlayer].name).apply {
                alignment = Alignment.CENTER_LEFT
                visual =
                    ColorVisual(Color(playerColors[nextPlayer])).apply {
                        style.borderRadius =
                            BorderRadius.MEDIUM
                    }
                font = Font(size = 15, fontWeight = Font.FontWeight.BOLD)
                onMouseClicked = {
                    shownPlayer = nextPlayer
                    initializePlayerView(game)
                }
            }
        val rightArrow =
            Label(110, 0, 40, 40, visual = itemImageLoader.imageFor("icons/arrow_forward.png", 96, 96))

        nextPlayerPane.addAll(nextPlayerButton, rightArrow)

        playerNamesGridPane[0, 0] = prevPlayerPane
        playerNamesGridPane[1, 0] = currentPlayerPane
        playerNamesGridPane[2, 0] = nextPlayerPane
    }

    private fun initializeDrawnCardsStack(state: GameState) {
        val drawnCards = state.players[shownPlayer].hiddenDeck
        drawnCardsStack.clear()
        for (card in drawnCards.reversed()) {
            drawnCardsStack.add(
                CardView(
                    0,
                    0,
                    143,
                    200,
                    front = cardImageLoader.frontImageFor(card),
                    back = cardImageLoader.backImage,
                ),
            )
        }
    }

    private fun initializeClaimedGoalsGridPane(state: GameState) {
        // TODO use claimedGoalCards of shownPlayer
        val claimedGameGoalCards = state.players[shownPlayer].acceptedGoals
        val targetWidths = listOf(178, 139, 124)
        val targetHeight = 80

        for ((idx, goalCard) in claimedGameGoalCards.withIndex()) {
            val claimedGoalCard =
                Label(
                    width = targetWidths[idx],
                    height = targetHeight,
                    visual =
                        itemImageLoader.imageFor(goalCard).apply {
                            style.borderRadius = BorderRadius.MEDIUM
                        },
                )
            claimedGoalCardsGridPane[0, idx] = claimedGoalCard
        }
    }

    private fun playDrawCardAnimation(drawnCardIndex: Int) {
        val animations = mutableListOf<Animation>()
        animations.add(getDrawnCardAnimation(drawnCardIndex))
        animations.addAll(getRefillCardsAnimation(drawnCardIndex))
        playAnimation(
            SequentialAnimation(
                animations,
            ),
        )
    }

    private fun getDrawnCardAnimation(drawnCardIndex: Int): Animation =
        MovementAnimation
            .toComponentView(
                componentView = cardStacks[drawnCardIndex].peek(),
                toComponentViewPosition =
                    when (cardMap.backward(cardStacks[drawnCardIndex].peek())) {
                        is ToolCard -> toolCardsView
                        is GrowthCard -> growthCardsView
                        else -> drawnCardsStack
                    },
                scene = this,
                duration = 1000,
            ).apply {
                onFinished = {
                    val target =
                        when (cardMap.backward(cardStacks[drawnCardIndex].peek())) {
                            is ToolCard -> toolCardsView
                            is GrowthCard -> growthCardsView
                            else -> drawnCardsStack
                        }

                    target.add(
                        cardStacks[drawnCardIndex].pop().apply {
                            if (target ==
                                drawnCardsStack
                            ) {
                                showBack()
                            } else {
                                showFront()
                            }
                        },
                    )

                    // update toolCardMultiplier if drawn card is tool card
                    if (target == toolCardsView) {
                        toolCardsMultiplierLabel.text = "*${toolCardsView.numberOfComponents()}"
                    }
                }
            }

    private fun getRefillCardsAnimation(drawnCardIndex: Int): List<Animation> {
        var refillCardIdx = drawnCardIndex - 1
        val animations = mutableListOf<Animation>()
        var rightestEmptyCardStack = -1
        // only the cards on non-empty stacks to the left can be moved to the right
        for (i in 3 downTo 0) {
            if (cardStacks[i].isEmpty()) {
                rightestEmptyCardStack = i
                break
            }
        }
        // create animation for moving cards to the right-neighboured stack
        while (refillCardIdx > rightestEmptyCardStack) {
            val idx = refillCardIdx
            animations.add(
                MovementAnimation
                    .toComponentView(
                        componentView = cardStacks[idx].peek(),
                        toComponentViewPosition = cardStacks[idx + 1],
                        scene = this@GameScene,
                        duration = 1000,
                    ).apply {
                        onFinished = {
                            val card = cardStacks[idx].peek()
                            card.removeFromParent()
                            card.showFront()
                            cardStacks[idx + 1].add(card)
                        }
                    },
            )
            refillCardIdx -= 1
        }

        return animations
    }
}
