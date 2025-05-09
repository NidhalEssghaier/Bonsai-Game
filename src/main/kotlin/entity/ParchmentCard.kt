package entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entity to represent the card type "Parchment"
 *
 * @property type The type of tile or card the player gets a bonus for
 */
@Serializable
data class ParchmentCard(
    val points: Int,
    @SerialName("CardType")
    val type: ParchmentCardType,
    val id: Int
) : ZenCard
