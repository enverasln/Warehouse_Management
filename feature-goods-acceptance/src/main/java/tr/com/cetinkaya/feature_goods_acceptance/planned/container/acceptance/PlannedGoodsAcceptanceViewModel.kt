package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.domain.usecase.order.AddOrderUseCase
import tr.com.cetinkaya.domain.usecase.order.GetNextOrderDocumentSeriesAndNumberUseCase
import tr.com.cetinkaya.domain.usecase.order.GetProductByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.order.UpdateOrderSyncStatusUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentWithRemainingQuantityUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.toDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.toDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.toDomainModel
import javax.inject.Inject

@HiltViewModel
class PlannedGoodsAcceptanceViewModel @Inject constructor(
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val addStockTransactionUseCase: AddStockTransactionUseCase,
    private val getStockTransactionsByDocumentUseCase: GetStockTransactionsByDocumentWithRemainingQuantityUseCase,
    private val getNextOrderDocumentSeriesAndNumberUseCase: GetNextOrderDocumentSeriesAndNumberUseCase,
    private val addOrderUseCase: AddOrderUseCase,
    private val updateOrderSyncStatusUseCase: UpdateOrderSyncStatusUseCase
) : BaseViewModel<PlannedGoodsAcceptanceContract.Event, PlannedGoodsAcceptanceContract.State, PlannedGoodsAcceptanceContract.Effect>() {
    companion object {
        const val TAG = "PlannedGoodsAcceptanceViewModel"
    }

    override fun createInitialState(): PlannedGoodsAcceptanceContract.State = PlannedGoodsAcceptanceContract.State()

    override fun handleEvent(event: PlannedGoodsAcceptanceContract.Event) {
        when (event) {
            is PlannedGoodsAcceptanceContract.Event.OnFetchNextDocument -> {
                fetchNextOrderDocumentSeriesAndNumber(event.orderType, event.orderKind, event.documentSeries)
            }

            is PlannedGoodsAcceptanceContract.Event.OnFetchProduct -> {
                val mappedSelectedDocuments = event.selectedDocuments.map { it.documentSeries to it.documentNumber }
                val request = GetProductByBarcodeUseCase.Request(
                    barcode = event.barcode, sekectedDocuments = mappedSelectedDocuments, warehouseNumber = event.warehouseNumber
                )

                viewModelScope.launch {
                    getProductByBarcodeUseCase(request).collectLatest { result ->
                        when (result) {
                            is Result.Loading -> {

                            }

                            is Result.Success -> {

                                val product = result.data.product.toUiModel()
                                val deliveredQty = if (currentState.isSingleQuantity) 1.0 else product.remainingQty

                                setState { copy(fetchedProduct = product, deliveredQuantity = deliveredQty) }
                            }

                            is Result.Error -> {
                                setState { copy(fetchedProduct = null) }
                                setEffect { PlannedGoodsAcceptanceContract.Effect.ShowWarning("${event.barcode} numaralı barkoda ait ürün bilgisi bulunamadı.") }
                            }
                        }
                    }
                }
            }

            is PlannedGoodsAcceptanceContract.Event.OnSaveQuantityWithCheck -> {
                val product = currentState.fetchedProduct

                if (product == null) {
                    setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError("Kayıt yapılacak herhangi bir ürün bulunmamaktadır.") }
                    return
                }
                if (event.deliveredQuantity > product.remainingQty) {
                    setEffect { PlannedGoodsAcceptanceContract.Effect.ShowOverQuantityDialog }
                } else {
                    if (event.deliveredQuantity == 0.0) {
                        setEffect { PlannedGoodsAcceptanceContract.Effect.ShowWarning("Miktar boş bırakılamaz.\r\nYa da 0(sıfır) olarak girilemez.") }
                        return
                    }

                    saveStockTransaction(
                        barcode = event.barcode,
                        quantity = event.deliveredQuantity,
                        selectedDocuments = event.selectedDocuments,
                        stockTransactionDocument = event.stockTransactionDocument,
                        loggedUser = event.loggedUser
                    )
                }
            }

            is PlannedGoodsAcceptanceContract.Event.OnAddOrder -> {

            }

            is PlannedGoodsAcceptanceContract.Event.OnChangeSingleQuantityChecked -> {
                val deliveredQuantity = currentState.fetchedProduct?.remainingQty ?: 0.0

                setState {
                    copy(
                        isSingleQuantity = event.isChecked, deliveredQuantity = if (event.isChecked) 1.0 else deliveredQuantity
                    )
                }

            }

            is PlannedGoodsAcceptanceContract.Event.OnFetchStockTransaction -> {
                if (event.stockTransactionDocument == null) {
                    setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError("Stok hareketi evrak bilgileri eksik olamaz") }
                    return
                }

                val (_, documentSeries, documentNumber, _, transactionType, transactionKind, isNormalOrReturn, documentType) = event.stockTransactionDocument

                fetchStockTransactionByDocument(
                    documentSeries, documentNumber, transactionType, transactionKind, isNormalOrReturn, documentType
                )
            }

            is PlannedGoodsAcceptanceContract.Event.OnUseConfirmedOverQuantity -> {

                val barcode = currentState.fetchedProduct?.barcode ?: return
                val warehouseNumber = event.loggedUser.warehouseNumber
                val selectedDocuments = event.selectedDocuments
                val newDocumentsSeriesAndNumber = currentState.nextDocumentSeriesAndNumber ?: return
                val deliveredQuantity = currentState.deliveredQuantity
                val remainingQuantity = currentState.fetchedProduct?.remainingQty ?: return
                val exceededQuantity = deliveredQuantity - remainingQuantity

                val request = AddOrderUseCase.Request(
                    newOrderDocumentSeries = newDocumentsSeriesAndNumber.documentSeries,
                    newOrderDocumentNumber = newDocumentsSeriesAndNumber.documentNumber,
                    barcode = barcode,
                    quantity = exceededQuantity,
                    warehouseNumber = warehouseNumber,
                    documents = selectedDocuments.map { it.documentSeries to it.documentNumber })

                viewModelScope.launch {
                    addOrderUseCase(request).onStart {

                    }.collect { result ->
                        when (result) {
                            is Result.Loading -> {}
                            is Result.Success -> {}
                            is Result.Error -> {}
                        }
                    }
                }

                saveStockTransaction(
                    barcode = barcode,
                    quantity = remainingQuantity,
                    selectedDocuments = event.selectedDocuments,
                    stockTransactionDocument = event.stockTransactionDocument,
                    loggedUser = event.loggedUser
                )
            }

            is PlannedGoodsAcceptanceContract.Event.OnDeliveredQuantityChanged -> {
                setState {
                    copy(deliveredQuantity = event.deliveredQuantity)
                }
            }

            is PlannedGoodsAcceptanceContract.Event.OnUpdateOrderSyncStatus -> {
                val newOrderDocumentSeriesAndNumber = currentState.nextDocumentSeriesAndNumber ?: return

                val request = UpdateOrderSyncStatusUseCase.Request(
                    newOrderDocumentSeriesAndNumber.documentSeries,
                    newOrderDocumentSeriesAndNumber.documentNumber,
                    "Aktarılacak"
                )
                viewModelScope.launch {
                    updateOrderSyncStatusUseCase(request)
                        .onStart {

                        }
                        .collect { result ->
                            when(result){
                                is Result.Loading -> {}
                                is Result.Success -> {}
                                is Result.Error -> {}
                            }
                        }
                }
            }

        }
    }

    private fun saveStockTransaction(
        barcode: String,
        quantity: Double,
        selectedDocuments: List<DocumentUiModel>,
        stockTransactionDocument: StockTransactionDocumentUiModel,
        loggedUser: UserUiModel
    ) {
        val request = AddStockTransactionUseCase.Request(
            barcode = barcode,
            quantity = quantity,
            selectedDocuments = selectedDocuments.map { it.toDomainModel() },
            stockTransactionDocument = stockTransactionDocument.toDomainModel(),
            user = loggedUser.toDomainModel()
        )

        viewModelScope.launch {
            addStockTransactionUseCase(request).onStart {
                emit(Result.Loading)
            }.collect { result ->
                when (result) {
                    is Result.Loading -> {

                    }

                    is Result.Error -> setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError("Kayıt başarısız: ${result.message}") }
                    is Result.Success -> {
                        setEffect { PlannedGoodsAcceptanceContract.Effect.ShowSuccess("Kayıt başarılı") }
                        setState { copy(fetchedProduct = null, deliveredQuantity = 0.0) }
                    }
                }
            }
        }
    }

