package entity

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity to represent the card type "Parchment"
 *
 * @property type The type of tile or card the player gets a bonus for
 */
@Serializable
data class ParchmentCard @OptIn(ExperimentalUuidApi::class) constructor(
    val points: Int,
    val type: ParchmentCardType,
    private val uuid: Uuid = Uuid.random()
) : ZenCard
