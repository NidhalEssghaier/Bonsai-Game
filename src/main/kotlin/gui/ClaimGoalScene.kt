package gui

import entity.GoalCard
import gui.utility.ItemImageLoader
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual

class ClaimGoalScene(
    private val goalCard: GoalCard,
) : MenuScene(1920, 1080) {
    private val itemImageLoader = ItemImageLoader()
    private val targetWidths = listOf(125, 139, 179)
    private val targetHeight = 80

    private val goalTitle =
        Label(
            760,
            50,
            400,
            80,
            "Claim Goal?",
            visual = Visual.EMPTY,
            font = Font(size = 66, fontWeight = Font.FontWeight.BOLD),
        )

    private val notice =
        Label(
            660,
            150,
            600,
            50,
            "Notice: This is your only chance to claim this goal.",
            visual = Visual.EMPTY,
            font = Font(size = 24, fontWeight = Font.FontWeight.BOLD),
        )

    private val goal =
        Label(
            960 - (targetWidths[goalCard.difficulty.ordinal] * 2),
            540 - (targetHeight * 2),
            width = targetWidths[goalCard.difficulty.ordinal] * 4,
            height = targetHeight * 4,
            visual =
                itemImageLoader.imageFor(goalCard).apply {
                    style.borderRadius = BorderRadius.MEDIUM
                },
        )

    val deny =
        Button(
            710,
            900,
            150,
            50,
            "Deny",
            visual =
                ColorVisual(200, 50, 50, 255).apply {
                    style.borderRadius =
                        BorderRadius.MEDIUM
                },
            font = Font(size = 24, fontWeight = Font.FontWeight.BOLD, color = Color.WHITE),
        ).apply {
            onMouseClicked = {
                // TODO call service denyGoal
            }
        }

    private val claim =
        Button(
            1060,
            900,
            150,
            50,
            "Claim",
            visual =
                ColorVisual(50, 200, 50, 255).apply {
                    style.borderRadius =
                        BorderRadius.MEDIUM
                },
            font = Font(size = 24, fontWeight = Font.FontWeight.BOLD),
        ).apply {
            onMouseClicked = {
                // TODO call service claimGoal
            }
        }

    init {

        addComponents(
            goalTitle,
            notice,
            goal,
            deny,
            claim,
        )
    }
}
