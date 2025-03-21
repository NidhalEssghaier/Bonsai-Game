package entity.bot

import entity.BonsaiGame

class Node(
    var move: PlayerAction? = null,
    var children: MutableList<Node?> = mutableListOf(),
    var playerNumber: Int,
    var winRatio: Pair<Int,Int> = Pair(0,0),
    val gameState: BonsaiGame
) {
    fun printVisit(list: MutableList<Pair<PlayerAction?,Pair<Int,Int>>>) : MutableList<Pair<PlayerAction?,Pair<Int,Int>>> {
        list.add(Pair(move,winRatio))
        for(child in children) {
            child?.printVisit(list)
        }

        return list
    }

    fun isLeaf() : Boolean {
        return children.isEmpty()
    }

    /*fun getState() {
        val player0Points = gameState.players[0].points
        val player1Points = gameState.players[1].points
    }*/
}