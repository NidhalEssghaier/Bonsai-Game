package serializer

import entity.GameState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ArrayDequeGameStateSerializer: KSerializer<ArrayDeque<GameState>> {
    private val listSerializer = ListSerializer(GameState.serializer())

    override val descriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: ArrayDeque<GameState>) {
        encoder.encodeSerializableValue(listSerializer, value.toList())
    }

    override fun deserialize(decoder: Decoder): ArrayDeque<GameState> {
        return ArrayDeque(decoder.decodeSerializableValue(listSerializer))
    }
}