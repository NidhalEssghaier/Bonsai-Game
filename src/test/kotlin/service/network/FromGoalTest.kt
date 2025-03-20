package service.network

import entity.GoalCard
import entity.GoalColor
import entity.GoalDifficulty
import messages.GoalTileTypeMessage
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class for testing the fromGoal method of the MessageConverter class
 */
class FromGoalTest {

    /**
     * Test conversions of multiple variations of goal cards
     */
    @Test
    fun conversionTest() {
        val messageConverter = MessageConverter()
        assertEquals(
            Pair(GoalTileTypeMessage.GREEN, 0),
            messageConverter.fromGoal(GoalCard(6, GoalColor.GREEN, GoalDifficulty.LOW))
        )
        assertEquals(
            Pair(GoalTileTypeMessage.BLUE, 1),
            messageConverter.fromGoal(GoalCard(10, GoalColor.BLUE, GoalDifficulty.INTERMEDIATE))
        )
        assertEquals(
            Pair(GoalTileTypeMessage.ORANGE, 2),
            messageConverter.fromGoal(GoalCard(13, GoalColor.ORANGE, GoalDifficulty.HARD))
        )
        assertEquals(
            Pair(GoalTileTypeMessage.PINK, 0),
            messageConverter.fromGoal(GoalCard(8, GoalColor.RED, GoalDifficulty.LOW))
        )
        assertEquals(
            Pair(GoalTileTypeMessage.BROWN, 1),
            messageConverter.fromGoal(GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE))
        )
    }

}
