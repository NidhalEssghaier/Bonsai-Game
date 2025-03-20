package service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import entity.*
import helper.pop
import helper.push
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
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
        testPlayer.supply.add(woodTile)

        playerActionService.cultivate(woodTile, 0, -1)

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

        assertTrue(bonsai.grid.isNotEmpty(0, -1))
        assertFalse(testPlayer.supply.contains(leafTile))

        // Verify that the helper card was partially used
        assertTrue(testPlayer.usedHelperTiles.contains(TileType.LEAF))
        assertFalse(testPlayer.usedHelperCards.contains(helperCard)) // Not fully used yet
    }

    @Test
    fun `test cultivate with generic helper tile`() {
        val woodTile = BonsaiTile(TileType.WOOD)
        testPlayer.supply.add(woodTile)

        val helperCard = HelperCard(listOf(TileType.GENERIC, TileType.LEAF), id = 2)
        testPlayer.hiddenDeck.add(helperCard)

        playerActionService.cultivate(woodTile, 0, -1)

        assertTrue(bonsai.grid.isNotEmpty(0, -1))
        assertFalse(testPlayer.supply.contains(woodTile))

        // Verify that the generic tile was used
        assertTrue(testPlayer.usedHelperTiles.contains(TileType.GENERIC))
        assertFalse(testPlayer.usedHelperCards.contains(helperCard)) // Not fully used yet
    }

    @Test
    fun `test cultivate violates helper card rules`() {
        val flowerTile = BonsaiTile(TileType.FLOWER)
        testPlayer.supply.add(flowerTile)

        val helperCard = HelperCard(listOf(TileType.WOOD, TileType.LEAF), id = 3)
        testPlayer.hiddenDeck.add(helperCard)

        val exception = assertThrows<IllegalStateException> {
            playerActionService.cultivate(flowerTile, 0, -1)
        }

        assertTrue(exception.message!!.contains("violated placing rules according to Helper Card"))
    }

    @Test
    fun `test cultivate with invalid tile placement`() {
        val fruitTile = BonsaiTile(TileType.FRUIT)
        testPlayer.supply.add(fruitTile)

        val exception = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(fruitTile, 0, -1)
        }

        assertTrue(exception.message!!.contains("A fruit tile must be placed between two adjacent leaf tiles."))
    }



    @Test
    fun `test cultivate with helper card and both tiles used`() {
        val woodTile = BonsaiTile(TileType.WOOD)
        val leafTile = BonsaiTile(TileType.LEAF)
        testPlayer.supply.addAll(listOf(woodTile, leafTile))

        val helperCard = HelperCard(listOf(TileType.WOOD, TileType.LEAF), id = 4)
        testPlayer.hiddenDeck.add(helperCard)

        playerActionService.cultivate(woodTile, 0, -1)
        assertTrue(testPlayer.usedHelperTiles.contains(TileType.WOOD))
        assertFalse(testPlayer.usedHelperCards.contains(helperCard)) // Not fully used

        playerActionService.cultivate(leafTile, 0, -2)
        assertTrue(testPlayer.usedHelperTiles.contains(TileType.LEAF))
        assertTrue(testPlayer.usedHelperCards.contains(helperCard)) // Now fully used
    }

    @Test
    fun `test cultivate with tile not in personal supply`() {
        val tile = BonsaiTile(TileType.WOOD)

        val exception = assertThrows<IllegalStateException> {
            playerActionService.cultivate(tile, 0, -1)
        }

        assertTrue(exception.message!!.contains("this Tile is not in your personal supply"))
    }


    @Test
    fun `test general placing rules`() {
        val woodTiles = List(8) { BonsaiTile(TileType.WOOD) }
        val leafTiles = List(8) { BonsaiTile(TileType.LEAF) }
        val flowerTiles = List(8) { BonsaiTile(TileType.FLOWER) }
        val fruitTiles = List(8) { BonsaiTile(TileType.FRUIT) }
        val invalidTile = BonsaiTile(TileType.UNPLAYABLE)

        testPlayer.supply.addAll(woodTiles)
        testPlayer.supply.addAll(leafTiles)
        testPlayer.supply.addAll(flowerTiles)
        testPlayer.supply.addAll(fruitTiles)
        testPlayer.supply.add(invalidTile)

        // Test placing wood tile next to a wood tile
        playerActionService.cultivate(woodTiles[0], 0, -1)
        assertTrue(bonsai.grid.isNotEmpty(0, -1))

        playerActionService.cultivate(woodTiles[1], 1, -1)
        assertTrue(bonsai.grid.isNotEmpty(1, -1))

        //  Test placing wood tile without adjacency
        val exceptionWood = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(woodTiles[2], 3, -3)
        }
        assertTrue(exceptionWood.message!!.contains("A wood tile must be placed adjacent to another wood tile."))

        //  Test placing leaf tile next to a wood tile
        playerActionService.cultivate(leafTiles[0], -1, -1)
        assertTrue(bonsai.grid.isNotEmpty(-1, -1))

        //  Test placing leaf tile without adjacency to a wood tile
        val exceptionLeaf = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(leafTiles[1], 3, -3)
        }
        assertTrue(exceptionLeaf.message!!.contains("A leaf tile must be placed adjacent to a wood tile."))

        //  Test placing flower tile next to a leaf tile
        playerActionService.cultivate(flowerTiles[0], 0, -2)
        assertTrue(bonsai.grid.isNotEmpty(0, -2))

        //  Test placing flower tile without adjacency to a leaf tile
        val exceptionFlower = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(flowerTiles[1], 4, -4)
        }
        assertTrue(exceptionFlower.message!!.contains("A flower tile must be placed adjacent to a leaf tile."))

        //  Test placing fruit tile between two adjacent leaf tiles
        playerActionService.cultivate(leafTiles[1], 1, -2)
        playerActionService.cultivate(leafTiles[2], 2, -2)
        playerActionService.cultivate(fruitTiles[0], 2, -3)
        assertTrue(bonsai.grid.isNotEmpty(2, -3))

        //  Test placing fruit tile with only one adjacent leaf tile
        val exceptionFruitOneLeaf = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(fruitTiles[1], 3, -3)
        }
        assertTrue(exceptionFruitOneLeaf.message!!.contains("A fruit tile must be placed between two adjacent leaf tiles."))

        //  Test placing fruit tile adjacent to another fruit tile
        playerActionService.cultivate(woodTiles[7], 2, -1)
        playerActionService.cultivate(leafTiles[3], 3, -2)
        val exceptionFruitAdjacent = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(fruitTiles[2], 3, -3)
        }
        assertTrue(exceptionFruitAdjacent.message!!.contains("A fruit tile cannot be placed adjacent to another fruit tile."))

        //  Test placing tile on an occupied position
        val exceptionOccupied = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(woodTiles[3], 1, -1)
        }
        assertTrue(exceptionOccupied.message!!.contains("Cannot place a tile on top of another tile."))



        //  Test placing tile inside pot area
        val exceptionPot = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(woodTiles[4], 1, 0)
        }
        assertTrue(exceptionPot.message!!.contains("Cannot place a tile in the Pot Area."))

        //  Test placing a fruit tile between two non adjacent leaf tiles

        playerActionService.cultivate(woodTiles[5], 3, -1)
        playerActionService.cultivate(leafTiles[4], 4, -1)

        //  Test placing a fruit tile when two leaf tiles are NOT adjacent
        val exceptionFruitNonAdjacentLeaves = assertThrows<IllegalArgumentException> {
            playerActionService.cultivate(fruitTiles[3], 4, -2)
        }
        assertTrue(exceptionFruitNonAdjacentLeaves.message!!.contains("The two leaf tiles must also be adjacent to each other."))

        //  Test placing an invalid tile type
        val exceptionInvalidTile = assertThrows<IllegalStateException> {
            playerActionService.cultivate(invalidTile, -2, -1)
        }
        println(exceptionInvalidTile.message)
        assertTrue(exceptionInvalidTile.message!!.contains("Invalid tile type for placement."))
    }


    @Test
    fun `test cultivate triggers green,brown and orange goals `() {
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE),
            Triple("Iyed",1,PotColor.PURPLE)),5, listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE))

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)

        val currentPlayer = game.currentState.players[0]
        val currentPlayer1 = game.currentState.players[1]
        val woodTiles = List(12) { BonsaiTile(TileType.WOOD) }
        val leafTiles = List(9) { BonsaiTile(TileType.LEAF) }

        currentPlayer.supply.addAll(woodTiles + leafTiles)
        currentPlayer1.supply.addAll(woodTiles + leafTiles)
        currentPlayer.treeTileLimit =mutableMapOf(
            TileType.GENERIC to 101,
            TileType.WOOD to 151,
            TileType.LEAF to 131,
            TileType.LEAF to 131,
            TileType.LEAF to 131,
        )
        currentPlayer1.treeTileLimit =mutableMapOf(
            TileType.GENERIC to 101,
            TileType.WOOD to 151,
            TileType.LEAF to 131,
            TileType.LEAF to 131,
            TileType.LEAF to 131,
        )


        val greenCardLow = GoalCard(5, GoalColor.GREEN, GoalDifficulty.LOW)
        val greenCardIntermediate = GoalCard(7, GoalColor.GREEN, GoalDifficulty.INTERMEDIATE)
        val greenCardHard = GoalCard(9, GoalColor.GREEN, GoalDifficulty.HARD)
        game.currentState.goalCards.add(greenCardLow)
        game.currentState.goalCards.add(greenCardIntermediate)
        game.currentState.goalCards.add(greenCardHard)

        val brownCardLow = GoalCard(5, GoalColor.BROWN, GoalDifficulty.LOW)
        val brownCardINTERMEDIATE = GoalCard(10, GoalColor.BROWN, GoalDifficulty.INTERMEDIATE)
        val brownCardHARD = GoalCard(15, GoalColor.BROWN, GoalDifficulty.HARD)

        game.currentState.goalCards.add(brownCardLow)
        game.currentState.goalCards.add(brownCardINTERMEDIATE)
        game.currentState.goalCards.add(brownCardHARD)


        // placement to check BROWN goal cards
        /*val flowerGrowth = GrowthCard(TileType.FLOWER,21)
        val woodGrowth = GrowthCard(TileType.WOOD,21)
        for (i in 1..20){
            game.currentState.openCards.add(woodGrowth)
            playerActionService.meditate(woodGrowth)
            playerActionService.endTurn()

        }*/
        playerActionService.endTurn()
        playerActionService.cultivate(woodTiles[0], 0, -1)
        playerActionService.cultivate(woodTiles[1], 1, -1)
        playerActionService.cultivate(woodTiles[2], -1, -1)
        playerActionService.cultivate(woodTiles[3], -2, -1)
        playerActionService.cultivate(woodTiles[4], 2, -1)
        playerActionService.cultivate(woodTiles[5], 3, -1)
        playerActionService.cultivate(woodTiles[6], 4, -1)
        playerActionService.cultivate(woodTiles[7], 5, -1)
        playerActionService.cultivate(woodTiles[8], 6, -1)
        playerActionService.cultivate(woodTiles[9], 7, -1)
        playerActionService.cultivate(woodTiles[10], 8, -1)
        playerActionService.cultivate(woodTiles[11], 9, -1)

        assertTrue(game.currentState.goalCards.contains(brownCardLow))
        assertTrue(game.currentState.goalCards.contains(brownCardINTERMEDIATE))
        assertTrue(game.currentState.goalCards.contains(brownCardHARD))


        // placement to check low green goal card
        playerActionService.cultivate(leafTiles[0], 0, -2)
        playerActionService.cultivate(leafTiles[1], 1, -2)
        playerActionService.cultivate(leafTiles[2], -1, -2)
        playerActionService.cultivate(leafTiles[3], -2, -2)
        playerActionService.cultivate(leafTiles[4], 2, -2)

        //  Verify that the low green goal is met
        assertTrue(game.currentState.goalCards.contains(greenCardLow))

        // placement to check intermediate green goal card
        playerActionService.cultivate(leafTiles[5], -3, -1)
        playerActionService.cultivate(leafTiles[6], 3, -2)

        //  Verify that the intermediate goal is met
        assertTrue(game.currentState.goalCards.contains(greenCardIntermediate))

        // placement to check orange goal cards
        val fruitTiles = List(5) { BonsaiTile(TileType.FRUIT) }
        testPlayer.supply.addAll(fruitTiles)

        val orangeLow = GoalCard(9, GoalColor.ORANGE, GoalDifficulty.LOW)
        val orangeIntermediate = GoalCard(11, GoalColor.ORANGE, GoalDifficulty.INTERMEDIATE)
        val orangeHard = GoalCard(13, GoalColor.ORANGE, GoalDifficulty.HARD)

        game.currentState.goalCards.add(orangeLow)
        game.currentState.goalCards.add(orangeIntermediate)
        game.currentState.goalCards.add(orangeHard)

        playerActionService.cultivate(leafTiles[7], -3, 0)

        playerActionService.cultivate(fruitTiles[0], -4, 0)
        playerActionService.cultivate(fruitTiles[1], -3, -2)
        playerActionService.cultivate(fruitTiles[2], -1, -3)
        playerActionService.cultivate(fruitTiles[3], 1, -3)
        playerActionService.cultivate(fruitTiles[4], 3, -3)

        assertTrue(game.currentState.goalCards.contains(orangeLow))
        assertTrue(game.currentState.goalCards.contains(orangeIntermediate))
        assertTrue(game.currentState.goalCards.contains(orangeHard))


    }

    @Test
    fun `test cultivate triggers red and blue goals  for all difficulties`() {
        //  Place the foundation of 7 Wood Tiles
        val woodTiles = List(15) { BonsaiTile(TileType.WOOD) }
        testPlayer.supply.addAll(woodTiles)

        playerActionService.cultivate(woodTiles[0], 0, -1)
        playerActionService.cultivate(woodTiles[1], 1, -1)
        playerActionService.cultivate(woodTiles[2], -1, -1)
        playerActionService.cultivate(woodTiles[3], -2, -1)
        playerActionService.cultivate(woodTiles[4], 2, -1)
        playerActionService.cultivate(woodTiles[5], 3, -1)
        playerActionService.cultivate(woodTiles[6], 4, -1)

        //  Place Leaf Tiles (2 on the left, 2 on the right)
        val leafTiles = List(4) { BonsaiTile(TileType.LEAF) }
        testPlayer.supply.addAll(leafTiles)

        playerActionService.cultivate(leafTiles[0], -3, -1)
        playerActionService.cultivate(leafTiles[1], -2, -2)
        playerActionService.cultivate(leafTiles[2], 5, -1)
        playerActionService.cultivate(leafTiles[3], 5, -2)

        //  Place 10 Flower Tiles (5 on the left, 5 on the right)
        val flowerTiles = List(10) { BonsaiTile(TileType.FLOWER) }
        testPlayer.supply.addAll(flowerTiles)

        // Left side Flowers
        playerActionService.cultivate(flowerTiles[0], -4, 0)
        playerActionService.cultivate(flowerTiles[1], -4, -1)
        playerActionService.cultivate(flowerTiles[2], -3, -2)
        playerActionService.cultivate(flowerTiles[3], -2, -3)
        playerActionService.cultivate(flowerTiles[4], -1, -3)

        // Right side Flowers
        playerActionService.cultivate(flowerTiles[5], 5, 0)
        playerActionService.cultivate(flowerTiles[6], 6, -1)
        playerActionService.cultivate(flowerTiles[7], 6, -2)
        playerActionService.cultivate(flowerTiles[8], 6, -3)
        playerActionService.cultivate(flowerTiles[9], 5, -3)

        //  Add GoalCard and Check for Achievement
        val goalCardLow = GoalCard(3, GoalColor.RED, GoalDifficulty.LOW)
        val goalCardIntermediate = GoalCard(4, GoalColor.RED, GoalDifficulty.INTERMEDIATE)
        val goalCardHard = GoalCard(5, GoalColor.RED, GoalDifficulty.HARD)
        game.currentState.goalCards.add(goalCardLow)
        game.currentState.goalCards.add(goalCardIntermediate)
        game.currentState.goalCards.add(goalCardHard)

        // Ensure goal is met after placement
        assertTrue(game.currentState.goalCards.contains(goalCardLow))
        assertTrue(game.currentState.goalCards.contains(goalCardIntermediate))
        assertTrue(game.currentState.goalCards.contains(goalCardHard))

        // check blue goal cards
        val goalBlueLow = GoalCard(7, GoalColor.BLUE, GoalDifficulty.LOW)
        val goalBlueIntermediate = GoalCard(10, GoalColor.BLUE, GoalDifficulty.INTERMEDIATE)
        val goalBlueHARD = GoalCard(14, GoalColor.BLUE, GoalDifficulty.HARD)

        game.currentState.goalCards.add(goalBlueLow)
        game.currentState.goalCards.add(goalBlueIntermediate)
        game.currentState.goalCards.add(goalBlueHARD)

        assertTrue(game.currentState.goalCards.contains(goalBlueLow))
        assertTrue(game.currentState.goalCards.contains(goalBlueIntermediate))

        playerActionService.cultivate(woodTiles[7], -3, 0)
        playerActionService.cultivate(woodTiles[8], -4, 1)
        playerActionService.cultivate(woodTiles[9], -5, 2)
        playerActionService.cultivate(woodTiles[10], -5, 3)
        playerActionService.cultivate(woodTiles[11], 4, 0)
        playerActionService.cultivate(woodTiles[12], 4, 1)
        playerActionService.cultivate(woodTiles[13], 3, 2)
        playerActionService.cultivate(woodTiles[14], 2, 3)

        assertTrue(game.currentState.goalCards.contains(goalBlueHARD))

        assertTrue(game.currentState.goalCards.contains(goalBlueHARD))

    }

    @Test
    fun `test cultivate not in seishi tile`(){

        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE))

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)
        var currentPlayer = game.currentState.players[game.currentState.currentPlayer]

        //test with no growth cards
        assertEquals(currentPlayer.treeTileLimit,mutableMapOf(
            TileType.GENERIC to 1,
            TileType.WOOD to 1,
            TileType.LEAF to 1))

        //test with growth card already in seishi
        val leafGrowth =GrowthCard(TileType.LEAF,22)
        game.currentState.openCards[0]= leafGrowth
        playerActionService.meditate(leafGrowth)
        assertEquals(currentPlayer.treeTileLimit,mutableMapOf(
            TileType.GENERIC to 1,
            TileType.WOOD to 1,
            TileType.LEAF to 2))

        playerActionService.endTurn()
        currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        //test with growth card not in seishi
        val flowerGrowth = GrowthCard(TileType.FLOWER,21)
        game.currentState.openCards[1]= flowerGrowth
        playerActionService.meditate(flowerGrowth)
        assertEquals(currentPlayer.treeTileLimit,mutableMapOf(
            TileType.GENERIC to 1,
            TileType.WOOD to 1,
            TileType.FLOWER to 1,
            TileType.LEAF to 1),)
    }
    @Test
    fun `test cultivate with not allowed by Seishi or Growth Cards`() {

        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)
        gameService.startNewGame(listOf(
            Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE))
        //   allowed tiles (Seishi or Growth)
        val game = mc.currentGame
        checkNotNull(game)
        val  currentPlayer = game.currentState.players[game.currentState.currentPlayer]
        val woodTile1 = BonsaiTile(TileType.WOOD)
        val leafTile = BonsaiTile(TileType.LEAF)
        val woodTile2 = BonsaiTile(TileType.WOOD)
        currentPlayer.supply.add(woodTile1)
        currentPlayer.supply.add(woodTile2)
        currentPlayer.supply.add(leafTile)

        // Allowed by generic
        assertDoesNotThrow {  playerActionService.cultivate(woodTile1, 0, -1)}  // Allowed by Seishi
        assertDoesNotThrow {  playerActionService.cultivate(woodTile2, 0, -2)}
        assertDoesNotThrow {  playerActionService.cultivate(leafTile, 0, -3) } // Allowed by Seishi

        //invalid place tile
        val flowerTile1 = BonsaiTile(TileType.FLOWER) // Another Fruit tile (not covered)
        currentPlayer.supply.add(flowerTile1)
        val exception = assertThrows<IllegalStateException> {
            playerActionService.cultivate(flowerTile1,0,-4)
        }
        assertEquals("Tile placement not allowed based on Seishi StartingTile ans Growth Cards.",
            exception.message)

        playerActionService.endTurn()
        playerActionService.endTurn()
        assertDoesNotThrow {  playerActionService.cultivate(flowerTile1, 0, -4)}  // Allowed by Seishi

    }


}
