package entity

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entity to represent the card type "Master"
 *
 * @property tiles The tiles the player is allowed to take with this card
 */
@Serializable
data class MasterCard @OptIn(ExperimentalUuidApi::class) constructor(
    val tiles: List<TileType>,
    private val uuid: Uuid = Uuid.random()
) : ZenCard
