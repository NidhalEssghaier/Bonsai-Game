package entity

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity to represent the card type "Tool".
 *
 * @property number The number of tiles the player can additionally place in the bonsai
 */
@Serializable
data class ToolCard @OptIn(ExperimentalUuidApi::class) constructor(
    private val uuid: Uuid = Uuid.random()
) : ZenCard
