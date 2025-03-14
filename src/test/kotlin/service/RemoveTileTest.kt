package service

import entity.*
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemoveTileTest {

    @Test
    fun `test remove tile when you can place wood`() {
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)

        //test with no game
        assertThrows<IllegalStateException> {playerActionService.removeTile(BonsaiTile(TileType.WOOD))  }
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, listOf())


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
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, listOf())

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


        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, listOf())

        //check game not null
        val game = mc.currentGame
        checkNotNull(game)

        val firstWoodTile= game.currentState.players[0].bonsai.grid[0, 0]

        //init 2 leaf tiles that go arround a wood tile
        val leafTile1=BonsaiTile(TileType.LEAF)
        val leafTile2=BonsaiTile(TileType.LEAF)

        //removing a wood tile when wood cant be played
        //here the other neighbor are the unplayabe pot tile
        game.currentState.players[0].bonsai.grid[1, -1] = leafTile2
        game.currentState.players[0].bonsai.grid[0, -1] = leafTile1
        var exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(firstWoodTile)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)

        //removing a non-neighbor tile
        val leafTile3=BonsaiTile(TileType.LEAF)
        game.currentState.players[0].bonsai.grid.set(1,-2,leafTile3)
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile3)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)


        //remove a surrounded leaf
        val leafTile4=BonsaiTile(TileType.LEAF)
        val leafTile5=BonsaiTile(TileType.LEAF)
        game.currentState.players[0].bonsai.grid.set(-1,-1,leafTile5)
        game.currentState.players[0].bonsai.grid.set(0,-2,leafTile4)
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile1)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)

        val fruitTile1  =BonsaiTile(TileType.FRUIT)
        game.currentState.players[0].bonsai.grid.set(1,-3,fruitTile1)

        //removing a necessary leaf tile for a fruit
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile4)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)

        val flowerTile1  =BonsaiTile(TileType.FRUIT)
        game.currentState.players[0].bonsai.grid.set(0,-3,flowerTile1)

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
        assertThrows<NoSuchElementException> {  game.currentState.players[0].bonsai.grid.get(2,-2)}
        assertTrue { testRefreshable.refreshAfterStartTileRemoved }

    }


}


