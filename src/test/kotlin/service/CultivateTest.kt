/*package service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import entity.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class CultivateTest {

    private lateinit var rootService: RootService
    private lateinit var playerActionService: PlayerActionService
    private lateinit var game: BonsaiGame
    private lateinit var testPlayer: Player
    private lateinit var bonsai: Bonsai

    @BeforeEach
    fun setup() {
        rootService = RootService()
        playerActionService = PlayerActionService(rootService)

        // Create a test player
        testPlayer = LocalPlayer("TestPlayer", PotColor.GRAY)

        // Initialize Bonsai correctly
        bonsai = Bonsai()

        // Set up the game with the test player
        game = BonsaiGame(
            gameSpeed = 1,
            players = listOf(testPlayer),
            goalCards = mutableListOf(),
            drawStack = ArrayDeque(),
            openCards = mutableListOf()
        )

        // Assign bonsai to the test player
        testPlayer.bonsai = bonsai

        rootService.currentGame = game
    }

    @Test
    fun `test cultivate with valid tile and no helper card`() {
        val woodTile = BonsaiTile(TileType.WOOD)
        testPlayer.supply.add(woodTile) // Ensure tile is in the player's supply

        // Cultivate a tile legally
        playerActionService.cultivate(woodTile, 0, -1)

        // Verify tile placement
        assertTrue(bonsai.grid.isNotEmpty(0, -1))
        assertFalse(testPlayer.supply.contains(woodTile))
    }

    @Test
    fun `test cultivate with valid tile using helper card`() {
        val leafTile = BonsaiTile(TileType.LEAF)
        testPlayer.supply.add(leafTile)

        val helperCard = HelperCard(listOf(TileType.LEAF, TileType.GENERIC), id = 1)
        testPlayer.hiddenDeck.add(helperCard)

        playerActionService.cultivate(leafTile, 0, -1)

        // Verify tile placement
        assertTrue(bonsai.grid.isNotEmpty(0, -1))
        assertFalse(testPlayer.supply.contains(leafTile))

        // Verify that the helper card was partially used
        assertTrue(testPlayer.usedHelperTiles.contains(TileType.LEAF))
        assertFalse(testPlayer.usedHelperCards.contains(helperCard)) // Should not be removed yet
    }

    @Test
    fun `test cultivate with invalid tile placement`() {
        val fruitTile = BonsaiTile(TileType.FRUIT)
        testPlayer.supply.add(fruitTile)

        // Attempt to place fruit tile where it's not allowed
        val exception = assertThrows<IllegalStateException> {
            playerActionService.cultivate(fruitTile, 0, -1)
        }

        assertTrue(exception.message!!.contains("Tile placement not allowed"))
    }

    @Test
    fun `test cultivate with helper card and both tiles used`() {
        val woodTile = BonsaiTile(TileType.WOOD)
        val leafTile = BonsaiTile(TileType.LEAF)
        testPlayer.supply.addAll(listOf(woodTile, leafTile))

        val helperCard = HelperCard(listOf(TileType.WOOD, TileType.LEAF), id = 2)
        testPlayer.hiddenDeck.add(helperCard)

        // Place first tile
        playerActionService.cultivate(woodTile, 0, -1)
        assertTrue(testPlayer.usedHelperTiles.contains(TileType.WOOD))
        assertFalse(testPlayer.usedHelperCards.contains(helperCard)) // Not fully used

        // Place second tile
        playerActionService.cultivate(leafTile, 0, -2)
        assertTrue(testPlayer.usedHelperTiles.contains(TileType.LEAF))
        assertTrue(testPlayer.usedHelperCards.contains(helperCard)) // Now it should be removed
    }

    @Test
    fun `test cultivate with tile not in personal supply`() {
        val tile = BonsaiTile(TileType.WOOD)

        val exception = assertThrows<IllegalStateException> {
            playerActionService.cultivate(tile, 0, -1)
        }

        assertTrue(exception.message!!.contains("this Tile is not in your personal supply"))
    }
}*/
