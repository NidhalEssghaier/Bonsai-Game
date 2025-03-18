package service

import entity.*
import helper.TileUtils
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoveTileTest {

    private fun setUpGrid():HexGrid {
        val mc = RootService()
        val gameService = GameService(mc)
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, listOf())

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)
        return game.currentState.players[0].bonsai.grid
    }


    @Test
    fun `test remove tile when you can place wood`() {
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)

        //test with no game
        assertThrows<IllegalStateException> {playerActionService.removeTile(BonsaiTile(TileType.WOOD))  }
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, mutableListOf())


        //check game not null
        val game = mc.currentGame
        checkNotNull(game)
        val firstWoodTile= game.currentState.players[0].bonsai.grid[0, 0]

        //check removing with empty bonsai
        val exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(firstWoodTile)
        }
        assertEquals("player can play wood",exception.message)
    }

    @Test
    fun `test remove tile not in bonsai`(){
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, mutableListOf())

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)
        //remove tile not in bonsai
        val leafTile1=BonsaiTile(TileType.LEAF)
        val exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile1)
        }
        assertEquals("cant remove a tile not in players bonsai",
            exception.message)
    }

    @Test
    fun `test remove unnecessary tile`(){

        val mc = RootService()
        val testRefreshable =TestRefreshable()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)
        playerActionService.addRefreshable(testRefreshable)


        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, mutableListOf())

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)

        val firstWoodTile= game.currentState.players[0].bonsai.grid[0, 0]

        //init 2 leaf tiles that go around a wood tile
        val leafTile1=BonsaiTile(TileType.LEAF)
        val leafTile2=BonsaiTile(TileType.LEAF)

        //removing a wood tile when wood cant be played
        //here the other neighbor are the unplayable pot tile
        game.currentState.players[0].bonsai.grid[1, -1] = leafTile2
        game.currentState.players[0].bonsai.grid[0, -1] = leafTile1
        var exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(firstWoodTile)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)

        //removing a non-neighbor tile
        val leafTile3=BonsaiTile(TileType.LEAF)
        game.currentState.players[0].bonsai.grid[1, -2] = leafTile3
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile3)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)


        //remove a surrounded leaf
        val leafTile4=BonsaiTile(TileType.LEAF)
        val leafTile5=BonsaiTile(TileType.LEAF)
        game.currentState.players[0].bonsai.grid[-1, -1] = leafTile5
        game.currentState.players[0].bonsai.grid[0, -2] = leafTile4
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile1)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)

        val fruitTile1  =BonsaiTile(TileType.FRUIT)
        game.currentState.players[0].bonsai.grid[1, -3] = fruitTile1

        //removing a necessary leaf tile for a fruit
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile4)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)

        val flowerTile1  =BonsaiTile(TileType.FRUIT)
        game.currentState.players[0].bonsai.grid[0, -3] = flowerTile1

        //removing a necessary leaf tile for a flower
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile4)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)

        assertFalse { testRefreshable.refreshAfterStartTileRemoved }

        //removing a valid leaf
        assertDoesNotThrow {  playerActionService.removeTile(leafTile2)}

        //see if leaf removed from player grid
        assertThrows<NoSuchElementException> { game.currentState.players[0].bonsai.grid[2, -2] }
    }

    @Test
    fun `test leastGroupOfTilesToBeRemoved with valid flower tile`() {
        val grid =setUpGrid()
        val flowerTile = BonsaiTile(TileType.FLOWER)
        val leafTile1=BonsaiTile(TileType.LEAF)
        grid[1, -1] = leafTile1
        grid[0, -1] = flowerTile
        val result = TileUtils.leastGroupOfTilesToBeRemoved(grid.tilesList.invoke(), grid)
        assertTrue(result.contains(flowerTile), "Flower tiles should be removable")
        assertTrue(!result.contains(leafTile1), "Flower tiles should be removable")
    }

    @Test
    fun `test leastGroupOfTilesToBeRemoved `() {
        val grid =setUpGrid()
        val fruitTile = BonsaiTile(TileType.FRUIT)
        val leafTile1=BonsaiTile(TileType.LEAF)
        val leafTile2=BonsaiTile(TileType.LEAF)
        val leafTile3=BonsaiTile(TileType.LEAF)
        grid[1, -1] = leafTile1
        grid[1, -2] = leafTile2
        grid[0, -2] = leafTile3
        grid[0, -1] = fruitTile
        var result = TileUtils.leastGroupOfTilesToBeRemoved(grid.tilesList.invoke(), grid)
        assertTrue(result.contains(leafTile1), "leaf tile should be removable")
        assertFalse(result.contains(leafTile2), "middle leaf tile should not be removable")
        val unplayableTile=BonsaiTile(TileType.UNPLAYABLE)
        grid[0, -1] = unplayableTile
        result = TileUtils.leastGroupOfTilesToBeRemoved(grid.tilesList.invoke(), grid)
        assertFalse(result.contains(unplayableTile))
    }
    @Test
    fun `test hasAdjacentPair`() {
        var grid =setUpGrid()
        val fruitTile = BonsaiTile(TileType.FRUIT)
        val leafTile1=BonsaiTile(TileType.LEAF)
        val leafTile2=BonsaiTile(TileType.LEAF)
        grid[1, -1] = leafTile1
        grid[1, -2] = leafTile2
        grid[0, -1] = fruitTile
        val fruitLeafNeighbors = grid.getNeighbors(fruitTile)
            .filter { it.type == TileType.LEAF }
        var result = TileUtils.hasAdjacentPair(fruitLeafNeighbors, grid)
        assertTrue(result, "there is 2 adjacent leaves")
        grid=setUpGrid()
        grid[0, -1] = leafTile1
        grid[2, -2] = leafTile2
        grid[1, -1] = fruitTile
        result = TileUtils.hasAdjacentPair(fruitLeafNeighbors, grid)
        assertFalse(result, "there is no 2 adjacent leaves")
    }

}


