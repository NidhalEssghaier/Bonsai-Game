package serializer

import entity.BonsaiGame
import entity.GameState
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import service.PlayerActionService

/**
 * Serializer for [BonsaiGame]
 * @property descriptor The descriptor of the serializer
 * @property stackSerializer The serializer for the undo and redo stack
 * @constructor Creates a new [BonsaiGameSerializer]
 */
class BonsaiGameSerializer: KSerializer<BonsaiGame> {
    private val stackSerializer = ArrayDequeGameStateSerializer()

    override val descriptor = buildClassSerialDescriptor("BonsaiGame") {
        element("undoStack", stackSerializer.descriptor)
        element("redoStack", stackSerializer.descriptor)
    }

    /**
     * Serializes the [BonsaiGame]
     * @param encoder The encoder to use
     * @param value The [BonsaiGame] object to serialize
     */
    override fun serialize(encoder: Encoder, value: BonsaiGame) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, stackSerializer, value.undoStack)
            encodeSerializableElement(descriptor, 1, stackSerializer, value.redoStack)
        }
    }

    /**
     * Deserializes the [BonsaiGame]
     * @param decoder The decoder to use
     * @return The deserialized [BonsaiGame]
     */
    override fun deserialize(decoder: Decoder): BonsaiGame {
        return decoder.decodeStructure(descriptor) {
            var undoStack = ArrayDeque<GameState>()
            var redoStack = ArrayDeque<GameState>()
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> undoStack = decodeSerializableElement(descriptor, index, stackSerializer)
                    1 -> redoStack = decodeSerializableElement(descriptor, index, stackSerializer)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            val game = BonsaiGame(undoStack, redoStack)
            PlayerActionService.switchPlayer(game)
            game
        }
    }
}