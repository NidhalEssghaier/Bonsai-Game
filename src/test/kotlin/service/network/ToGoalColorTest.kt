package service.network

import entity.GoalColor
import messages.GoalTileTypeMessage
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class for testing the toGoalColor method of the MessageConverter class
 */
class ToGoalColorTest {

    /**
     * Test conversion for all GoalColors
     */
    @Test
    fun conversionTest() {
        val messageConverter = MessageConverter()
        assertEquals(GoalColor.GREEN, messageConverter.toGoalColor(GoalTileTypeMessage.GREEN))
        assertEquals(GoalColor.BLUE, messageConverter.toGoalColor(GoalTileTypeMessage.BLUE))
        assertEquals(GoalColor.BROWN, messageConverter.toGoalColor(GoalTileTypeMessage.BROWN))
        assertEquals(GoalColor.ORANGE, messageConverter.toGoalColor(GoalTileTypeMessage.ORANGE))
        assertEquals(GoalColor.RED, messageConverter.toGoalColor(GoalTileTypeMessage.PINK))
    }

}
