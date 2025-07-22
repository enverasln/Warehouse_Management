package tr.com.cetinkaya.feature_goods_acceptance.planned.search_document

import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel

data class State(
    val documentsState: DocumentsState,
    val loggedUser: UserUiModel?
) : UiState


sealed class DocumentsState {
    data object Idle : DocumentsState()
    data object Loading : DocumentsState()
    data class Success(val documents: List<DocumentUiModel>) : DocumentsState()
}


sealed class Event : UiEvent {
    data class OnSearchButtonClick(val documentDate: String, val companyName: String) : Event()
    data class OnDocumentSelected(val selectedDocument: DocumentUiModel?) : Event()
    data object SelectAllDocuments : Event()
    data object DeselectAllDocuments : Event()
    data object OnBeginGoodsAcceptance : Event()

}


sealed class Effect : UiEffect {
    data class ShowError(val message: String) : Effect()
    data class ShowValidationError(val message: String) : Effect()
    data class NavigateGoodsAcceptance(val documents: Set<DocumentUiModel>) : Effect()
}