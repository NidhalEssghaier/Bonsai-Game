package service.network

import entity.*
import messages.CardTypeMessage
import org.junit.jupiter.api.BeforeEach
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Class for testing the fromCard method of the MessageConverter class
 */
class FromCardTest {

    private lateinit var messageConverter: MessageConverter

    /**
     * Set up objects used in multiple tests
     */
    @BeforeEach
    fun setup() {
        messageConverter = MessageConverter()
    }

    /**
     * Test conversion for various valid cards
     */
    @Test
    fun validCardTest() {
        assertEquals(
            Pair(CardTypeMessage.TOOL, 43),
            messageConverter.fromCard(ToolCard(43))
        )
        assertEquals(
            Pair(CardTypeMessage.GROWTH, 11),
            messageConverter.fromCard(GrowthCard(TileType.FLOWER, 11))
        )
        assertEquals(
            Pair(CardTypeMessage.MASTER, 27),
            messageConverter.fromCard(MasterCard(listOf(TileType.LEAF, TileType.FRUIT), 27))
        )
        assertEquals(
            Pair(CardTypeMessage.HELPER, 18),
            messageConverter.fromCard(HelperCard(listOf(TileType.GENERIC, TileType.LEAF), 18))
        )
        assertEquals(
            Pair(CardTypeMessage.PARCHMENT, 36),
            messageConverter.fromCard(ParchmentCard(2, ParchmentCardType.HELPER, 36))
        )
    }

    /**
     * Test error handling for the invalid placeholder card
     */
    @Test
    fun invalidCardTest() {
        val exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            messageConverter.fromCard(PlaceholderCard)
        })
        assertEquals("invalid card", exception.message)
    }

}