//    private fun savePlannedGoodsAcceptance(barcode: String, quantity: Double, selectedDocuments: List<DocumentUiModel>, userUiModel: UserUiModel) {
//
//    }

    private fun fetchStockTransactionByDocument(
        documentSeries: String, documentNumber: Int, transactionType: StockTransactionTypes, transactionKind: Byte, isNormalOrReturn: Byte, documentType: Byte
    ) {
        viewModelScope.launch {
            getStockTransactionsByDocumentUseCase(
                GetStockTransactionsByDocumentWithRemainingQuantityUseCase.Request(
                    transactionType, transactionKind, isNormalOrReturn, documentType, documentSeries, documentNumber
                )
            ).onStart {
                emit(Result.Loading)
            }.collectLatest { result ->
                when (result) {
                    is Result.Loading -> {

                    }

                    is Result.Success -> {
                        setState { copy(stockTransactions = result.data.stockTransactions.map { it.toUiModel() }) }
                    }

                    is Result.Error -> {
                        setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError(result.message) }
                    }
                }
            }
        }
    }

    private fun fetchNextOrderDocumentSeriesAndNumber(orderType: Byte, orderKind: Byte, documentSeries: String) {
        viewModelScope.launch {
            val request = GetNextOrderDocumentSeriesAndNumberUseCase.Request(orderType, orderKind, documentSeries)

            getNextOrderDocumentSeriesAndNumberUseCase(request).collect { result ->
                when (result) {
                    is Result.Loading -> {

                    }

                    is Result.Success<*> -> {
                        val nextDocument = (result.data as GetNextOrderDocumentSeriesAndNumberUseCase.Response).nextDocument.toUiModel()
                        setState { copy(nextDocumentSeriesAndNumber = nextDocument) }

                    }

                    is Result.Error -> {
                        setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError(result.message) }
                    }
                }
            }
        }
    }
}