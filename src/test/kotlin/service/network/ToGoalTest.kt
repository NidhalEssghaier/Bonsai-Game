package service.network

import entity.GoalCard
import entity.GoalColor
import entity.GoalDifficulty
import messages.GoalTileTypeMessage
import org.junit.jupiter.api.BeforeEach
import service.MessageConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Class for testing the toGoal method of the MessageConverter class
 */
class ToGoalTest {

    private lateinit var messageConverter: MessageConverter

    /**
     * Set up objects used in multiple tests
     */
    @BeforeEach
    fun setup() {
        messageConverter = MessageConverter()
    }

    /**
     * Test conversion for all green goal cards (including invalid indices)
     */
    @Test
    fun greenGoalsTest() {
        assertEquals(
            GoalCard(6, GoalColor.GREEN, GoalDifficulty.LOW),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.GREEN, 0))
        )
        assertEquals(
            GoalCard(9, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.GREEN, 1))
        )
        assertEquals(
            GoalCard(12, GoalColor.GREEN, GoalDifficulty.HARD),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.GREEN, 2))
        )

        val exception = assertFailsWith(exceptionClass = IllegalStateException::class, block = {
            messageConverter.toGoal(Pair(GoalTileTypeMessage.GREEN, 3))
        })
        assertEquals("invalid goal tier", exception.message)
    }

    /**
     * Test conversion for all brown goal cards (including invalid indices)
     */
    @Test
    fun brownGoalsTest() {
        assertEquals(
            GoalCard(5, GoalColor.BROWN, GoalDifficulty.LOW),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.BROWN, 0))
        )
        assertEquals(
            GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.BROWN, 1))
        )
        assertEquals(
            GoalCard(15, GoalColor.BROWN, GoalDifficulty.HARD),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.BROWN, 2))
        )

        val exception = assertFailsWith(exceptionClass = IllegalStateException::class, block = {
            messageConverter.toGoal(Pair(GoalTileTypeMessage.BROWN, 3))
        })
        assertEquals("invalid goal tier", exception.message)
    }

    /**
     * Test conversion for all red goal cards (including invalid indices)
     */
    @Test
    fun redGoalsTest() {
        assertEquals(
            GoalCard(8, GoalColor.RED, GoalDifficulty.LOW),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.PINK, 0))
        )
        assertEquals(
            GoalCard(12, GoalColor.RED, GoalDifficulty.INTERMEDIATE),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.PINK, 1))
        )
        assertEquals(
            GoalCard(16, GoalColor.RED, GoalDifficulty.HARD),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.PINK, 2))
        )

        val exception = assertFailsWith(exceptionClass = IllegalStateException::class, block = {
            messageConverter.toGoal(Pair(GoalTileTypeMessage.PINK, 3))
        })
        assertEquals("invalid goal tier", exception.message)
    }

    /**
     * Test conversion for all orange goal cards (including invalid indices)
     */
    @Test
    fun orangeGoalsTest() {
        assertEquals(
            GoalCard(9, GoalColor.ORANGE, GoalDifficulty.LOW),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.ORANGE, 0))
        )
        assertEquals(
            GoalCard(11, GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.ORANGE, 1))
        )
        assertEquals(
            GoalCard(13, GoalColor.ORANGE, GoalDifficulty.HARD),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.ORANGE, 2))
        )

        val exception = assertFailsWith(exceptionClass = IllegalStateException::class, block = {
            messageConverter.toGoal(Pair(GoalTileTypeMessage.ORANGE, 3))
        })
        assertEquals("invalid goal tier", exception.message)
    }

    /**
     * Test conversion for all blue goal cards (including invalid indices)
     */
    @Test
    fun blueGoalsTest() {
        assertEquals(
            GoalCard(7, GoalColor.BLUE, GoalDifficulty.LOW),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.BLUE, 0))
        )
        assertEquals(
            GoalCard(10, GoalColor.BLUE, GoalDifficulty.INTERMEDIATE),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.BLUE, 1))
        )
        assertEquals(
            GoalCard(14, GoalColor.BLUE, GoalDifficulty.HARD),
            messageConverter.toGoal(Pair(GoalTileTypeMessage.BLUE, 2))
        )

        val exception = assertFailsWith(exceptionClass = IllegalStateException::class, block = {
            messageConverter.toGoal(Pair(GoalTileTypeMessage.BLUE, 3))
        })
        assertEquals("invalid goal tier", exception.message)
    }

}
