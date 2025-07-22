package tr.com.cetinkaya.feature_goods_acceptance.planned.container

import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.ProductUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel

class PlannedGoodsAcceptanceContainerContract {


    sealed class Event : UiEvent {
        data class Initialize(val loggedUser: UserUiModel?, val selectedDocuments: List<DocumentUiModel>) : Event()
        data object OnStateReset : Event()
        data class OnDocumentDialogConfirmed(val stockTransactionDocument: StockTransactionDocumentUiModel) : Event()
        data object OnFinishAcceptance : Event()
        data object FetchProducts : Event()
        data class CheckDocumentStatus(
            val documentSeries: String, val documentNumber: Int, val companyCode: String, val paperNumber: String
        ) : Event()

        data class TabChanged(val index: Int) : Event()
        data class OnProductDoubleTab(val product: ProductUiModel) : Event()
    }


    data class State(
        val currentTabIndex: Int = 0,
        val tappedBarcode: String = "",
        val products: List<ProductUiModel> = emptyList(),
        val selectedDocuments: List<DocumentUiModel> = emptyList(),
        val stockTransactionDocument: StockTransactionDocumentUiModel? = null,
        val loggedUser: UserUiModel? = null,
        val companyName: String? = null,
    ) : UiState


    sealed class Effect : UiEffect {
        data class ShowError(val message: String) : Effect()
        data class ShowConfirmationDialog(val message: String) : Effect()
        data class ShowSnackbar(val message: String) : Effect()
        data object DismissDialog : Effect()
        data object ShowDocumentDialog : Effect()
    }
}
