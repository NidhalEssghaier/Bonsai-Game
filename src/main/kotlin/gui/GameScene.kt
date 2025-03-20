package gui

import entity.*
import gui.utility.CardImageLoader
import gui.utility.ItemImageLoader
import service.PlayerActionService
import service.RootService
import tools.aqua.bgw.animation.*
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
import tools.aqua.bgw.event.MouseButtonType
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.*

private const val ANIMATION_TIME = 100

/**
 * This is the main scene for the [BonsaiGame]. The scene shows alls game
 * components and allows switching between the different players.
 */
class GameScene(
    val rootService: RootService,
    val application: BonsaiApplication,
) : BoardGameScene(1920, 1080),
    Refreshable {
    private lateinit var bonsaiGame: BonsaiGame
    private val cardImageLoader = CardImageLoader()
    private val itemImageLoader = ItemImageLoader()
    private val cardMap: BidirectionalMap<ZenCard, CardView> = BidirectionalMap()
    private val tileMap: BidirectionalMap<BonsaiTile, HexagonView> = BidirectionalMap()
    private var currentPlayer = -1 // The currently active player
    private var shownPlayer = -1; // The player that is currently visible on the screen
    private val playerColors: MutableList<String> = mutableListOf()
    private var cardStacks: List<CardStack<CardView>> = listOf()
    private var tileToRemove: BonsaiTile? = null

    // drop area to remove bonsai tiles
    private val trashInfo = Label(30, 396, 192, 50, isWrapText = true)
    private val trashArea =
        Area<HexagonView>(
            30,
            446,
            192,
            192,
            visual = itemImageLoader.imageFor("icons/delete_icon.png", 192, 192),
        ).apply {
            dropAcceptor = { dragEvent ->
                when (dragEvent.draggedComponent) {
                    is HexagonView -> true
                    else -> false
                }
            }
            onDragDropped = { dragEvent ->
                rootService.playerActionService.discardTile(tileMap.backward(dragEvent.draggedComponent as HexagonView))
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

    // button to remove selected tile from bonsai
    private val removeTileButton =
        Button(650, 390, 150, 40, "Remove selected tile").apply {
            onMouseClicked = { tileToRemove?.let { tile -> rootService.playerActionService.removeTile(tile) } }
        }

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
        ).apply { isDisabled = true }

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
            visual = Visual.EMPTY,
        ).apply { panMouseButton = MouseButtonType.RIGHT_BUTTON }

    private val playerNamesGridPane =
        GridPane<Pane<LabeledUIComponent>>(830, 1007, 3, 1, 50, layoutFromCenter = false)

    private val endTurnButton =
        Button(1712, 970, 178, 80, text = "End Turn").apply {
            onMouseClicked = { rootService.playerActionService.endTurn() }
            visual =
                ColorVisual(200, 66, 51, 255).apply {
                    style.borderRadius =
                        BorderRadius.MEDIUM
                }
            font = Font(size = 24, fontWeight = Font.FontWeight.BOLD, color = Color.WHITE)
        }

    // elements of the players
    // bonsai tiles views
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

    // bonsai-pots, disabled to allow tile selection for removal
    private val potGrey =
        Label(
            width = 260,
            height = 174,
            visual = itemImageLoader.imageFor("pots/pot_grey.png", 360, 240),
        ).apply { isDisabled = true }
    private val potRed =
        Label(
            width = 260,
            height = 174,
            visual = itemImageLoader.imageFor("pots/pot_red.png", 360, 240),
        ).apply { isDisabled = true }
    private val potBlue =
        Label(
            width = 260,
            height = 174,
            visual = itemImageLoader.imageFor("pots/pot_blue.png", 360, 240),
        ).apply { isDisabled = true }
    private val potPurple =
        Label(
            width = 260,
            height = 174,
            visual = itemImageLoader.imageFor("pots/pot_purple.png", 360, 240),
        ).apply { isDisabled = true }

    // bonsai placing area
    private var playAreaGrey = HexagonGrid<HexagonView>(coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL)
    private var playAreaRed = HexagonGrid<HexagonView>(coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL)
    private var playAreaBlue = HexagonGrid<HexagonView>(coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL)
    private var playAreaPurple = HexagonGrid<HexagonView>(coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL)

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

    // drawn cards
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
            trashArea,
            trashInfo,
            bonsaiTilesView1,
            bonsaiTilesView2,
            toolCardsView,
            toolCardsMultiplierLabel,
            seishiTile,
            growthCardsView,
            removeTileButton,
            playAreaCameraPane,
            playerNamesGridPane,
            claimedGoalCardsGridPane,
            drawnCardsStack,
            endTurnButton,
        )
        playAreaCameraPane.interactive = true
    }

    // refreshes
    override fun refreshAfterStartNewGame() {
        val game = rootService.currentGame
        checkNotNull(game) { "No started game found." }
        bonsaiGame = game

        cardMap.clear()
        tileMap.clear()

        currentPlayer = game.currentState.currentPlayer
        shownPlayer = currentPlayer

        // initialize elements that do not change during the game
        initializePlayerColors(game.currentState.players)
        initializeGameOptionsGridPane()
        initializeGridsForPlayers(game.currentState)

        initializeGameElements(rootService, game)
        initializePlayerView(game)
        application.repaint()
    }

    override fun refreshAfterDrawCard(
        drawnCard: ZenCard,
        drawnCardIndex: Int,
        chooseTilesByBoard: Boolean,
        chooseTilesByCard: Boolean,
    ) {
        shownPlayer = currentPlayer
        initializePlayerView(bonsaiGame)
        endTurnButton.isDisabled = true
        playDrawCardAnimation(drawnCard, drawnCardIndex + 1, chooseTilesByBoard, chooseTilesByCard)
    }

    override fun refreshAfterChooseTile() {
        initializeSupplyTiles(bonsaiGame.currentState)
    }

    override fun refreshAfterEndTurn() {
        currentPlayer = bonsaiGame.currentState.currentPlayer
        shownPlayer = currentPlayer

        cardMap.clear()
        tileMap.clear()
        tileToRemove = null

        initializeGameElements(rootService, bonsaiGame)
        initializePlayerView(bonsaiGame)
    }

    override fun refreshAfterUndoRedo() {
        // undo/redo acts as loading a new game
        refreshAfterStartNewGame()
    }

    override fun refreshAfterDiscardTile(
        tilesToDiscard: Int,
        removedTile: BonsaiTile?,
    ) {
        trashInfo.text = "Supply size contains $tilesToDiscard tiles more than allowed"
        trashInfo.isVisible = true
        trashArea.isVisible = true

        // make tiles removable as they are being disbaled after drawin a card to avoid miss-input
        bonsaiTilesView1.forEach { it.isDraggable = true }
        bonsaiTilesView2.forEach { it.isDraggable = true }

        if (removedTile != null) {
            val view = tileMap.forward(removedTile)
            view.removeFromParent()
            tileMap.remove(removedTile, view)
        }
    }

    override fun refreshAfterDecideGoal() {
        initializeGoalCardGridPane(bonsaiGame)
        initializeClaimedGoalsGridPane(bonsaiGame.currentState)
    }

    override fun refreshAfterPlaceTile(placedTile: BonsaiTile) {
        initializeSupplyTiles(bonsaiGame.currentState)
        initializeBonsai(bonsaiGame.currentState)
        initializePlayArea(bonsaiGame.currentState)
    }

    override fun refreshAfterRemoveTile(tile: BonsaiTile) {
        val tileView = tileMap.forward(tile)
        val playArea =
            when (bonsaiGame.currentState.players[currentPlayer].potColor) {
                PotColor.GRAY -> playAreaGrey
                PotColor.PURPLE -> playAreaPurple
                PotColor.BLUE -> playAreaBlue
                PotColor.RED -> playAreaRed
            }

        // get coordinates of tile
        val coordinateMap = playArea.getCoordinateMap()
        var coordinates: Pair<Int, Int> = Pair(0, 0)
        for ((key, value) in coordinateMap) {
            if (value == tileView) {
                coordinates = key
            }
        }

        // remove tile from bonsai
        tileView.removeFromParent()
        tileMap.removeForward(tile)

        // fill empty space in grid
        val hexagon =
            HexagonView(
                visual = Visual.EMPTY,
                size = 25,
            ).apply {
                dropAcceptor = { dragEvent ->
                    when (dragEvent.draggedComponent) {
                        is HexagonView -> true
                        else -> false
                    }
                }
                onDragDropped = { dragEvent ->
                    rootService.playerActionService.cultivate(
                        tileMap.backward(dragEvent.draggedComponent as HexagonView),
                        r = coordinates.second,
                        q = coordinates.first,
                    )
                }
            }
        playArea[coordinates.first, coordinates.second] = hexagon

        initializeBonsai(bonsaiGame.currentState)
        initializePlayArea(bonsaiGame.currentState)
    }

    // initializes to set up game elements according to the state of the entities
    private fun initializeGameElements(
        rootService: RootService,
        game: BonsaiGame,
    ) {
        initializeGoalCardGridPane(game)
        initializeBoardPane(rootService.playerActionService, game)
        initializeTrash()
    }

    private fun initializePlayerView(game: BonsaiGame) {
        initializeSupplyTiles(game.currentState)
        initializeSeishiTile(game.currentState)
        initializeSeishiCards(game.currentState)
        initializeToolCardsMultiplier()
        initializePlayerNamesGridPane(game)
        initializeClaimedGoalsGridPane(game.currentState)
        initializeDrawnCardsStack(game.currentState)
        initializeBonsai(game.currentState)
        initializePlayArea(game.currentState)
    }

    private fun initializeSupplyTiles(state: GameState) {
        // clear views & map to include only tiles of shownPlayer
        bonsaiTilesView1.clear()
        bonsaiTilesView2.clear()
        tileMap.clear()

        // create tileViews based on player supply
        for ((index, tile) in state.players[shownPlayer].supply.withIndex()) {
            val tileView =
                HexagonView(visual = itemImageLoader.imageFor(tile), size = 25).apply {
                    isDraggable = true
                }
            if (index < 9) {
                bonsaiTilesView1.add(tileView)
            } else {
                bonsaiTilesView2.add(tileView)
            }
            tileMap.add(tile, tileView)
        }
    }

    private fun initializeGridsForPlayers(state: GameState) {
        for (i in 0..<state.players.size) {
            val playArea = HexagonGrid<HexagonView>(coordinateSystem = HexagonGrid.CoordinateSystem.AXIAL)
            val bonsai = bonsaiGame.currentState.players[shownPlayer].bonsai
            val gridSize = bonsai.grid.size

            // create whole grid
            for (row in -gridSize..gridSize) {
                for (col in -gridSize..gridSize) {
                    // Only add hexagons that would fit in a circle
                    if (row + col in -gridSize..gridSize) {
                        val hexagon =
                            HexagonView(
                                visual = Visual.EMPTY,
                                size = 25,
                            ).apply {
                                dropAcceptor = { dragEvent ->
                                    when (dragEvent.draggedComponent) {
                                        is HexagonView -> true
                                        else -> false
                                    }
                                }
                                onDragDropped = { dragEvent ->
                                    rootService.playerActionService.cultivate(
                                        tileMap.backward(dragEvent.draggedComponent as HexagonView),
                                        r = row,
                                        q = col,
                                    )
                                }
                            }
                        playArea[col, row] = hexagon
                    }
                }
            }
            // match play areas by player color
            when (state.players[i].potColor) {
                PotColor.GRAY -> playAreaGrey = playArea
                PotColor.PURPLE -> playAreaPurple = playArea
                PotColor.BLUE -> playAreaBlue = playArea
                PotColor.RED -> playAreaRed = playArea
            }
        }
    }

    private fun initializeBonsai(state: GameState) {
        val playArea =
            when (state.players[shownPlayer].potColor) {
                PotColor.GRAY -> playAreaGrey
                PotColor.PURPLE -> playAreaPurple
                PotColor.BLUE -> playAreaBlue
                PotColor.RED -> playAreaRed
            }

        val bonsai = bonsaiGame.currentState.players[shownPlayer].bonsai
        val bonsaiStructure = bonsai.grid.getCoordinateToTileMap()

        // create bonsai
        bonsaiStructure.forEach { (coordinate, tile) ->
            val row = coordinate.first
            val col = coordinate.second
            val hexagon =
                HexagonView(
                    visual = itemImageLoader.imageFor(tile),
                    size = 25,
                ).apply { onMouseClicked = { tileToRemove = tile } } // make tiles clickable for removal
            playArea[row, col] = hexagon
            tileMap.add(tile, hexagon)
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
        val gameGoalCards = game.currentState.goalCards

        val targetWidths = listOf(125, 139, 179)

        // clear elements
        for (row in 0..<goalCardsPane.rows) {
            for (column in 0..<goalCardsPane.columns) {
                goalCardsPane[column, row] = null
            }
        }

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

    private fun initializeBoardPane(
        playerActionService: PlayerActionService,
        game: BonsaiGame,
    ) {
        // remove existing stackViews to avoid stacking of stackView-objects due to new initialization after refreshOnEndTurn
        cardStacks.forEach { it.removeFromParent() }

        val drawStackView = createDrawStackView(32, 33, game.currentState.drawStack, 0, false)
        val cardStackView1 = createOpenCardView(playerActionService, 216, 33, game.currentState.openCards[0], 1, true)
        val cardStackView2 = createOpenCardView(playerActionService, 371, 33, game.currentState.openCards[1], 2, true)
        val cardStackView3 = createOpenCardView(playerActionService, 525, 33, game.currentState.openCards[2], 3, true)
        val cardStackView4 = createOpenCardView(playerActionService, 679, 33, game.currentState.openCards[3], 4, true)

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
            )

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
        playerActionService: PlayerActionService,
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
                        playerActionService.meditate(cardMap.backward(this.peek()))
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

            if (idx < 3 && bonsaiGame.currentState.players.any { it is NetworkPlayer }) {
                gameOptionsGridPane[idx, 0]?.isDisabled = true
                gameOptionsGridPane[idx, 0]?.isVisible = false
            }
        }
    }

    private fun initializeTrash() {
        trashInfo.isVisible = false
        trashArea.isVisible = false
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

    private fun initializeToolCardsMultiplier() {
        val toolCardsCount = toolCardsView.numberOfComponents()
        if (toolCardsCount > 0) {
            toolCardsMultiplierLabel.text = "*$toolCardsCount"
        } else {
            toolCardsMultiplierLabel.text = ""
        }
    }

    private fun initializeSeishiTile(state: GameState) {
        when (state.players[shownPlayer].potColor) {
            PotColor.GRAY -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_grey.png", 160, 242)
            PotColor.RED -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_red.png", 160, 242)
            PotColor.BLUE -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_blue.png", 160, 242)
            PotColor.PURPLE -> seishiTile.visual = itemImageLoader.imageFor("seishis/seishi_purple.png", 160, 242)
        }
    }

    private fun initializePlayerColors(players: List<Player>) {
        playerColors.clear()
        players.forEach { player ->
            when (player.potColor) {
                PotColor.GRAY -> playerColors.add("#a2aca6")
                PotColor.RED -> playerColors.add("#cc8c7e")
                PotColor.BLUE -> playerColors.add("#409cab")
                PotColor.PURPLE -> playerColors.add("#9a92b8")
            }
        }
    }

    private fun initializePlayerNamesGridPane(game: BonsaiGame) {
        val players = game.currentState.players
        val prevPlayer = (shownPlayer - 1 + players.size) % players.size
        val nextPlayer = (shownPlayer + 1) % players.size

        val prevPlayerPane = Pane<LabeledUIComponent>(0, 0, 150, 40)
        val prevPlayerText =
            if (prevPlayer == currentPlayer) {
                "${players[prevPlayer].name} (Active)"
            } else {
                players[prevPlayer].name
            }
        val prevPlayerButton =
            Button(0, 0, 150, 40, text = prevPlayerText).apply {
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
        val currentPlayerText =
            if (shownPlayer == currentPlayer) {
                "${players[shownPlayer].name} (Active)"
            } else {
                players[shownPlayer].name
            }
        val currentPlayerLabel =
            Label(0, 0, 400, 40, text = currentPlayerText).apply {
                visual =
                    ColorVisual(Color(playerColors[shownPlayer])).apply {
                        style.borderRadius =
                            BorderRadius.MEDIUM
                    }
                font = Font(size = 24, fontWeight = Font.FontWeight.BOLD)
            }
        currentPlayerPane.add(currentPlayerLabel)

        val nextPlayerPane = Pane<LabeledUIComponent>(0, 0, 150, 40)
        val nextPlayerText =
            if (nextPlayer == currentPlayer) {
                "${players[nextPlayer].name} (Active)"
            } else {
                players[nextPlayer].name
            }
        val nextPlayerButton =
            Button(0, 0, 150, 40, text = nextPlayerText).apply {
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
        // show only dummy card as other cards are not relevant for the GUI
        if (drawnCards.isNotEmpty()) {
            drawnCardsStack.add(
                CardView(
                    0,
                    0,
                    143,
                    200,
                    front = cardImageLoader.frontImageFor(drawnCards.last()),
                    back = cardImageLoader.backImage,
                ),
            )
        }
    }

    private fun initializeClaimedGoalsGridPane(state: GameState) {
        val claimedGameGoalCards = state.players[shownPlayer].acceptedGoals
        val targetWidths = listOf(178, 139, 124)
        val targetHeight = 80

        // clear elements
        for (row in 0..<claimedGoalCardsGridPane.rows) {
            for (column in 0..<claimedGoalCardsGridPane.columns) {
                claimedGoalCardsGridPane[column, row] = null
            }
        }

        for ((idx, goalCard) in claimedGameGoalCards.withIndex()) {
            println("idx $idx, goalCard $goalCard")
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

    private fun playDrawCardAnimation(
        card: ZenCard,
        drawnCardIndex: Int,
        chooseTilesByBoard: Boolean,
        chooseTilesByCard: Boolean,
    ) {
        val animations = mutableListOf<Animation>()
        animations.add(getDrawnCardAnimation(drawnCardIndex))
        animations.addAll(getRefillCardsAnimation(drawnCardIndex))
        // Add final animation that triggers tile refresh and tile selection if necessary
        animations.add(
            ScaleAnimation(
                drawnCardsStack,
                1,
                1,
                1,
                1,
                duration = ANIMATION_TIME * bonsaiGame.currentState.gameSpeed,
            ).apply {
                onFinished = {
                    if (chooseTilesByBoard || chooseTilesByCard) {
                        application.chooseTileScene =
                            ChooseTileScene(rootService, application, chooseTilesByBoard, chooseTilesByCard)
                        val game = rootService.currentGame
                        checkNotNull(game)
                        val player = game.currentState.players[game.currentState.currentPlayer]
                        if(player is RandomBot) {
                            val botService = rootService.botService
                            botService.chooseCardLogic(chooseTilesByBoard, chooseTilesByCard)
                            println("choseTile")
                        } else {
                            application.showMenuScene(application.chooseTileScene)
                        }

                    } else {
                        initializeSupplyTiles(bonsaiGame.currentState)
                    }
                    endTurnButton.isDisabled = false
                }
            },
        )
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
                duration = ANIMATION_TIME * bonsaiGame.currentState.gameSpeed,
            ).apply {
                onFinished = {
                    val target =
                        when (cardMap.backward(cardStacks[drawnCardIndex].peek())) {
                            is ToolCard -> toolCardsView
                            is GrowthCard -> growthCardsView
                            else -> drawnCardsStack
                        }

                    target.add(cardStacks[drawnCardIndex].pop())

                    // update toolCardMultiplier if drawn card is tool card
                    if (target == toolCardsView) {
                        initializeToolCardsMultiplier()
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
                        duration = ANIMATION_TIME * bonsaiGame.currentState.gameSpeed,
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
