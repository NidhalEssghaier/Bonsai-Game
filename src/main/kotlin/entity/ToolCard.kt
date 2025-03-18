package entity

import kotlinx.serialization.Serializable

/**
 * Entity to represent the card type "Tool".
 *
 * @property number The number of tiles the player can additionally place in the bonsai
 */
@Serializable
data class ToolCard(
    val id: Int
) : ZenCard
