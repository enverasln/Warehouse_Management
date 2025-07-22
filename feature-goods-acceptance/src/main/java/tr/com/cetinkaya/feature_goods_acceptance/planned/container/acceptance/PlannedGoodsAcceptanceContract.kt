package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance

import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.GetNextOrderDocumentSeriesAndNumberUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.GetProductByBarcodeUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.GetStockTransactionsByDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel

class PlannedGoodsAcceptanceContract {

    sealed class Event : UiEvent {
        data class OnFetchNextDocument(val orderType: Byte, val orderKind: Byte, val documentSeries: String) : Event()
        data class OnFetchProduct(val barcode: String, val selectedDocuments: List<DocumentUiModel>, val warehouseNumber: Int) : Event()
        data class OnSaveQuantityWithCheck(
            val barcode: String,
            val deliveredQuantity: Double,
            val selectedDocuments: List<DocumentUiModel>,
            val stockTransactionDocument: StockTransactionDocumentUiModel,
            val loggedUser: UserUiModel
        ) : Event()

        data class OnAddOrder(
            val barcode: String,
            val deliveredQuantity: Double
        ) : Event()

        data class OnChangeSingleQuantityChecked(val isChecked: Boolean) : Event()
        data class OnFetchStockTransaction(val stockTransactionDocument: StockTransactionDocumentUiModel?) : Event()
        data class OnUseConfirmedOverQuantity(
            val selectedDocuments: List<DocumentUiModel>,
            val loggedUser: UserUiModel,
            val stockTransactionDocument: StockTransactionDocumentUiModel
        ) : Event()

        data object OnUpdateOrderSyncStatus: Event()

        data class OnDeliveredQuantityChanged(val deliveredQuantity: Double) : Event()
//        data class OnUserRejectedOverQuantity() : Event()
    }

    data class State(
        val fetchedProduct: GetProductByBarcodeUiModel? = null,
        val nextDocumentSeriesAndNumber: GetNextOrderDocumentSeriesAndNumberUiModel? = null,
        val deliveredQuantity: Double = 1.0,
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