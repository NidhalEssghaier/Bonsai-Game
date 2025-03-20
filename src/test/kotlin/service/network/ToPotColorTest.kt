package service.network

import entity.PotColor
import messages.ColorTypeMessage
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class for testing the toPotColor method of the MessageConverter class
 */
class ToPotColorTest {

    /**
     * Test conversion of all pot colors
     */
    @Test
    fun conversionTest() {
        val messageConverter = MessageConverter()
        assertEquals(PotColor.RED, messageConverter.toPotColor(ColorTypeMessage.RED))
        assertEquals(PotColor.BLUE, messageConverter.toPotColor(ColorTypeMessage.BLUE))
        assertEquals(PotColor.PURPLE, messageConverter.toPotColor(ColorTypeMessage.PURPLE))
        assertEquals(PotColor.GRAY, messageConverter.toPotColor(ColorTypeMessage.BLACK))
    }

}
