package gui

import entity.BonsaiTile
import entity.MasterCard
import entity.TileType
import gui.utility.ItemImageLoader
import service.RootService
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.style.Cursor
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual
import java.util.*

private const val BUTTON_WIDTH = 130
private const val BUTTON_HEIGHT = 150
private const val HORIZONTAL_SPACING = 75

/**
 * [MenuScene] that gets displayed when the player needs to choose one or two tiles after drawing a card.
 * Choices are presented based on if the scene was triggered by drawing a [MasterCard] with [TileType.GENERIC] and / or
 * the position from where the card was taken from gives either a [TileType.WOOD] or [TileType.LEAF]
 *
 * @property chooseByBoard Determines if the scene was triggered by drawing a card from the position where they can choose between a [TileType.WOOD] or [TileType.LEAF]
 * @property chooseByCard Determines if the scene was triggered by drawing a [MasterCard] with [TileType.GENERIC]
 */
class ChooseTileScene(
    val rootService: RootService,
    val application: BonsaiApplication,
    val chooseByBoard: Boolean,
    val chooseByCard: Boolean,
) : MenuScene(1920, 1080),
    Refreshable {
    private val itemImageLoader = ItemImageLoader()
    private var selectedTileByBoard: BonsaiTile? = null
    private var selectedButtonByBoard: Button? = null
    private var selectedTileByCard: BonsaiTile? = null
    private var selectedButtonByCard: Button? = null

    private val titleText = if (chooseByBoard && chooseByCard) "Choose Tiles" else "Choose Tile"
    private val titleLabel =
        Label(760, 50, 400, 100, text = titleText, font = Font(size = 48, fontWeight = Font.FontWeight.BOLD))

    // show options for taking card from field with tile choice (wood / leaf)
    private val selectByBoardPane =
        GridPane<Button>(960, 360, 2, 1, spacing = HORIZONTAL_SPACING, layoutFromCenter = true)

    // show options for taking card with tile choice (all tile types)
    private val selectByCardPane =
        GridPane<Button>(960, 720, 4, 1, spacing = HORIZONTAL_SPACING, layoutFromCenter = true)

    private val confirmButton =
        Button(
            810,
            950,
            300,
            80,
            text = "Confirm",
            font = Font(size = 36, fontWeight = Font.FontWeight.BOLD),
            visual = ColorVisual(169, 169, 169, 255),
        ).apply {
            isDisabled = true
            onMouseClicked = {
                if (chooseByBoard) rootService.playerActionService.applyTileChoice(selectedTileByBoard!!.type, false)
                if (chooseByCard) rootService.playerActionService.applyTileChoice(selectedTileByCard!!.type, true)
            }
        }

    init {
        this.background = Visual.EMPTY

        initializeBoardSelections()
        initializeCardSelections()
        updateConfirmButtonState()

        addComponents(
            titleLabel,
            selectByBoardPane,
            selectByCardPane,
            confirmButton,
        )
    }

    private fun createTileButton(
        tile: BonsaiTile,
        isBoardSelection: Boolean,
    ): Button =
        Button(
            0,
            0,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            visual = itemImageLoader.imageFor(tile, true).apply { style.cursor = Cursor.POINTER },
        ).apply {
            onMouseClicked = {
                // reset selection & select clicked tile
                if (isBoardSelection) {
                    // reset selection visually
                    selectedButtonByBoard?.let { button ->
                        selectedTileByBoard?.let { selectedTile ->
                            button.visual = itemImageLoader.imageFor(selectedTile, true)
                        }
                    }
                    // select clicked element
                    selectedTileByBoard = tile
                    selectedButtonByBoard = this
                } else {
                    // reset selection visually
                    selectedButtonByCard?.let { button ->
                        selectedTileByCard?.let { selectedTile ->
                            button.visual = itemImageLoader.imageFor(selectedTile, true)
                        }
                    }
                    // select clicked element
                    selectedTileByCard = tile
                    selectedButtonByCard = this
                }
                // update visual to show selection
                visual = itemImageLoader.imageFor(tile)

                updateConfirmButtonState()
            }
        }

    private fun initializeBoardSelections() {
        if (!chooseByBoard) {
            selectByBoardPane.isVisible = false
            return
        }

        val tileTypes = listOf(TileType.WOOD, TileType.LEAF)

        tileTypes.forEachIndexed { index, tileType ->
            val tile = BonsaiTile(tileType)
            val button = createTileButton(tile, isBoardSelection = true)
            selectByBoardPane[index, 0] = button
        }
    }

    private fun initializeCardSelections() {
        if (!chooseByCard) {
            selectByCardPane.isVisible = false
            return
        }

        val tileTypes = listOf(TileType.WOOD, TileType.LEAF, TileType.FLOWER, TileType.FRUIT)

        tileTypes.forEachIndexed { index, tileType ->
            val tile = BonsaiTile(tileType)
            val button = createTileButton(tile, isBoardSelection = false)
            selectByCardPane[index, 0] = button
        }
    }

    private fun updateConfirmButtonState() {
        val boardSelectionComplete = !chooseByBoard || selectedTileByBoard != null
        val cardSelectionComplete = !chooseByCard || selectedTileByCard != null

        if (boardSelectionComplete && cardSelectionComplete) {
            confirmButton.isDisabled = false
            confirmButton.visual = ColorVisual(211, 211, 211, 255).apply { style.cursor = Cursor.POINTER }
        }
    }

    override fun refreshAfterChooseTile() {
        application.hideMenuScene()
    }
}
