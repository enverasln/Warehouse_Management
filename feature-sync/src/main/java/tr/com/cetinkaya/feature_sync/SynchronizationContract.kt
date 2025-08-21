package tr.com.cetinkaya.feature_sync

import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_sync.models.TransferredDocumentUiModel

class SynchronizationContract {
    sealed class Event : UiEvent {
        data object OnStartSynchronization : Event()
        data object OnFetchTransferredDocuments : Event()
    }

    data class State(
        val messages: List<String> = emptyList(),
        val documents: List<TransferredDocumentUiModel> = emptyList()
    ) : UiState

    sealed class Effect : UiEffect {

    }
}