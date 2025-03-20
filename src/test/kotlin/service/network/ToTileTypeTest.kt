package service.network

import entity.TileType
import messages.TileTypeMessage
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class for testing the toTileType method of the MessageConverter class
 */
class ToTileTypeTest {

    /**
     * Test conversion for all Tile Types
     */
    @Test
    fun conversionTest() {
        val messageConverter = MessageConverter()
        assertEquals(TileType.WOOD, messageConverter.toTileType(TileTypeMessage.WOOD))
        assertEquals(TileType.LEAF, messageConverter.toTileType(TileTypeMessage.LEAF))
        assertEquals(TileType.FLOWER, messageConverter.toTileType(TileTypeMessage.FLOWER))
        assertEquals(TileType.FRUIT, messageConverter.toTileType(TileTypeMessage.FRUIT))
    }

}
