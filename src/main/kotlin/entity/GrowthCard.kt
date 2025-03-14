package entity

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity to represent the card type "Growth"
 *
 * @property type The player can place more tiles of this type in the bonsai.
 */
@Serializable
data class GrowthCard @OptIn(ExperimentalUuidApi::class) constructor(
    val type: TileType,
    private val uuid: Uuid = Uuid.random()
) : ZenCard