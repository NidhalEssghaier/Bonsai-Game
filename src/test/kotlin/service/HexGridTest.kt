package service

import entity.BonsaiTile
import entity.GoalCard
import entity.PotColor
import entity.TileType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HexGridTest {
    @Test
    fun neighborTest() {
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)
        gameService.startNewGame(listOf(Triple("Anas",0, PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, listOf<GoalCard>())
        val game = mc.currentGame
        checkNotNull(game)
        val woodTile= BonsaiTile(TileType.WOOD)
        game.currentState.players[0].bonsai.grid.set(1,-2,woodTile)

        //should have 0 neighbors
        assertEquals( game.currentState.players[0].bonsai.grid.getNeighbors(woodTile).size,0)

        //init 6 leaf tiles that go arround a wood tile
        val leafTile1=BonsaiTile(TileType.LEAF)
        val leafTile2=BonsaiTile(TileType.LEAF)
        val leafTile3=BonsaiTile(TileType.LEAF)
        val leafTile4=BonsaiTile(TileType.LEAF)
        val leafTile5=BonsaiTile(TileType.LEAF)
        val leafTile6=BonsaiTile(TileType.LEAF)


        game.currentState.players[0].bonsai.grid.set(2,-2,leafTile1)
        game.currentState.players[0].bonsai.grid.set(1,-1,leafTile2)
        game.currentState.players[0].bonsai.grid.set(0,-1,leafTile3)
        game.currentState.players[0].bonsai.grid.set(0,-2,leafTile4)
        game.currentState.players[0].bonsai.grid.set(1,-3,leafTile5)
        game.currentState.players[0].bonsai.grid.set(2,-3,leafTile6)
        //should have 6 neighbors
        assertEquals( game.currentState.players[0].bonsai.grid.getNeighbors(woodTile).size,6)

        //should have 3 neighbors
        assertEquals( game.currentState.players[0].bonsai.grid.getNeighbors(leafTile1).size,3)

        assertTrue(true)
    }
}