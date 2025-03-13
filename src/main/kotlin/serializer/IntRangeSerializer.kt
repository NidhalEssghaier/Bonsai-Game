package serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

class IntRangeSerializer: KSerializer<IntRange> {
    override val descriptor = buildClassSerialDescriptor("IntRange") {
        element<Int>("start")
        element<Int>("endInclusive")
        element<Int>("step")
    }

    override fun serialize(encoder: Encoder, value: IntRange) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.start)
            encodeIntElement(descriptor, 1, value.endInclusive)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): IntRange {
        return decoder.decodeStructure(descriptor) {
            var start = 0
            var endInclusive = 0
            if(decodeSequentially()) {
                start = decodeIntElement(descriptor, 0)
                endInclusive = decodeIntElement(descriptor, 1)
            } else {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> start = decodeIntElement(descriptor, index)
                        1 -> endInclusive = decodeIntElement(descriptor, index)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }
            start..endInclusive
        }
    }
}