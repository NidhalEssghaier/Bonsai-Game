package service

import entity.BonsaiTile
import entity.GoalCard
import entity.PotColor
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
        gameService.startNewGame(listOf(Triple("Anas",0,PotColor.PURPLE), Triple("Iyed",1,PotColor.PURPLE)),5, listOf<GoalCard>())
        val game = mc.currentGame
        checkNotNull(game)
        val woodTile=BonsaiTile(TileType.WOOD)
        game.currentState.players[0].bonsai.grid.set(1,-2,woodTile)
        var exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(woodTile)
        }
        assertEquals("player can play wood", exception.message)

        //init 6 leaf tiles that go arround a wood tile
        val leafTile1=BonsaiTile(TileType.LEAF)
        val leafTile2=BonsaiTile(TileType.LEAF)
        val leafTile3=BonsaiTile(TileType.LEAF)
        val leafTile4=BonsaiTile(TileType.LEAF)
        val leafTile5=BonsaiTile(TileType.LEAF)
        val leafTile6=BonsaiTile(TileType.LEAF)

        //remove tile not in bonsai
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(leafTile1)
        }
        assertEquals("cant remove a tile not in players bonsai",
            exception.message)

        game.currentState.players[0].bonsai.grid.set(2,-2,leafTile1)
        game.currentState.players[0].bonsai.grid.set(1,-1,leafTile2)
        game.currentState.players[0].bonsai.grid.set(0,-1,leafTile3)
        game.currentState.players[0].bonsai.grid.set(0,-2,leafTile4)
        game.currentState.players[0].bonsai.grid.set(1,-3,leafTile5)
        game.currentState.players[0].bonsai.grid.set(2,-3,leafTile6)

        //removing a wood tile
        exception = assertThrows<IllegalStateException> {
            playerActionService.removeTile(woodTile)
        }
        assertEquals("tile not part of the least number of tiles to be removed to make placing a wood possible",
            exception.message)
        playerActionService.removeTile(leafTile1)
        assertDoesNotThrow {playerActionService.removeTile(leafTile1) }


    }
}