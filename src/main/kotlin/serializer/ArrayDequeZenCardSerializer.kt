package serializer

import entity.ZenCard
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
/**
 * Serializer for ArrayDeque of ZenCard
 * @property descriptor The descriptor of the serializer
 */
class ArrayDequeZenCardSerializer: KSerializer<ArrayDeque<ZenCard>> {
    private val listSerializer = ListSerializer(PolymorphicSerializer(ZenCard::class))

    override val descriptor = listSerializer.descriptor

    /**
     * Serializes the given value
     * @param encoder The encoder to use
     * @param value The value to serialize
     */
    override fun serialize(encoder: Encoder, value: ArrayDeque<ZenCard>) {
        encoder.encodeSerializableValue(listSerializer, value.toList())
    }

    /**
     * Deserializes the given value
     * @param decoder The decoder to use
     * @return The deserialized ArrayDeque of ZenCard
     */
    override fun deserialize(decoder: Decoder): ArrayDeque<ZenCard> {
        return ArrayDeque(decoder.decodeSerializableValue(listSerializer))
    }
}