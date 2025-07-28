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
        data class OnDocumentDialogConfirmed(val stockTransactionDocument: StockTransactionDocumentUiModel?) : Event()
        data class OnTransferredQuantityChanged(val quantity: Double) : Event()
        data object OnSaveTransfer : Event()
        data class OnWarehouseSelected(val warehouse: WarehouseUiModel) : Event()
        data class OnUnitSelected(val selectedUnit: String) : Event()
        data object OnFinishWarehouseTransfer: Event()
    }

    data class State(
        val loggedUser: UserUiModel? = null,
        val stockTransactionDocument: StockTransactionDocumentUiModel? = null,
        val transferredProducts: List<StockTransactionUiModel> = emptyList(),
        val warehouses: List<WarehouseUiModel> = emptyList(),
        val selectedWarehouse: WarehouseUiModel? = null,
        val units: List<String> = listOf("Adet", "Paket", "Koli"),
        val selectedUnit: String = "Adet",
        val barcodeDefinition: BarcodeDefinitionUiModel? = null,
        val quantity: Double = 1.0
    ) : UiState

    sealed class Effect : UiEffect {
        data object ShowLoading : Effect()
        data object DismissLoading : Effect()
        data class ShowError(val message: String) : Effect()
        data class ShowSuccess(val message: String) : Effect()
        data class ShowDocumentDialog(val documentSeries: String, val documentNumber: Int) : Effect()
        data object DismissDialog : Effect()
        data object RequestFocusOnBarcode : Effect()
        data object RequestFocusOnQuantity: Effect()
        data object NavigateToMainMenu : Effect()
    }
}