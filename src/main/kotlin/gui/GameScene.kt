package gui

import entity.*
import gui.utility.CardImageLoader
import gui.utility.ItemImageLoader
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
import tools.aqua.bgw.util.Stack
import tools.aqua.bgw.visual.*

// TODO add private val rootService: RootService,
class GameScene : BoardGameScene(1920, 1080) {
    private val cardImageLoader = CardImageLoader()
    private val itemImageLoader = ItemImageLoader()
    private val cardMap: BidirectionalMap<ZenCard, CardView> = BidirectionalMap()
    private val goalMap: BidirectionalMap<GoalCard, Label> = BidirectionalMap()
    private val tileMap: BidirectionalMap<BonsaiTile, HexagonView> = BidirectionalMap()
    private val currentPlayer = 0 // The currently active player TODO use rootService.currentGame.currentPlayer
    private var shownPlayer = 0; // The player that is currently visible on the screen TODO use rootService.currentGame.currentPlayer
    private val playerColors = listOf("#a2aca6", "#cc8c7e", "#409cab", "#9a92b8")
    private val tilesToRemove = mutableListOf<BonsaiTile>()

    // dummy variables for testing without service layer
    private val playerNames = listOf("player1", "player2", "player3", "player4")
    private val drawStack = Stack<ZenCard>(ToolCard(2))
    private val cardStack1 = Stack<ZenCard>(MasterCard(listOf(TileType.WOOD,TileType.WOOD),21))
    private val cardStack2 = Stack<ZenCard>(GrowthCard(
        TileType.WOOD,
        id = 0
    ))
    private val cardStack3 = Stack<ZenCard>(GrowthCard(
        TileType.WOOD,
        id = 1
    ))
    private val cardStack4 = Stack<ZenCard>(ToolCard(2))
    private lateinit var cardStacks: List<CardStack<CardView>>
    private val bonsaiTiles =
        mutableListOf(
            BonsaiTile(TileType.WOOD),
            BonsaiTile(TileType.WOOD),
            BonsaiTile(TileType.LEAF),
            BonsaiTile(TileType.WOOD),
            BonsaiTile(TileType.LEAF),
            BonsaiTile(TileType.FRUIT),
            BonsaiTile(TileType.FRUIT),
            BonsaiTile(TileType.LEAF),
            BonsaiTile(TileType.FLOWER),
            BonsaiTile(TileType.FRUIT),
            BonsaiTile(TileType.WOOD),
            BonsaiTile(TileType.LEAF),
            BonsaiTile(TileType.WOOD),
            BonsaiTile(TileType.LEAF),
            BonsaiTile(TileType.FRUIT),
            BonsaiTile(TileType.FRUIT),
            BonsaiTile(TileType.LEAF),
            BonsaiTile(TileType.FLOWER),
        )

    private val toolCardsStack = Stack<ToolCard>()
    private val growthCardsStack = Stack<GrowthCard>()

    // drop area to remove bonsai tiles
    private val thrashArea =
        Area<HexagonView>(150, 500, 100, 100, visual = ColorVisual.ORANGE).apply {
            dropAcceptor = { dragEvent ->
                when (dragEvent.draggedComponent) {
                    is HexagonView -> true
                    else -> false
                }
            }
            onDragDropped = { dragEvent ->
                val view = dragEvent.draggedComponent as HexagonView
                view.removeFromParent()
                // TODO call service removeTile
                bonsaiTiles.remove(tileMap.backward(view))

                tileMap.removeBackward(view)
            }
        }

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
    private val pot1 =
        Label(width = 260, height = 174, visual = itemImageLoader.imageFor("pots/pot_grey.png", 360, 240))
    private val pot2 =
        Label(width = 260, height = 174, visual = itemImageLoader.imageFor("pots/pot_red.png", 360, 240))
    private val pot3 =
        Label(width = 260, height = 174, visual = itemImageLoader.imageFor("pots/pot_blue.png", 360, 240))
    private val pot4 =
        Label(width = 260, height = 174, visual = itemImageLoader.imageFor("pots/pot_purple.png", 360, 240))

    // bonsai placing area
    private var playArea1 = createPlayArea(0)
    private var playArea2 = createPlayArea(1)
    private var playArea3 = createPlayArea(2)
    private var playArea4 = createPlayArea(3)

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
        initializeGameElements()
        initializePlayerView()

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

    private fun initializeGameElements() {
        initializeGoalCardGridPane()
        initializeBoardPane()
        initializeGameOptionsGridPane()
    }

    private fun initializePlayerView() {
        // TODO use elements of given player
        initializePlayArea()
        initializeBonsaiTiles()
        initializeSeishiTile()
        initializeSeishiCards()
        initializePlayerNamesGridPane()
        initializeClaimedGoalsGridPane()
        initializeDrawnCardsStack()
    }

