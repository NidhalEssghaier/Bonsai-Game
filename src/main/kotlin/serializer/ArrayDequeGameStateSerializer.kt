package serializer

import entity.GameState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for ArrayDeque of GameState
 * @property descriptor The descriptor of the serializer
 */
class ArrayDequeGameStateSerializer: KSerializer<ArrayDeque<GameState>> {
    private val listSerializer = ListSerializer(GameState.serializer())

    override val descriptor = listSerializer.descriptor

    /**
     * Serializes the given value
     * @param encoder The encoder to use
     * @param value The value to serialize
     */
    override fun serialize(encoder: Encoder, value: ArrayDeque<GameState>) {
        encoder.encodeSerializableValue(listSerializer, value.toList())
    }

    /**
     * Deserializes the given value
     * @param decoder The decoder to use
     * @return The deserialized ArrayDeque of GameState
     */
    override fun deserialize(decoder: Decoder): ArrayDeque<GameState> {
        return ArrayDeque(decoder.decodeSerializableValue(listSerializer))
    }
}