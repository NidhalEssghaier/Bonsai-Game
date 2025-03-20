package service.bot

import entity.*
import entity.bot.*
import service.*

class BotMain {

    fun startBotGame() {
        val rootService = RootService()
        val gameService = GameService(rootService)
        val botService = BotService(rootService)




        val player0 = Triple("Alice",0, PotColor.RED)
        val player1 = Triple("Bob",0, PotColor.PURPLE)
        val players = mutableListOf(player0,player1)

        val goalColors = listOf(GoalColor.BROWN, GoalColor.GREEN, GoalColor.ORANGE)

        gameService.startNewGame(players,3,goalColors)

        val game = rootService.currentGame
        checkNotNull(game)
        var node = Node(playerNumber = game.currentState.currentPlayer, gameState = game)
        var round = 0
        while (true) {
            /*println("Runde: $round")
            val newNode = node.children[botService.calculateNextMove(node)]
            checkNotNull(newNode)
            node = newNode
            round += 1

            println(node.printVisit(mutableListOf()))*/
            botService.playRandomMove()
        }
    }
}