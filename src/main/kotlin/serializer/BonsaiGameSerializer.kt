package serializer

import entity.BonsaiGame
import entity.GameState
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import service.PlayerActionService

class BonsaiGameSerializer: KSerializer<BonsaiGame> {
    private val stackSerializer = ArrayDequeGameStateSerializer()

    override val descriptor = buildClassSerialDescriptor("BonsaiGame") {
        element("undoStack", stackSerializer.descriptor)
        element("redoStack", stackSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: BonsaiGame) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, stackSerializer, value.undoStack)
            encodeSerializableElement(descriptor, 1, stackSerializer, value.redoStack)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
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