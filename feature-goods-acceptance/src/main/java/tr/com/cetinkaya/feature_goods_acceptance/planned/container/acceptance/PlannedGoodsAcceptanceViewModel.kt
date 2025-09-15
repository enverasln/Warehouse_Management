package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.DataOrigin
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.common.flow.awaitResult
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.domain.model.order_transaction.AddOrderTransactionDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.AddStockTransactionDomainModel
import tr.com.cetinkaya.domain.usecase.order_transaction.GetNextOrderTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.AddOrderTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentWithRemainingQuantityUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.models.AddOrderTransactionParams
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.toDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order_transaction.OrderTransactionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.toDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.toDomainModel
import javax.inject.Inject

@HiltViewModel
class PlannedGoodsAcceptanceViewModel @Inject constructor(
    private val addStockTransactionUseCase: AddStockTransactionUseCase,
    private val addOrderTransactionUseCase: AddOrderTransactionUseCase,
    private val getStockTransactionsByDocumentUseCase: GetStockTransactionsByDocumentWithRemainingQuantityUseCase,
    private val getNextOrderTxDocUseCase: GetNextOrderTransactionDocumentUseCase,
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

            is PlannedGoodsAcceptanceContract.Event.OnFetchOrderTx -> {
                val addOrderTxParams =
                    event.orderTxs.filter { it.barcode == event.barcode }.groupBy { it.barcode to it.stockName }.map { (key, group) ->
                        AddOrderTransactionParams(
                            barcode = key.first,
                            stockName = key.second,
                            totalQty = group.sumOf { it.remainingQuantity },
                            totalRemainingQty = group.sumOf { it.remainingQuantity - it.deliveredQuantity },
                            deliveredQty = if (currentState.isSingleQuantity) 1.0 else group.sumOf { it.remainingQuantity - it.deliveredQuantity })
                    }.firstOrNull()


                if (addOrderTxParams == null) {
                    setState { copy(addOrderTxParams = null) }
                    setEffect { PlannedGoodsAcceptanceContract.Effect.ShowWarning("${event.barcode} numaralı barkoda ait ürün bilgisi bulunamadı.") }
                }
                setState { copy(addOrderTxParams = addOrderTxParams) }
            }

            is PlannedGoodsAcceptanceContract.Event.OnSaveWithCheckQuantity -> {
                val addOrderTxParams = currentState.addOrderTxParams ?: return
                val orderTxs = event.selectedOrderTxs.filter { it.remainingQuantity != it.deliveredQuantity }
                val stockTxDocument = event.stockTxDocument

                var totalDeliveredQty = addOrderTxParams.deliveredQty

                if(addOrderTxParams.deliveredQty > addOrderTxParams.totalRemainingQty) {
                    setEffect { PlannedGoodsAcceptanceContract.Effect.ShowOverQuantityDialog }
                    return
                }

                viewModelScope.launch {
                    for (orderTx in orderTxs) {
                        // find the remaining quantity of orderTx
                        val remainingQty = orderTx.remainingQuantity - orderTx.deliveredQuantity
                        var deliveredQty = 0.0

                        // if the remaining quantity is zero then continue the loop
                        if (remainingQty == 0.0) continue

                        // if the remaining quantity is greater than total quantity then set delivered quantity to total quantity
                        if (remainingQty >= totalDeliveredQty) deliveredQty = totalDeliveredQty

                        // if the remaining quantity is less than total quantity then set delivered quantity to remaining quantity
                        if (remainingQty < totalDeliveredQty) deliveredQty = remainingQty


                        // build stock transaction line
                        val toAddStockTx = buildStockTransactionDocument(
                            orderTx = orderTx, stockTxDocument = stockTxDocument, loggedUser = event.loggedUser, deliveredQty = deliveredQty
                        )

                        // create size transaction lines for stock transaction
                        val stockTxSizeTxs = if (orderTx.isColoredAndSized) createSizeTransactionsIfExist(
                            addOrderTxParams.copy(deliveredQty = deliveredQty), SizeTransactionType.StockTransaction
                        )
                        else emptyList()

                        // add stock transaction with size transactions
                        val addStockTxRes = AddStockTransactionUseCase.Request(stockTransaction = toAddStockTx, sizeTransactions = stockTxSizeTxs)

                        // build order transaction line
                        val toAddOrderTx = createOrderTransactionLine(orderTx.copy(deliveredQuantity = deliveredQty))

                        // create size transaction lines for order transaction
                        val toAddOrderTxSizeTxs = if (orderTx.isColoredAndSized) createSizeTransactionsIfExist(
                            addOrderTxParams.copy(deliveredQty = deliveredQty), SizeTransactionType.Order
                        )
                        else emptyList()

                        // add order transaction with size transactions
                        val addOrderTxRes = AddOrderTransactionUseCase.Request(toAddOrderTx, toAddOrderTxSizeTxs)

                        addStockTransactionUseCase(addStockTxRes).awaitResult()
                        when(val result = addOrderTransactionUseCase(addOrderTxRes).awaitResult()) {
                            is Result.Loading -> {}
                            is Result.Success -> {
                                setEffect { PlannedGoodsAcceptanceContract.Effect.ShowSuccess("Kayıt başarılı") }
                                setState { copy(addOrderTxParams = null) }
                            }
                            is Result.Error -> {
                                setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError("Kayıt başarısız: ${result.message}") }
                            }
                        }
                        totalDeliveredQty = totalDeliveredQty - deliveredQty
                    }

                }
            }

            is PlannedGoodsAcceptanceContract.Event.OnChangeSingleQuantityChecked -> {
                val deliveredQuantity = currentState.addOrderTxParams?.deliveredQty ?: 0.0

                setState {
                    copy(
                        isSingleQuantity = event.isChecked,
                        addOrderTxParams = addOrderTxParams?.copy(deliveredQty = if (event.isChecked) 1.0 else (addOrderTxParams.totalRemainingQty))
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

                val (addOrderTxParams, orderTxs, loggedUser) = event
                val lastOrderTxs = orderTxs.maxByOrNull { it.orderDate }!!
                val totalQty = addOrderTxParams.deliveredQty

                viewModelScope.launch {
                    if (totalQty > 0) {
                        // build order transaction line
                        val toAddOrderTx = createOrderTransactionLine(
                            lastOrderTxs.copy(
                                rowNumber = 0,
                                remainingQuantity = totalQty,
                                deliveredQuantity = 0.0,
                                quantity = totalQty,
                                totalPrice = lastOrderTxs.unitPrice * totalQty,
                                documentSeries = currentState.nextDocumentSeriesAndNumber?.docSeries!!,
                                documentNumber = currentState.nextDocumentSeriesAndNumber?.docNumber!!,
                                userCode = loggedUser.mikroFlyUserId
                            )
                        )
                        // create size transaction lines for order transaction
                        val toAddOrderTxSizeTxs = if (lastOrderTxs.isColoredAndSized) createSizeTransactionsIfExist(
                            addOrderTxParams.copy(deliveredQty = totalQty), SizeTransactionType.Order
                        )
                        else emptyList()
                        // add order transaction with size transactions
                        val addOrderTxRes = AddOrderTransactionUseCase.Request(toAddOrderTx, toAddOrderTxSizeTxs)

                        viewModelScope.launch {
                            addOrderTransactionUseCase(addOrderTxRes).awaitResult()
                        }
                    }
                }
            }

            is PlannedGoodsAcceptanceContract.Event.OnDeliveredQuantityChanged -> {
                setState { copy(addOrderTxParams = addOrderTxParams?.copy(deliveredQty = event.deliveredQuantity)) }
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
        val request = AddStockTransactionByBarcodeUseCase.Request(
            barcode = barcode,
            quantity = quantity,
            selectedDocuments = selectedDocuments.map { it.toDomainModel() },
            stockTransactionDocument = stockTransactionDocument.toDomainModel(),
            user = loggedUser.toDomainModel()
        )

    }

    private fun fetchStockTransactionByDocument(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType
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

    private fun fetchNextOrderDocumentSeriesAndNumber(orderType: OrderTransactionTypes, orderKind: OrderTransactionKinds, documentSeries: String) {
        viewModelScope.launch {
            val request = GetNextOrderTransactionDocumentUseCase.Request(orderType, orderKind, documentSeries)

            getNextOrderTxDocUseCase(request).collect { result ->
                when (result) {
                    is Result.Loading -> {

                    }

                    is Result.Success<*> -> {
                        val nextDocument = (result.data as GetNextOrderTransactionDocumentUseCase.Response).nextDocument
                        setState { copy(nextDocumentSeriesAndNumber = nextDocument) }

                    }

                    is Result.Error -> {
                        setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError(result.message) }
                    }
                }
            }
        }
    }

    private fun buildStockTransactionDocument(
        orderTx: OrderTransactionUiModel, stockTxDocument: StockTransactionDocumentUiModel, loggedUser: UserUiModel, deliveredQty: Double
    ): AddStockTransactionDomainModel {
        val stockTx = AddStockTransactionDomainModel(
            transactionType = stockTxDocument.transactionType,
            transactionKind = stockTxDocument.transactionKind,
            isNormalOrReturn = stockTxDocument.isNormalOrReturn,
            transactionDocumentType = stockTxDocument.transactionDocumentType,
            documentDate = DateConverter.uiToTimestamp(stockTxDocument.documentDate),
            documentSeries = stockTxDocument.documentSeries,
            documentNumber = stockTxDocument.documentNumber,
            lineNumber = 0,
            stockCode = orderTx.stockCode,
            stockName = orderTx.stockName,
            currentCode = orderTx.currentCode,
            quantity = deliveredQty,
            inputWarehouseNumber = loggedUser.warehouseNumber,
            outputWarehouseNumber = loggedUser.warehouseNumber,
            paymentPlanNumber = orderTx.paymentPlanNumber,
            salesman = loggedUser.username,
            responsibilityCenter = loggedUser.warehouseNumber.toString(),
            userCode = loggedUser.mikroFlyUserId,
            totalPrice = deliveredQty * orderTx.unitPrice,
            discount1 = orderTx.discount1,
            discount2 = orderTx.discount2,
            discount3 = orderTx.discount3,
            discount4 = orderTx.discount4,
            discount5 = orderTx.discount5,
            taxPointer = orderTx.taxPointer,
            orderId = orderTx.id,
            price = orderTx.unitPrice,
            paperNumber = stockTxDocument.paperNumber,
            companyNumber = 0,
            storeNumber = 0,
            barcode = orderTx.barcode,
            isColoredAndSized = orderTx.isColoredAndSized,
            transportationStatus = 0
        )
        return stockTx
    }

    private fun createSizeTransactionsIfExist(
        addOrderTxParams: AddOrderTransactionParams, sizeTransactionType: SizeTransactionType
    ): List<AddSizeTransactionDomainModel> {
        val sizeTx = AddSizeTransactionDomainModel(
            barcode = addOrderTxParams.barcode, refRecordId = "", sizeTransactionType = sizeTransactionType, quantity = addOrderTxParams.deliveredQty
        )
        return listOf(sizeTx)
    }

    private fun createOrderTransactionLine(orderTx: OrderTransactionUiModel): AddOrderTransactionDomainModel {
        return AddOrderTransactionDomainModel(
            orderDate = orderTx.orderDate,
            documentSeries = orderTx.documentSeries,
            documentNumber = orderTx.documentNumber,
            rowNumber = orderTx.rowNumber,
            stockId = orderTx.stockId,
            stockCode = orderTx.stockCode,
            stockName = orderTx.stockName,
            barcode = orderTx.barcode,
            currentId = orderTx.currentId,
            currentCode = orderTx.currentCode,
            currentName = orderTx.currentName,
            paymentPlanNumber = orderTx.paymentPlanNumber,
            warehouseId = orderTx.warehouseId,
            warehouseNumber = orderTx.warehouseNumber,
            warehouseName = orderTx.warehouseName,
            quantity = orderTx.quantity,
            unitPrice = orderTx.unitPrice,
            currencyType = orderTx.currencyType,
            discount1 = orderTx.discount1,
            discount2 = orderTx.discount2,
            discount3 = orderTx.discount3,
            discount4 = orderTx.discount4,
            discount5 = orderTx.discount5,
            totalPrice = orderTx.totalPrice,
            taxPointer = orderTx.taxPointer,
            currentResponsibilityCenter = orderTx.currentResponsibilityCenter,
            stockResponsibilityCenter = orderTx.stockResponsibilityCenter,
            remainingQuantity = orderTx.remainingQuantity,
            deliveredQuantity = orderTx.deliveredQuantity,
            isColoredAndSized = orderTx.isColoredAndSized,
            syncStatus = SyncStatus.New,
            userCode = orderTx.userCode,
            dataOrigin = DataOrigin.Local
        )
    }
}