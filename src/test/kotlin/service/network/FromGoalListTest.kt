package service.network

import entity.GoalCard
import entity.GoalColor
import entity.GoalDifficulty
import messages.GoalTileTypeMessage
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class for testing the fromGoalList method of the MessageConverter class
 */
class FromGoalListTest {

    /**
     * Test conversion of a mixed list of goal cards
     */
    @Test
    fun conversionTest() {
        val messageConverter = MessageConverter()
        val goalCards: MutableList<GoalCard?> = mutableListOf(
            GoalCard(12, GoalColor.GREEN, GoalDifficulty.HARD),
            GoalCard(5, GoalColor.BROWN, GoalDifficulty.LOW),
            GoalCard(12, GoalColor.RED, GoalDifficulty.INTERMEDIATE),
            GoalCard(9, GoalColor.ORANGE, GoalDifficulty.LOW),
            GoalCard(15, GoalColor.BROWN, GoalDifficulty.HARD),
            GoalCard(10, GoalColor.BLUE, GoalDifficulty.INTERMEDIATE),
            GoalCard(8, GoalColor.RED, GoalDifficulty.LOW),
            GoalCard(9, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE),
            GoalCard(13, GoalColor.ORANGE, GoalDifficulty.HARD),
            GoalCard(7, GoalColor.BLUE, GoalDifficulty.LOW),
        )
        val goalColors = mutableListOf(
            GoalTileTypeMessage.GREEN,
            GoalTileTypeMessage.BROWN,
            GoalTileTypeMessage.PINK,
            GoalTileTypeMessage.ORANGE,
            GoalTileTypeMessage.BLUE
        )

        assertEquals(goalColors, messageConverter.fromGoalList(goalCards))
    }

}
