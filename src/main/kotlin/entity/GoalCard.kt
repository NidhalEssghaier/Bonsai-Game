package entity

import kotlinx.serialization.Serializable

/**
 * Entity to represent a goal card
 *
 * @property points The number of points the player gains, if the card is acquired.
 * @property color Indicates which goals are part of the same group.
 * @property difficulty The difficulty of the goal.
 */
@Serializable
data class GoalCard(
    val points: Int,
    val color: GoalColor,
    val difficulty: GoalDifficulty,
)
