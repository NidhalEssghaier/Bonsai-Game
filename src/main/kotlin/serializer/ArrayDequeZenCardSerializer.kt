package serializer

import entity.ZenCard
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ArrayDequeZenCardSerializer: KSerializer<ArrayDeque<ZenCard>> {
    private val listSerializer = ListSerializer(PolymorphicSerializer(ZenCard::class))

    override val descriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ArrayDeque<ZenCard>) {
        encoder.encodeSerializableValue(listSerializer, value.toList())
    }

    override fun deserialize(decoder: Decoder): ArrayDeque<ZenCard> {
        return ArrayDeque(decoder.decodeSerializableValue(listSerializer))
    }
}