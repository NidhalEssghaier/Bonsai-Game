package service.network

import entity.TileType
import messages.TileTypeMessage
import org.junit.jupiter.api.BeforeEach
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Class for testing the fromTileType method of the MessageConverter class
 */
class FromTileTypeTest {

    private lateinit var messageConverter: MessageConverter

    /**
     * Set up objects used in multiple tests
     */
    @BeforeEach
    fun setup() {
        messageConverter = MessageConverter()
    }

    /**
     * Test conversion for all valid TileTypes
     */
    @Test
    fun validTileTypes() {
        assertEquals(TileTypeMessage.WOOD, messageConverter.fromTileType(TileType.WOOD))
        assertEquals(TileTypeMessage.LEAF, messageConverter.fromTileType(TileType.LEAF))
        assertEquals(TileTypeMessage.FLOWER, messageConverter.fromTileType(TileType.FLOWER))
        assertEquals(TileTypeMessage.FRUIT, messageConverter.fromTileType(TileType.FRUIT))
    }

    /**
     * Test error handling for invalid TileTypes
     */
    @Test
    fun invalidTileTypes() {
        var exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            messageConverter.fromTileType(TileType.GENERIC)
        })
        assertEquals("This tile type cannot be used here.", exception.message)

        exception = assertFailsWith(exceptionClass = IllegalArgumentException::class, block = {
            messageConverter.fromTileType(TileType.UNPLAYABLE)
        })
        assertEquals("This tile type cannot be used here.", exception.message)
    }

}
