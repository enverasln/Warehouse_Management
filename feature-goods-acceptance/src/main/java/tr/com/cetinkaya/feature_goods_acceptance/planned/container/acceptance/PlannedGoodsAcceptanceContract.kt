package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance

import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDocumentDomainModel
import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.models.AddOrderTransactionParams
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order_transaction.OrderTransactionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.GetStockTransactionsByDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel

class PlannedGoodsAcceptanceContract {

    sealed class Event : UiEvent {
        data class OnFetchNextDocument(val orderType: OrderTransactionTypes, val orderKind: OrderTransactionKinds, val documentSeries: String) :
            Event()

        data class OnFetchOrderTx(val barcode: String, val orderTxs: List<OrderTransactionUiModel>) : Event()
        data class OnSaveWithCheckQuantity(
            val selectedOrderTxs: List<OrderTransactionUiModel>, val stockTxDocument: StockTransactionDocumentUiModel, val loggedUser: UserUiModel
        ) : Event()

        data class OnChangeSingleQuantityChecked(val isChecked: Boolean) : Event()
        data class OnFetchStockTransaction(val stockTransactionDocument: StockTransactionDocumentUiModel?) : Event()
        data class OnUseConfirmedOverQuantity(
            val addOrderTxParams: AddOrderTransactionParams, val orderTxs: List<OrderTransactionUiModel>, val loggedUser: UserUiModel
        ) : Event()

        data class OnDeliveredQuantityChanged(val deliveredQuantity: Double) : Event()
    }

    data class State(
        val addOrderTxParams: AddOrderTransactionParams? = null,
        val nextDocumentSeriesAndNumber: OrderTransactionDocumentDomainModel? = null,
        val isSingleQuantity: Boolean = false,
        val stockTransactions: List<GetStockTransactionsByDocumentUiModel> = emptyList()
    ) : UiState


    sealed class Effect : UiEffect {
        data object ShowOverQuantityDialog : Effect()
        data class ShowWarning(val message: String) : Effect()
        data class ShowSuccess(val message: String) : Effect()
        data class ShowError(val message: String) : Effect()

        data class ShowBarcodeNotFound(val barcode: String) : Effect()
    }


}