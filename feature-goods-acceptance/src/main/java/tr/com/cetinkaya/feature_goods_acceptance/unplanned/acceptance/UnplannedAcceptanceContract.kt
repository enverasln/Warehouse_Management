package tr.com.cetinkaya.feature_goods_acceptance.unplanned.acceptance

import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_acceptance.models.barcode_definition.BarcodeDefinitionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.models.stock_transaction.StockTransactionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.models.user.UserUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.unplanned.model.current_account.CurrentAccountUiModel

class UnplannedAcceptanceContract {

    sealed class Event : UiEvent {
        data class OnInitialize(val loggedUser: UserUiModel?, val currentAccount: CurrentAccountUiModel?) : Event()
        data class OnConfirmDocumentDialog(val stockTxDoc: StockTransactionDocumentUiModel?, val currentAccount: CurrentAccountUiModel) : Event()
        data object OnCancelDocumentDialog : Event()
        data class OnBarcodeEntered(val barcode: String, val warehouseNumber: Int) : Event()
        data class OnSaveAcceptance(val stockTxDoc: StockTransactionDocumentUiModel?, val currentAccCode: String, val barcodeDef: BarcodeDefinitionUiModel?, val unit: String, val quantity: Double, val user: UserUiModel? ) : Event()
        data class OnQuantityChanged(val quantity: Double) : Event()
        data class OnClickAssortmentBarcodeIcon(val stockCode: String, val warehouseNumber: Int) : Event()
        data class OnClickExit(val stockDocTx: StockTransactionDocumentUiModel) : Event()
        data class OnClickFinish(val stockDocTx: StockTransactionDocumentUiModel, val currentAccCode: String) : Event()
        data class OnLongTapStockTx(val stockTx: StockTransactionUiModel) : Event()
        data class OnChangeSingleQuantityCheckStatus(val isChecked: Boolean) : Event()
    }

    data class State(
        val stockTxDoc: StockTransactionDocumentUiModel? = null,
        val stockTransactions: List<StockTransactionUiModel> = emptyList(),
        val barcodeDef: BarcodeDefinitionUiModel? = null,
        val quantity: Double = 1.0,
        val singleQuantityStatus : Boolean = false,
        val units: List<String> = listOf("Adet", "Paket", "Koli"),
        val selectedUnit: String = "Adet"
    ) : UiState

    sealed class Effect : UiEffect {
        data object DismissDocumentDialog : Effect()
        data class ShowDocumentDialog(val docSeries: String, val docNumber: Int) : Effect()
        data object RequestFocusOnBarcode : Effect()
        data object RequestFocusOnQuantity: Effect()
        data object ShowLoading : Effect()
        data object DismissLoading : Effect()
        data object NavigateToMainMenu : Effect()
        data object NavigateToSearchCompany : Effect()
        data object TriggerAutoSave : Effect()
    }

}