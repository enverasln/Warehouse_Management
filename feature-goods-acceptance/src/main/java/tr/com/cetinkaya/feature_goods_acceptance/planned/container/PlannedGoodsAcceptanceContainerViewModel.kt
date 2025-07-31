package tr.com.cetinkaya.feature_goods_acceptance.planned.container

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.domain.usecase.order.ObservePlannedGoodsAcceptanceProductsUseCase
import tr.com.cetinkaya.domain.usecase.order.SyncPlannedGoodsAcceptanceProductsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CheckDocumentIsUsableUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.UpdateStockTransactionSyncStatusUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel
import javax.inject.Inject

@HiltViewModel
class PlannedGoodsAcceptanceContainerViewModel @Inject constructor(
    private val syncPlannedGoodsAcceptanceProductsUseCase: SyncPlannedGoodsAcceptanceProductsUseCase,
    private val observePlannedGoodsAcceptanceProductsUseCase: ObservePlannedGoodsAcceptanceProductsUseCase,
    private val checkDocumentIsUsableUseCase: CheckDocumentIsUsableUseCase,
    private val updateStockTransactionSyncStatusUseCase: UpdateStockTransactionSyncStatusUseCase
) : BaseViewModel<PlannedGoodsAcceptanceContainerContract.Event, PlannedGoodsAcceptanceContainerContract.State, PlannedGoodsAcceptanceContainerContract.Effect>() {

    override fun createInitialState(): PlannedGoodsAcceptanceContainerContract.State = PlannedGoodsAcceptanceContainerContract.State()

    override fun handleEvent(event: PlannedGoodsAcceptanceContainerContract.Event) {
        when (event) {
            is PlannedGoodsAcceptanceContainerContract.Event.Initialize -> onInitialize(event.loggedUser, event.selectedDocuments)

            is PlannedGoodsAcceptanceContainerContract.Event.OnStateReset -> setState { createInitialState() }

            is PlannedGoodsAcceptanceContainerContract.Event.OnDocumentDialogConfirmed -> {
                onDocumentDialogConfirmed(event.stockTransactionDocument)
            }

            is PlannedGoodsAcceptanceContainerContract.Event.OnFinishAcceptance -> {
                val documentSeries = currentState.stockTransactionDocument?.documentSeries ?: return
                val documentNumber = currentState.stockTransactionDocument?.documentNumber ?: return
                val request = UpdateStockTransactionSyncStatusUseCase.Request(documentSeries, documentNumber, "Aktarılacak")
                viewModelScope.launch {
                    updateStockTransactionSyncStatusUseCase(request).onStart {

                    }.collect {

                    }
                }
            }

            is PlannedGoodsAcceptanceContainerContract.Event.CheckDocumentStatus -> {
                val (documentSeries: String, documentNumber: Int, companyCode: String, paperNumber: String) = event
                checkDocumentStatus(documentSeries, documentNumber, companyCode, paperNumber)
            }

            is PlannedGoodsAcceptanceContainerContract.Event.FetchProducts -> fetchProducts()

            is PlannedGoodsAcceptanceContainerContract.Event.OnProductDoubleTab -> {
                setState { copy(currentTabIndex = 0, tappedBarcode = event.product.barcode) }
            }

            is PlannedGoodsAcceptanceContainerContract.Event.TabChanged -> setState { copy(currentTabIndex = event.index) }

        }
    }


    private fun onInitialize(loggedUser: UserUiModel?, selectedDocuments: List<DocumentUiModel>) {
        val companyName = selectedDocuments.firstOrNull()?.companyName ?: return
        setState { copy(loggedUser = loggedUser, selectedDocuments = selectedDocuments, companyName = companyName) }
        setEffect { PlannedGoodsAcceptanceContainerContract.Effect.ShowDocumentDialog }
    }

    private fun onDocumentDialogConfirmed(stockTransactionDocument: StockTransactionDocumentUiModel) {
        setStockTransactionDocument(stockTransactionDocument)
    }

    private fun setStockTransactionDocument(stockTransactionDocument: StockTransactionDocumentUiModel) {
        setState { copy(stockTransactionDocument = stockTransactionDocument) }
        val companyCode = currentState.selectedDocuments.firstOrNull()?.companyCode ?: return
        setEvent(
            PlannedGoodsAcceptanceContainerContract.Event.CheckDocumentStatus(
                stockTransactionDocument.documentSeries, stockTransactionDocument.documentNumber, companyCode, stockTransactionDocument.paperNumber
            )
        )
    }

    private fun checkDocumentStatus(documentSeries: String, documentNumber: Int, companyCode: String, paperNumber: String) {
        viewModelScope.launch {
            checkDocumentIsUsableUseCase(
                CheckDocumentIsUsableUseCase.Request(
                    documentSeries,
                    documentNumber,
                    companyCode,
                    paperNumber,
                    StockTransactionTypes.Input,
                    StockTransactionKinds.Wholesale,
                    StockTransactionDocumentTypes.EntryDispatchNote,
                    0
                )
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        if (result.data.documentStatus.isDocumentNew) {
                            setEffect { PlannedGoodsAcceptanceContainerContract.Effect.DismissDialog }
                            setEvent(PlannedGoodsAcceptanceContainerContract.Event.FetchProducts)
                        } else {
                            setEffect { PlannedGoodsAcceptanceContainerContract.Effect.ShowConfirmationDialog(result.data.documentStatus.message) }
                        }
                    }

                    is Result.Loading -> {}

                    is Result.Error -> setEffect { PlannedGoodsAcceptanceContainerContract.Effect.ShowError(result.message) }
                }
            }


        }
    }

    private fun observeProducts(selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int) {
        viewModelScope.launch {
            observePlannedGoodsAcceptanceProductsUseCase(
                ObservePlannedGoodsAcceptanceProductsUseCase.Request(selectedDocuments, warehouseNumber)
            ).collect { result ->
                when (result) {
                    is Result.Loading -> Unit

                    is Result.Success -> {
                        setState {
                            val products = result.data.products.map { it.toUiModel() }
                            copy(products = products.toList())
                        }
                    }

                    is Result.Error -> {
                        setEffect { PlannedGoodsAcceptanceContainerContract.Effect.ShowError(result.message) }
                    }
                }
            }
        }
    }

    private fun syncProducts(selectedDocuments: List<Pair<String, Int>>, warehouseNumber: Int) {
        viewModelScope.launch {
            syncPlannedGoodsAcceptanceProductsUseCase(
                SyncPlannedGoodsAcceptanceProductsUseCase.Request(
                    selectedDocuments, warehouseNumber
                )
            ).onStart {
                emit(Result.Loading)
            }.collect { result ->
                when (result) {
                    is Result.Loading -> {}

                    is Result.Success<*> -> {
                        PlannedGoodsAcceptanceContainerContract.Effect.ShowSnackbar("Sipariş başarıyla senkronize edildi")
                    }

                    is Result.Error -> {
                        setEffect { PlannedGoodsAcceptanceContainerContract.Effect.ShowError("Sipariş senkronizasyonu başarısız: ${result.message}") }
                        return@collect
                    }
                }
            }

            observePlannedGoodsAcceptanceProductsUseCase(
                ObservePlannedGoodsAcceptanceProductsUseCase.Request(selectedDocuments, warehouseNumber)
            ).collect { result ->
                when (result) {
                    is Result.Loading -> Unit

                    is Result.Success -> {
                        setState {
                            val products = result.data.products.map { it.toUiModel() }
                            copy(products = products)
                        }
                    }

                    is Result.Error -> {
                        setEffect { PlannedGoodsAcceptanceContainerContract.Effect.ShowError(result.message) }
                    }
                }
            }
        }
    }

    private fun fetchProducts() {
        val mappedSelectedDocuments = currentState.selectedDocuments.map { it.documentSeries to it.documentNumber }
        val warehouseNumber = currentState.loggedUser?.warehouseNumber ?: return

        observeProducts(mappedSelectedDocuments, warehouseNumber)
        syncProducts(mappedSelectedDocuments, warehouseNumber)

    }
}



