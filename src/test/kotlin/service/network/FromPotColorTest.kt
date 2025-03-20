package service.network

import entity.PotColor
import messages.ColorTypeMessage
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class for testing the fromPotColor method of the MessageConverter class
 */
class FromPotColorTest {

    /**
     * Test conversion of all pot colors
     */
    @Test
    fun conversionTest() {
        val messageConverter = MessageConverter()
        assertEquals(ColorTypeMessage.RED, messageConverter.fromPotColor(PotColor.RED))
        assertEquals(ColorTypeMessage.BLUE, messageConverter.fromPotColor(PotColor.BLUE))
        assertEquals(ColorTypeMessage.PURPLE, messageConverter.fromPotColor(PotColor.PURPLE))
        assertEquals(ColorTypeMessage.BLACK, messageConverter.fromPotColor(PotColor.GRAY))
    }

}