    private fun initializeBonsaiTiles() {
        // TODO use bonsai tiles in players supply

        // clear views & map to include only tiles of shownPlayer
        bonsaiTilesView1.clear()
        bonsaiTilesView2.clear()
        tileMap.clear()

        // create tileViews based on player supply
        for ((index, tile) in bonsaiTiles.withIndex()) {
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

    private fun initializePlayArea() {
        // TODO initialize with bonsai in entity layer
        when (shownPlayer) {
            0 -> {
                gridPanePlayArea[0, 0] = playArea1
                gridPanePot[0, 0] = pot1
            }
            1 -> {
                gridPanePlayArea[0, 0] = playArea2
                gridPanePot[0, 0] = pot2
            }
            2 -> {
                gridPanePlayArea[0, 0] = playArea3
                gridPanePot[0, 0] = pot3
            }
            3 -> {
                gridPanePlayArea[0, 0] = playArea4
                gridPanePot[0, 0] = pot4
            }
        }
    }

    private fun initializeGoalCardGridPane() {
        // TODO use goalCard-entity & fill [goalMap] with goald-entites/view
        val gameGoalCards =
            listOf(
                listOf(
                    GoalCard(5, GoalColor.BROWN, GoalDifficulty.LOW),
                    GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE),
                ),
                listOf(
                    GoalCard(6, GoalColor.GREEN, GoalDifficulty.LOW),
                    GoalCard(12, GoalColor.GREEN, GoalDifficulty.HARD),
                ),
                listOf(
                    GoalCard(11, GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE),
                    GoalCard(13, GoalColor.ORANGE, GoalDifficulty.HARD),
                ),
            )

        val targetWidths = listOf(125, 139, 179)

        for ((listIdx, list) in gameGoalCards.withIndex()) {
            for (goalCard in list) {
                val goalCardView =
                    Button(
                        width = targetWidths[goalCard.difficulty.ordinal],
                        height = 80,
                        visual =
                            itemImageLoader.imageFor(goalCard).apply {
                                style.borderRadius = BorderRadius.MEDIUM
                            },
                    )
                goalCardsPane[goalCard.difficulty.ordinal, listIdx] = goalCardView
            }
        }
    }

    private fun initializeBoardPane() {
        // TODO use game stack entities & fill [cardMap] with card-entites/view

        val drawStackView = createDrawStackView(32, 33, drawStack, 0, false)
        val cardStackView1 = createDrawStackView(216, 33, cardStack1, 1, true)
        val cardStackView2 = createDrawStackView(371, 33, cardStack2, 2, true)
        val cardStackView3 = createDrawStackView(525, 33, cardStack3, 3, true)
        val cardStackView4 = createDrawStackView(679, 33, cardStack4, 4, true)

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
        cardStack: Stack<ZenCard>,
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

        for (card in cardStack.peekAll().reversed()) {
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
                )
            gameOptionsGridPane[idx, 0] = menuOptionButton
        }
    }

    private fun initializeSeishiCards() {
        toolCardsView.clear()
        for (card in toolCardsStack.peekAll().reversed()) {
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
        for (card in growthCardsStack.peekAll().reversed()) {
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

    private fun initializeSeishiTile() {
        // TODO get visual by function in ItemImageLoader
        when (shownPlayer) {
            0 -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_grey.png", 160, 242)
            1 -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_red.png", 160, 242)
            2 -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_blue.png", 160, 242)
            3 -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_purple.png", 160, 242)
        }
    }

    private fun initializePlayerNamesGridPane() {
        val prevPlayer = (shownPlayer - 1 + 4) % 4 // TODO use number of players for addition & modulo
        val nextPlayer = (shownPlayer + 1) % 4 // TODO use number of players for modulo
        val prevPlayerPane = Pane<LabeledUIComponent>(0, 0, 150, 40)
        val prevPlayerButton =
            Button(0, 0, 150, 40, text = playerNames[prevPlayer]).apply {
                alignment = Alignment.CENTER_RIGHT
                visual =
                    ColorVisual(Color(playerColors[prevPlayer])).apply {
                        style.borderRadius =
                            BorderRadius.MEDIUM
                    }
                font = Font(size = 15, fontWeight = Font.FontWeight.BOLD)
                onMouseClicked = {
                    shownPlayer = prevPlayer
                    initializePlayerView()
                }
            }
        val leftArrow =
            Label(0, 0, 40, 40, visual = itemImageLoader.imageFor("icons/arrow_back.png", 96, 96))
        prevPlayerPane.addAll(prevPlayerButton, leftArrow)

        val currentPlayerPane = Pane<LabeledUIComponent>(0, 0, 400, 40)
        val currentPlayerLabel =
            Label(0, 0, 400, 40, text = playerNames[shownPlayer]).apply {
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
            Button(0, 0, 150, 40, text = playerNames[nextPlayer]).apply {
                alignment = Alignment.CENTER_LEFT
                visual =
                    ColorVisual(Color(playerColors[nextPlayer])).apply {
                        style.borderRadius =
                            BorderRadius.MEDIUM
                    }
                font = Font(size = 15, fontWeight = Font.FontWeight.BOLD)
                onMouseClicked = {
                    shownPlayer = nextPlayer
                    initializePlayerView()
                }
            }
        val rightArrow =
            Label(110, 0, 40, 40, visual = itemImageLoader.imageFor("icons/arrow_forward.png", 96, 96))

        nextPlayerPane.addAll(nextPlayerButton, rightArrow)

        playerNamesGridPane[0, 0] = prevPlayerPane
        playerNamesGridPane[1, 0] = currentPlayerPane
        playerNamesGridPane[2, 0] = nextPlayerPane
    }

    private fun initializeDrawnCardsStack() {
        // TODO use drawn cards of shownPlayer to initialize
        val drawnCards = Stack(ParchmentCard(1,ParchmentCardType.WOOD,40))
        drawnCardsStack.clear()
        for (card in drawnCards.peekAll().reversed()) {
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

    private fun initializeClaimedGoalsGridPane() {
        // TODO use claimedGoalCards of shownPlayer
        val claimedGameGoalCards =
            listOf(
                GoalCard(15, GoalColor.BROWN, GoalDifficulty.HARD),
                GoalCard(9, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE),
                GoalCard(9, GoalColor.ORANGE, GoalDifficulty.LOW),
            )
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
