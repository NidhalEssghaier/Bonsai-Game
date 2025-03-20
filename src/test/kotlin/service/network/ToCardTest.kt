package service.network

import entity.*
import messages.CardTypeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import service.MessageConverter
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Class for testing the toCard method of the MessageConverter class
 */
class ToCardTest {

    private lateinit var messageConverter: MessageConverter

    /**
     * Set up objects used in multiple tests
     */
    @BeforeEach
    fun setup() {
        messageConverter = MessageConverter()
    }

    /**
     * Test conversion for every growth card and check invalid index error handling
     */
    @Test
    fun growthCardTest() {
        for (i in 0..13) {
            assertEquals(
                when (i) {
                    0, 1, 8, 12 -> GrowthCard(TileType.WOOD, i)
                    2, 3, 9, 10 -> GrowthCard(TileType.LEAF, i)
                    4, 5, 11 -> GrowthCard(TileType.FLOWER, i)
                    6, 7, 13 -> GrowthCard(TileType.FRUIT, i)
                    else -> {}
                },
                messageConverter.toCard(Pair(CardTypeMessage.GROWTH, i))
            )
        }

        val exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            messageConverter.toCard(Pair(CardTypeMessage.GROWTH, 14))
        })
        assertEquals("invalid index", exception.message)
    }

    /**
     * Test conversion for every helper card and check invalid index error handling
     */
    @Test
    fun helperCardTest() {
        for (i in 14..20) {
            assertEquals(
                when (i) {
                    14, 15, 16 -> HelperCard(listOf(TileType.GENERIC, TileType.WOOD), i)
                    17, 18 -> HelperCard(listOf(TileType.GENERIC, TileType.LEAF), i)
                    19 -> HelperCard(listOf(TileType.GENERIC, TileType.FLOWER), i)
                    20 -> HelperCard(listOf(TileType.GENERIC, TileType.FRUIT), i)
                    else -> {}
                },
                messageConverter.toCard(Pair(CardTypeMessage.HELPER, i))
            )
        }

        val exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            messageConverter.toCard(Pair(CardTypeMessage.HELPER, 21))
        })
        assertEquals("invalid index", exception.message)
    }

    /**
     * Test conversion for every master card and check invalid index error handling
     */
    @Test
    fun masterCardTest() {
        for (i in 21..33) {
            assertEquals(
                when (i) {
                    21 -> MasterCard(listOf(TileType.WOOD, TileType.WOOD), i)
                    22, 26 -> MasterCard(listOf(TileType.LEAF, TileType.LEAF), i)
                    23, 29, 30 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF), i)
                    24, 25, 28 -> MasterCard(listOf(TileType.GENERIC), i)
                    27 -> MasterCard(listOf(TileType.LEAF, TileType.FRUIT), i)
                    31 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF, TileType.FLOWER), i)
                    32 -> MasterCard(listOf(TileType.WOOD, TileType.LEAF, TileType.FRUIT), i)
                    33 -> MasterCard(listOf(TileType.LEAF, TileType.FLOWER, TileType.FLOWER), i)
                    else -> {}
                },
                messageConverter.toCard(Pair(CardTypeMessage.MASTER, i))
            )
        }

        val exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            messageConverter.toCard(Pair(CardTypeMessage.MASTER, 34))
        })
        assertEquals("invalid index", exception.message)
    }

    /**
     * Test conversion for every parchment card and check invalid index error handling
     */
    @Test
    fun parchmentCardTest() {
        for (i in 34..40) {
            assertEquals(
                when (i) {
                    34 -> ParchmentCard(2, ParchmentCardType.MASTER, i)
                    35 -> ParchmentCard(2, ParchmentCardType.GROWTH, i)
                    36 -> ParchmentCard(2, ParchmentCardType.HELPER, i)
                    37 -> ParchmentCard(2, ParchmentCardType.FLOWER, i)
                    38 -> ParchmentCard(2, ParchmentCardType.FRUIT, i)
                    39 -> ParchmentCard(1, ParchmentCardType.LEAF, i)
                    40 -> ParchmentCard(1, ParchmentCardType.WOOD, i)
                    else -> {}
                },
                messageConverter.toCard(Pair(CardTypeMessage.PARCHMENT, i))
            )
        }

        val exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            messageConverter.toCard(Pair(CardTypeMessage.PARCHMENT, 41))
        })
        assertEquals("invalid index", exception.message)
    }

    /**
     * Test conversion for every tool card and check invalid index error handling
     */
    @Test
    fun toolCardTest() {
        for (i in 41..46) {
            assertEquals(
                ToolCard(i),
                messageConverter.toCard(Pair(CardTypeMessage.TOOL, i))
            )
        }

        val exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            messageConverter.toCard(Pair(CardTypeMessage.TOOL, 47))
        })
        assertEquals("invalid index", exception.message)
    }

}
