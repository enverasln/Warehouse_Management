package tr.com.cetinkaya.feature_goods_acceptance.planned.container

import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDocumentDomainModel
import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order_transaction.OrderTransactionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel

class PlannedGoodsAcceptanceContainerContract {


    sealed class Event : UiEvent {
        data class Initialize(val loggedUser: UserUiModel?, val selectedDocuments: List<DocumentUiModel>) : Event()
        data object OnStateReset : Event()
        data class OnDocumentDialogConfirmed(val stockTransactionDocument: StockTransactionDocumentUiModel) : Event()
        data object OnFinishAcceptance : Event()
        data class TabChanged(val index: Int) : Event()
        data class OnListItemDoubleTab(val orderTx: OrderTransactionUiModel) : Event()
        data class OnDocumentNumberChanged(val documentSeries: String, val documentNumber: Int) : Event()
    }


    data class State(
        val currentTabIndex: Int = 0,
        val tappedBarcode: String = "",
        val orderTxs: List<OrderTransactionUiModel> = emptyList(),
        val selectedDocuments: List<DocumentUiModel> = emptyList(),
        val stockTransactionDocument: StockTransactionDocumentUiModel? = null,
        val nextOrderTxDoc: OrderTransactionDocumentDomainModel? = null,
        val loggedUser: UserUiModel? = null,
        val companyName: String? = null,
        val companyCode: String? = null,
        val isFinishingAcceptance: Boolean = false
    ) : UiState


    sealed class Effect : UiEffect {
        data class ShowError(val message: String) : Effect()
        data class ShowConfirmationDialog(val message: String) : Effect()
        data class ShowSnackbar(val message: String) : Effect()
        data object DismissDialog : Effect()
        data object ShowDocumentDialog : Effect()
        data object CloseAcceptance : Effect()
        data class SetDialogPaperNumber(val paperNumber: String? = null) : Effect()
        data class SetDialogBlockingError(val message: String?) : Effect()
    }
}
