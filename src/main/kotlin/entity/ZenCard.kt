package entity

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

/**
 * Interface to represent a zen card
 */
@Serializable
@Polymorphic
sealed interface ZenCard
