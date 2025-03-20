package gui

import entity.Player
import gui.utility.ItemImageLoader
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual

/**
 * [MenuScene] that gets displayed at the end of the game showing the
 * points each player achieved.
 *
 * @param results the points each player received as a list mapped to each
 *    player based on the bonsai score-pad.
 */
class ResultScene(
    private val results: Map<Player, List<Int>>,
) : MenuScene(1920, 1080) {
    private val itemImageLoader = ItemImageLoader()

    // GUI elements
    private val title =
        Label(
            660,
            30,
            600,
            100,
            text = "Results",
            visual = Visual.EMPTY,
            font = Font(size = 96, fontWeight = Font.FontWeight.BOLD),
        )
    private val scorePadView = Label(560, 140, 800, 800, visual = itemImageLoader.imageFor("score_pad.jpg", 1048, 1048))
    private val namesView = GridPane<Label>(700, 160, 4, 1, 10, layoutFromCenter = false)
    private val pointsView = GridPane<Label>(700, 230, 4, 6, 35, layoutFromCenter = false)
    val mainMenuButton =
        Button(
            posX = 810,
            posY = 970,
            width = 300,
            height = 80,
            text = "Main Menu",
            font = Font(33, fontWeight = Font.FontWeight.BOLD),
            visual = ColorVisual(256, 107, 62),
        )

    init {
        results.keys.forEachIndexed { index, player ->
            namesView[index, 0] =
                Label(
                    0,
                    0,
                    150,
                    45,
                    text = player.name,
                    visual = Visual.EMPTY,
                    font = Font(size = 20, fontWeight = Font.FontWeight.BOLD),
                )
        }

        results.values.forEachIndexed { col, pointsList ->
            pointsList.forEachIndexed { row, points ->
                pointsView[col, row] =
                    Label(
                        0,
                        0,
                        130,
                        85,
                        text = points.toString(),
                        visual = Visual.EMPTY,
                        font =
                            when (row) {
                                5 -> Font(size = 48, fontWeight = Font.FontWeight.BOLD)
                                else -> Font(size = 36, fontWeight = Font.FontWeight.BOLD)
                            },
                    )
            }
        }

        addComponents(title, scorePadView, namesView, pointsView, mainMenuButton)
    }
}
