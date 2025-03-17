package service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import entity.*
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DecideGoalClaimTest {

    private lateinit var rootService: RootService
    private lateinit var playerActionService: PlayerActionService
    private lateinit var game: BonsaiGame
    private lateinit var testPlayer: Player

    @BeforeEach
    fun setup() {
        rootService = RootService()
        playerActionService = PlayerActionService(rootService)

        // Create a test player
        testPlayer = LocalPlayer("TestPlayer", PotColor.GRAY)

        game = BonsaiGame(
            gameSpeed = 1,
            players = listOf(testPlayer),
            goalCards = mutableListOf(),
            drawStack = ArrayDeque(),
            openCards = mutableListOf()
        )

        rootService.currentGame = game
    }

    @Test
    fun `test decideGoalClaim when claiming a goal`() {
        val goalCard = GoalCard(8, GoalColor.BROWN, GoalDifficulty.LOW)
        game.currentState.goalCards.add(goalCard)

        // Ensure the goal is not already accepted
        assertFalse(testPlayer.acceptedGoals.contains(goalCard))

        // Call the method
        playerActionService.decideGoalClaim(goalCard, claim = true)

        // Verify that the goal was added to acceptedGoals
        assertTrue(testPlayer.acceptedGoals.contains(goalCard))

        // Verify that the goal was removed from the game's goal list
        assertFalse(game.currentState.goalCards.contains(goalCard))
    }

    @Test
    fun `test decideGoalClaim when renouncing a goal adds it to declinedGoals`() {
        val goalCard = GoalCard(8, GoalColor.BROWN, GoalDifficulty.LOW)

        // Ensure the goal is not in declinedGoals initially
        assertFalse(testPlayer.declinedGoals.contains(goalCard))

        // Call the method
        playerActionService.decideGoalClaim(goalCard, claim = false)

        // Verify that the goal is now in declinedGoals
        assertTrue(testPlayer.declinedGoals.contains(goalCard))

        // Ensure it is not in forbiddenGoals (since it was manually declined)
        assertFalse(testPlayer.forbiddenGoals.contains(goalCard))
    }

    @Test
    fun `test decideGoalClaim ensures goals of the same color are forbidden`() {
        val goalCard = GoalCard(8, GoalColor.BROWN, GoalDifficulty.LOW)
        val otherBrownGoal1 = GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE)
        val otherBrownGoal2 = GoalCard(12, GoalColor.BROWN, GoalDifficulty.HARD)

        // Add the goal to be claimed and others of the same color
        game.currentState.goalCards.add(goalCard)
        game.currentState.goalCards.add(otherBrownGoal1)
        game.currentState.goalCards.add(otherBrownGoal2)

        // Claim the goal
        playerActionService.decideGoalClaim(goalCard, claim = true)

        // Verify the claimed goal is in acceptedGoals
        assertTrue(testPlayer.acceptedGoals.contains(goalCard))

        // Verify that the claimed goal is removed from goalCards
        assertFalse(game.currentState.goalCards.contains(goalCard))

        // Verify all same-color goals are forbidden
        assertTrue(testPlayer.forbiddenGoals.contains(otherBrownGoal1))
        assertTrue(testPlayer.forbiddenGoals.contains(otherBrownGoal2))
    }

    @Test
    fun `test decideGoalClaim does not affect previously declined goals`() {
        val goalCard = GoalCard(8, GoalColor.BROWN, GoalDifficulty.LOW)
        val previouslyDeclinedGoal = GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE)

        // The player already declined this goal
        game.currentState.goalCards.add(goalCard)
        testPlayer.declinedGoals.add(previouslyDeclinedGoal)

        // Claim the goal
        playerActionService.decideGoalClaim(goalCard, claim = true)
        // Verify the claimed goal is in acceptedGoals
        assertTrue(testPlayer.acceptedGoals.contains(goalCard))

        // Verify that the previously declined goal remains in declinedGoals
        assertTrue(testPlayer.declinedGoals.contains(previouslyDeclinedGoal))

        // Ensure it is not in forbiddenGoals (since it was manually declined)
        assertFalse(testPlayer.forbiddenGoals.contains(previouslyDeclinedGoal))
    }
}
