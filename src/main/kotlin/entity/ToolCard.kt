package entity

import kotlinx.serialization.Serializable

/**
 * Represents a "Tool" card in the game, which provides a specific effect.
 *
 * @property id The unique identifier for this tool card.
 */
@Serializable
data class ToolCard(
    val id: Int
) : ZenCard
