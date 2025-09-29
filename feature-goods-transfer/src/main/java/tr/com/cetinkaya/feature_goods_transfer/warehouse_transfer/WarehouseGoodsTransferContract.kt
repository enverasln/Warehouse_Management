package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_transfer.models.UserUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.BarcodeDefinitionUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.WarehouseUiModel

class WarehouseGoodsTransferContract {
    sealed class Event : UiEvent {
        data class OnInitialize(val loggedUser: UserUiModel?) : Event()
        data class OnBarcodeEntered(val barcode: String) : Event()
        data class OnConfirmDocumentDialog(val stockTransactionDocument: StockTransactionDocumentUiModel?) : Event()
        data class OnTransferredQuantityChanged(val quantity: Double) : Event()
        data object OnSaveTransfer : Event()
        data class OnWarehouseSelected(val warehouse: WarehouseUiModel) : Event()
        data class OnQuantityChanged(val quantity: Double) : Event()
        data class OnUnitSelected(val selectedUnit: String) : Event()
        data object OnClickFinish : Event()
        data class OnDocumentNumberChanged(val stockTxDoc: StockTransactionDocumentUiModel?) : Event()
        data class OnClickGetAssortmentBarcodeIcon(val stockCode: String) : Event()
        data object OnClickExit : Event()
        data class OnLongTapStockTx(val stockTx: StockTransactionUiModel) : Event()
    }

    data class State(
        val loggedUser: UserUiModel? = null,
        val stockTxDoc: StockTransactionDocumentUiModel? = null,
        val stockTransactions: List<StockTransactionUiModel> = emptyList(),
        val warehouses: List<WarehouseUiModel> = emptyList(),
        val selectedWarehouse: WarehouseUiModel? = null,
        val units: List<String> = listOf("Adet", "Paket", "Koli"),
        val selectedUnit: String = "Adet",
        val barcodeDefinition: BarcodeDefinitionUiModel? = null,
        val quantity: Double = 1.0,
        val selectedStockTransaction: StockTransactionUiModel? = null
    ) : UiState

    sealed class Effect : UiEffect {
        data object ShowLoading : Effect()
        data object DismissLoading : Effect()
        data class ShowDocumentDialog(val documentSeries: String, val documentNumber: Int) : Effect()
        data object DismissDialog : Effect()
        data object RequestFocusOnBarcode : Effect()
        data object RequestFocusOnQuantity : Effect()
        data object NavigateToMainMenu : Effect()
        data class SetDialogBlockingError(val message: String?) : Effect()
    }
}