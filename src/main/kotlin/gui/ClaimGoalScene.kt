package gui

import entity.GoalCard
import gui.utility.ItemImageLoader
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.style.BorderRadius
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual

/**
 * [MenuScene] that gets displayed when the player's bonsai meets goals
 * after placing a new tile. Allows to deny or claim the belonging goal
 * card.
 *
 * @property rootService The [RootService] to access the other services
 * @property application The running application
 * @property reachedGoals The goals that the player's bonsai fulfills
 */
class ClaimGoalScene(
    private val rootService: RootService,
    private val application: BonsaiApplication,
    private val reachedGoals: List<GoalCard>,
) : MenuScene(1920, 1080) {
    private val itemImageLoader = ItemImageLoader()
    private val targetWidths = listOf(125, 139, 179)
    private val targetHeight = 80

    private var currentGoal: Int = 0
    private val goalCount: Int = reachedGoals.size

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

    private var goal = Label()

    private val deny =
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
        )

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
        )

    init {
        initializeComponents()

        addComponents(
            goalTitle,
            notice,
            goal,
            deny,
            claim,
        )
    }

    private fun initializeComponents() {
        goal =
            Label(
                960 - (targetWidths[reachedGoals[currentGoal].difficulty.ordinal] * 2),
                540 - (targetHeight * 2),
                width = targetWidths[reachedGoals[currentGoal].difficulty.ordinal] * 4,
                height = targetHeight * 4,
                visual =
                    itemImageLoader.imageFor(reachedGoals[currentGoal]).apply {
                        style.borderRadius = BorderRadius.MEDIUM
                    },
            )

        deny.apply {
            onMouseClicked = {
                rootService.playerActionService.decideGoalClaim(reachedGoals[currentGoal], false)
                // show next goal if available
                if (currentGoal < goalCount - 1) {
                    currentGoal++
                    initializeComponents()
                } else {
                    application.hideMenuScene()
                }
            }
        }

        claim.apply {
            onMouseClicked = {
                rootService.playerActionService.decideGoalClaim(reachedGoals[currentGoal], true)
                // show next goal if available
                if (currentGoal < goalCount - 1) {
                    currentGoal++
                    initializeComponents()
                } else {
                    application.hideMenuScene()
                }
            }
        }
    }
}
