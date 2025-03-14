package serializer

import entity.BonsaiTile
import entity.HexGrid
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

class HexGridSerializer: KSerializer<HexGrid> {
    override val descriptor = buildClassSerialDescriptor("HexGrid") {
        element<Int>("Size")
        element<Map<BonsaiTile, Pair<Int, Int>>>("Map")
    }

    override fun serialize(encoder: Encoder, value: HexGrid) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.size)
            encodeSerializableElement(
                descriptor,
                1,
                MapSerializer(BonsaiTile.serializer(), PairSerializer(Int.serializer(), Int.serializer())),
                value.getInternalMap()
            )
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): HexGrid {
        return decoder.decodeStructure(descriptor) {
            var size = 0
            var map = mapOf<BonsaiTile, Pair<Int, Int>>()
            if (decodeSequentially()) {
                size = decodeIntElement(descriptor, 0)
                map = decodeSerializableElement(
                    descriptor,
                    1,
                    MapSerializer(BonsaiTile.serializer(), PairSerializer(Int.serializer(), Int.serializer())),
                )
            } else {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> size = decodeIntElement(descriptor, index)
                        1 -> map = decodeSerializableElement(
                            descriptor,
                            index,
                            MapSerializer(BonsaiTile.serializer(), PairSerializer(Int.serializer(), Int.serializer())),
                        )
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }
            HexGrid(size, map.toMutableMap())
        }
    }
}