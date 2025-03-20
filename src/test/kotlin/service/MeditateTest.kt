package service
import entity.*

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals


class MeditateTest {

    private lateinit var rootService: RootService
    private lateinit var gameService: GameService
    private lateinit var playerActionService: PlayerActionService
    private lateinit var testRefreshable: TestRefreshable
    private lateinit var game: BonsaiGame
    private lateinit var currentPlayer: Player

    @BeforeEach
    fun setup() {
        rootService = RootService()
        gameService = rootService.gameService
        playerActionService = rootService.playerActionService
        testRefreshable = TestRefreshable()
        rootService.addRefreshable(testRefreshable)
        gameService.startNewGame(players = createTestPlayers(), 3, createGoalCards())

        game = rootService.currentGame ?: throw IllegalStateException("Game should be initialized")
        currentPlayer = game.currentState.players[game.currentState.currentPlayer]
    }
     private  fun createTestPlayers(): MutableList<Triple<String, Int, PotColor>> {
        return mutableListOf(
            Triple("Alice", 0, PotColor.RED),
            Triple("Bob", 0, PotColor.PURPLE)
        )
    }

    private fun createGoalCards() : List<GoalColor> {
        return  listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)
    }

    /**
     * Tests meditating with a `HelperCard`.
     * Ensures it is added to the player's hidden deck and triggers the appropriate UI refresh.
     */
    @Test
    fun testMeditateWithHelperCard() {
        val helperCard = HelperCard(listOf(TileType.WOOD, TileType.LEAF), id = 1)
        game.currentState.openCards[0] = helperCard
        playerActionService.meditate(helperCard)
        assertTrue { currentPlayer.hiddenDeck.contains(helperCard) }

    }

    /**
     * Tests meditating with a `ParchmentCard`.
     * Ensures it is added to the player's hidden deck and retains its point value.
     */
    @Test
    fun testMeditateWithParchmentCard() {
        val parchmentCard = ParchmentCard(points = 3, type = ParchmentCardType.WOOD, id = 99)
        game.currentState.openCards[1] = parchmentCard
        playerActionService.meditate(parchmentCard)
        assertTrue { currentPlayer.hiddenDeck.contains(parchmentCard) }
        assertEquals(3, parchmentCard.points) // Ensure points value is preserved
    }

    /**
     * Tests meditating with a `MasterCard`.
     * Ensures it is added to the hidden deck and its tiles are correctly added to the player's supply.
     */
    @Test
    fun testMeditateWithMasterCard() {
        val masterCard = MasterCard(tiles = listOf(TileType.WOOD, TileType.LEAF), id = 2)
        game.currentState.openCards[0] = masterCard
        println("Before meditate: Supply = ${currentPlayer.supply}")
        val supplyBefore = currentPlayer.supply.size
        playerActionService.meditate(masterCard)
        println("After meditate: Supply = ${currentPlayer.supply}")
        assertTrue { currentPlayer.hiddenDeck.contains(masterCard) }
        assertEquals(supplyBefore + 2, currentPlayer.supply.size) // Expecting only +2
        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.WOOD) }
        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.LEAF) }
        assertTrue { testRefreshable.refreshAfterDrawCardCalled }
    }

    @Test
    fun testMeditateWithMasterCardGENERICTile() {
        val masterCard = MasterCard(tiles = listOf(TileType.WOOD, TileType.GENERIC), id = 2)

        game.currentState.openCards[0] = masterCard
        playerActionService.meditate(masterCard)

        assertTrue { currentPlayer.hiddenDeck.contains(masterCard) }
        assertTrue { testRefreshable.refreshAfterDrawCardCalled }
    }
    /**
     * Tests meditating with a `GrowthCard`.
     * Ensures it is correctly added to the player's growth stack.
     */
    @Test
    fun testMeditateWithGrowthCard() {
        val growthCard = GrowthCard(type = TileType.FLOWER, id = 3)
        game.currentState.openCards[3] = growthCard
        playerActionService.meditate(growthCard)
        assertTrue { currentPlayer.seishiGrowth.contains(growthCard) }
        assertTrue { testRefreshable.refreshAfterDrawCardCalled }
    }

    /**
     * Tests meditating with a `ToolCard`.
     * Ensures it is correctly added to the player's tool stack and increases the player's tile limit.
     */
    @Test
    fun testMeditateWithToolCard() {
        val toolCard = ToolCard(id = 4)
        game.currentState.openCards[2] = toolCard
        playerActionService.meditate(toolCard)
        assertTrue { currentPlayer.seishiTool.contains(toolCard) }
        assertEquals(7, currentPlayer.supplyTileLimit) // 2 for the position 2
        assertTrue { testRefreshable.refreshAfterDrawCardCalled }
    }

    /**
     * Tests meditating with a `ToolCard`.
     * Ensures it is correctly added to the player's tool stack and increases the player's tile limit.
     */
    @Test
    fun testMeditateWithPlaceholderCard() {
        game.currentState.openCards[0] = PlaceholderCard

        playerActionService.meditate(PlaceholderCard)

        assertTrue { testRefreshable.refreshAfterDrawCardCalled }
    }

    /**
     * Tests meditating with an invalid card that does not exist in `openCards`.
     * Ensures an exception is thrown.
     */
    @Test
    fun testMeditateWithInvalidCard() {
        val invalidCard = ToolCard(id = 999) // This card does not exist in openCards
        assertThrows<IllegalArgumentException> { playerActionService.meditate(invalidCard) }
    }

    /**
     * Tests meditating when there is no active game.
     * Ensures an exception is thrown.
     */
    @Test
    fun testMeditateWithNoActiveGame() {
        val newRootService = RootService()
        val newPlayerActionService = newRootService.playerActionService
        val toolCard = ToolCard(id = 1)
        assertThrows<IllegalStateException> { newPlayerActionService.meditate(toolCard) }
    }

    /**
     * Tests meditating when `openCards` is empty.
     * Ensures an exception is thrown.
     */
    @Test
    fun testMeditateWithEmptyOpenCards() {
        game.currentState.openCards.clear()
        val toolCard = ToolCard(id = 1)
        assertThrows<IllegalStateException> { playerActionService.meditate(toolCard) }
    }

    /**
     * Checks if a player can meditate only once per turn.
     * The first meditation should succeed, while the second attempt in the same turn should fail.
     */
    @Test
    fun testCannotMeditateMultipleTimesPerTurn() {
        val masterCard = MasterCard(tiles = listOf(TileType.WOOD, TileType.LEAF), id = 2)
        game.currentState.openCards[0] = masterCard

        val supplyBefore = currentPlayer.supply.size

        // First meditate should work
        playerActionService.meditate(masterCard) // Adds WOOD and LEAF
        assertEquals(supplyBefore + 2, currentPlayer.supply.size) // Expect 2 new tiles
        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.WOOD) }
        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.LEAF) }
        assertTrue { testRefreshable.refreshAfterDrawCardCalled }

        // Attempt to meditate again in the same turn
        val anotherCard = GrowthCard(type = TileType.FLOWER, id = 3)
        game.currentState.openCards[1] = anotherCard

        // The player has already drawn a card, so this should fail
        assertThrows<IllegalStateException> { playerActionService.meditate(anotherCard) }
    }

    /**
     * Ensures that after a player ends their turn, the next player is allowed to meditate.
     * This confirms that `hasDrawnCard` is reset when the turn changes.
     */
    @Test
    fun testNextPlayerCanMeditateAfterEndTurn() {
        val masterCard = MasterCard(tiles = listOf(TileType.WOOD, TileType.LEAF), id = 2)
        game.currentState.openCards[0] = masterCard

        val supplyBefore = currentPlayer.supply.size

        // First player meditates
        playerActionService.meditate(masterCard)
        assertEquals(supplyBefore + 2, currentPlayer.supply.size) // Expect 2 new tiles
        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.WOOD) }
        assertTrue { currentPlayer.supply.map { it.type }.contains(TileType.LEAF) }
        assertTrue { testRefreshable.refreshAfterDrawCardCalled }

        // End turn -> Switch to next player
        playerActionService.endTurn()

        // Verify next player is active
        val nextPlayer = game.currentState.players[game.currentState.currentPlayer]
        assertNotEquals(currentPlayer, nextPlayer)

        println("Next player before meditation: ${nextPlayer.name}, hasDrawnCard = ${nextPlayer.hasDrawnCard}")

        // Reset test state for the new player
        val nextCard = GrowthCard(type = TileType.FLOWER, id = 3)
        game.currentState.openCards[0] = nextCard
        val seishiGrowthBefore = nextPlayer.seishiGrowth.size

        // Next player meditates successfully
        playerActionService.meditate(nextCard)

        println("Next player after meditation: ${nextPlayer.name}, hasDrawnCard = ${nextPlayer.hasDrawnCard}")

        // Ensure the next player's meditation worked
        assertEquals(seishiGrowthBefore + 1, nextPlayer.seishiGrowth.size)
    }

}

