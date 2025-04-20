package service.network

import entity.GoalCard
import entity.GoalColor
import entity.GoalDifficulty
import entity.TileType
import messages.CultivateMessage
import messages.GoalTileTypeMessage
import messages.MeditateMessage
import messages.TileTypeMessage
import org.junit.jupiter.api.BeforeEach
import service.MessageBuilder
import kotlin.test.*

/**
 * Class for testing the MessageBuilder class
 */
class MessageBuilderTest {

    private lateinit var messageBuilder: MessageBuilder
    private lateinit var skipMessage: CultivateMessage

    /**
     * Set up objects used in multiple tests
     */
    @BeforeEach
    fun setup() {
        // Objects to test with
        messageBuilder = MessageBuilder()
        skipMessage = CultivateMessage(listOf(), listOf(), listOf(), listOf())
    }

    /**
     * Test building a message with no actions taken
     */
    @Test
    fun skipTurnTest() {
        // Prepare Test
        val build = messageBuilder.build()

        // Test
        assertEquals(skipMessage, build.first)
        assertNull(build.second)
    }

    /**
     * Test building a message for a full cultivate turn
     */
    @Test
    fun cultivateTest() {
        // Objects to test with
        val cultivateMessage = CultivateMessage(
            listOf(
                Pair(1, 2),
                Pair(6, -3)
            ),
            listOf(
                Pair(TileTypeMessage.FRUIT, Pair(5, 4)),
                Pair(TileTypeMessage.LEAF, Pair(9, -4))
            ),
            listOf(
                Pair(GoalTileTypeMessage.ORANGE, 1),
                Pair(GoalTileTypeMessage.GREEN, 0)
            ),
            listOf(
                Pair(GoalTileTypeMessage.BLUE, 2),
                Pair(GoalTileTypeMessage.GREEN, 1)
            )
        )

        // Prepare Test
        messageBuilder.addRemovedTile(Pair(1, 2))
        messageBuilder.addRemovedTile(Pair(6, -3))
        messageBuilder.addPlacedTile(TileType.FRUIT, Pair(5, 4))
        messageBuilder.addPlacedTile(TileType.LEAF, Pair(9, -4))
        messageBuilder.addClaimedGoal(GoalCard(11, GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE))
        messageBuilder.addClaimedGoal(GoalCard(6, GoalColor.GREEN, GoalDifficulty.LOW))
        messageBuilder.addRenouncedGoal(GoalCard(7, GoalColor.BLUE, GoalDifficulty.HARD))
        messageBuilder.addRenouncedGoal(GoalCard(9, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE))
        val build = messageBuilder.build()

        // Test
        assertEquals(cultivateMessage, build.first)
        assertNull(build.second)
    }

    /**
     * Test building a message for a full meditate
     */
    @Test
    fun meditateTest() {
        // Objects to test with
        val meditateMessage = MeditateMessage(
            listOf(
                Pair(1, 2),
                Pair(6, -3)
            ),
            2,
            listOf(
                Pair(TileTypeMessage.FRUIT, Pair(5, 4)),
                Pair(TileTypeMessage.LEAF, Pair(9, -4))
            ),
            listOf(
                TileTypeMessage.FLOWER,
                TileTypeMessage.WOOD
            ),
            listOf(
                Pair(GoalTileTypeMessage.ORANGE, 1),
                Pair(GoalTileTypeMessage.GREEN, 0)
            ),
            listOf(
                Pair(GoalTileTypeMessage.BLUE, 2),
                Pair(GoalTileTypeMessage.GREEN, 1)
            ),
            listOf()
        )

        // Prepare Test
        messageBuilder.addRemovedTile(Pair(1, 2))
        messageBuilder.addRemovedTile(Pair(6, -3))
        messageBuilder.setDrawnCard(2)
        messageBuilder.addPlacedTile(TileType.FRUIT, Pair(5, 4))
        messageBuilder.addPlacedTile(TileType.LEAF, Pair(9, -4))
        messageBuilder.addDrawnTile(TileType.FLOWER)
        messageBuilder.addDrawnTile(TileType.WOOD)
        messageBuilder.addClaimedGoal(GoalCard(11, GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE))
        messageBuilder.addClaimedGoal(GoalCard(6, GoalColor.GREEN, GoalDifficulty.LOW))
        messageBuilder.addRenouncedGoal(GoalCard(7, GoalColor.BLUE, GoalDifficulty.HARD))
        messageBuilder.addRenouncedGoal(GoalCard(9, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE))
        val build = messageBuilder.build()

        // Test
        assertEquals(meditateMessage, build.second)
        assertNull(build.first)
    }

    /**
     * Test resetting the message builder after building a message
     */
    @Test
    fun resettingTest() {
        // Objects to test with
        val meditateMessage = MeditateMessage(
            listOf(
                Pair(1, 2),
                Pair(6, -3)
            ),
            0,
            listOf(
                Pair(TileTypeMessage.FRUIT, Pair(5, 4)),
                Pair(TileTypeMessage.LEAF, Pair(9, -4))
            ),
            listOf(),
            listOf(
                Pair(GoalTileTypeMessage.ORANGE, 1),
                Pair(GoalTileTypeMessage.GREEN, 0)
            ),
            listOf(
                Pair(GoalTileTypeMessage.BLUE, 2),
                Pair(GoalTileTypeMessage.GREEN, 1)
            ),
            listOf(
                TileTypeMessage.FLOWER,
                TileTypeMessage.FRUIT
            )
        )

        // Prepare first Test
        messageBuilder.addRemovedTile(Pair(1, 2))
        messageBuilder.addRemovedTile(Pair(6, -3))
        messageBuilder.setDrawnCard(0)
        messageBuilder.addPlacedTile(TileType.FRUIT, Pair(5, 4))
        messageBuilder.addPlacedTile(TileType.LEAF, Pair(9, -4))
        messageBuilder.addClaimedGoal(GoalCard(11, GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE))
        messageBuilder.addClaimedGoal(GoalCard(6, GoalColor.GREEN, GoalDifficulty.LOW))
        messageBuilder.addRenouncedGoal(GoalCard(7, GoalColor.BLUE, GoalDifficulty.HARD))
        messageBuilder.addRenouncedGoal(GoalCard(9, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE))
        messageBuilder.addDiscardedTile(TileType.FLOWER)
        messageBuilder.addDiscardedTile(TileType.FRUIT)
        var build = messageBuilder.build()

        // first Test
        assertEquals(meditateMessage, build.second)
        assertNull(build.first)

        // Prepare second Test
        messageBuilder.reset()
        build = messageBuilder.build()

        // second Test
        assertEquals(skipMessage, build.first)
        assertNull(build.second)
    }

    /**
     * Test not drawing a tile where it should have been
     */
    @Test
    fun noDrawnTilesTest() {
        // Prepare Test
        messageBuilder.setDrawnCard(2)

        // Test
        val exception = assertFailsWith(exceptionClass = IllegalStateException::class, block = {
            messageBuilder.build()
        })
        assertEquals("at least one tile should have been drawn", exception.message)
    }

}
