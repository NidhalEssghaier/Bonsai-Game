package service

import entity.BonsaiTile
import entity.GoalCard
import entity.TileType
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tools.aqua.bgw.components.gamecomponentviews.HexagonView
import tools.aqua.bgw.visual.Visual
import kotlin.test.Test
import kotlin.test.assertEquals

class removeTileTest {

    @Test
    fun `test remove tile when you can place wood`() {
        val mc = RootService()
        val gameService = GameService(mc)
        val playerActionService = PlayerActionService(mc)
        gameService.startNewGame(listOf(Pair("Anas",0), Pair("Iyed",1)),5, listOf<GoalCard>())
        val game = mc.currentGame
        checkNotNull(game)
        val woodTile=BonsaiTile(TileType.WOOD)
        val woodTileView = HexagonView(1,1,1,Visual.EMPTY)
        game.currentState.players[0].bonsai.map.add(Pair(woodTileView,woodTile))
        var exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(woodTileView)
        }
        assertEquals("player can play wood", exception.message)

        //init 6 leaf tiles that go arround a wood tile
        val leafTile1=BonsaiTile(TileType.LEAF)
        val leafTile2=BonsaiTile(TileType.LEAF)
        val leafTile3=BonsaiTile(TileType.LEAF)
        val leafTile4=BonsaiTile(TileType.LEAF)
        val leafTile5=BonsaiTile(TileType.LEAF)
        val leafTile6=BonsaiTile(TileType.LEAF)
        val leafTileView1=HexagonView(2,2,1,Visual.EMPTY)
        val leafTileView2=HexagonView(3,3,1,Visual.EMPTY)
        val leafTileView3=HexagonView(4,4,1,Visual.EMPTY)
        val leafTileView4=HexagonView(5,5,1,Visual.EMPTY)
        val leafTileView5=HexagonView(6,6,1,Visual.EMPTY)
        val leafTileView6=HexagonView(7,7,1,Visual.EMPTY)

        woodTile.neighbors.addAll(listOf(leafTile1,leafTile2,leafTile3,leafTile4,leafTile5,leafTile6))
        leafTile1.neighbors.addAll(listOf(leafTile2,leafTile6,woodTile))
        leafTile2.neighbors.addAll(listOf(leafTile3,leafTile1,woodTile))
        leafTile3.neighbors.addAll(listOf(leafTile4,leafTile2,woodTile))
        leafTile4.neighbors.addAll(listOf(leafTile5,leafTile3,woodTile))
        leafTile5.neighbors.addAll(listOf(leafTile6,leafTile4,woodTile))
        leafTile6.neighbors.addAll(listOf(leafTile1,leafTile5,woodTile))


        //removing a wood tile
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(woodTileView)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)

        //remove tile not in bonsai
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTileView1)
        }
        assertEquals("cant remove a tile not in players bonsai",
            exception.message)
        game.currentState.players[0].bonsai.map.addAll(leafTileView1 to leafTile1,leafTileView2 to leafTile2
            ,leafTileView3 to leafTile3,leafTileView4 to leafTile4,leafTileView5 to leafTile5,leafTileView6 to leafTile6)

        assertDoesNotThrow {playerActionService.removeTile(leafTileView1) }


    }
}