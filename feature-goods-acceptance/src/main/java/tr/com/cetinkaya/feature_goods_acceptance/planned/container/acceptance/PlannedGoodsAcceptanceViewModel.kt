package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.flow.awaitResult
import tr.com.cetinkaya.domain.usecase.order_transaction.AddOrderTransactionUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.GetNextOrderTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentWithRemainingQuantityUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import tr.com.cetinkaya.feature_common.dialog.global_dialog.DialogRequestRegistry
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.OrderTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.SizeTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.factories.StockTransactionFactory
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.models.AddOrderTransactionParams
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.policy.AllocatedDelivery
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.policy.OverQuantityPolicy
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.policy.QuantityAllocator
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order_transaction.OrderTransactionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel
import javax.inject.Inject

@HiltViewModel
class PlannedGoodsAcceptanceViewModel @Inject constructor(
    private val addStockTransactionUseCase: AddStockTransactionUseCase,
    private val addOrderTransactionUseCase: AddOrderTransactionUseCase,
    private val getStockTransactionsByDocumentUseCase: GetStockTransactionsByDocumentWithRemainingQuantityUseCase,
    private val getNextOrderTxDocUseCase: GetNextOrderTransactionDocumentUseCase,
    // Policies / Factories (DIP)
    private val quantityAllocator: QuantityAllocator,
    private val stockTxFactory: StockTransactionFactory,
    private val orderTxFactory: OrderTransactionFactory,
    private val sizeTxFactory: SizeTransactionFactory,
    private val overQuantityPolicy: OverQuantityPolicy,
    appEventBus: AppEventBus,
    dialogRegister: DialogRequestRegistry
) : BaseViewModel<PlannedGoodsAcceptanceContract.Event, PlannedGoodsAcceptanceContract.State, PlannedGoodsAcceptanceContract.Effect>(appEventBus, dialogRegister) {
    companion object {
        const val TAG = "PlannedGoodsAcceptanceViewModel"
    }

    override fun createInitialState(): PlannedGoodsAcceptanceContract.State = PlannedGoodsAcceptanceContract.State()

    override fun handleEvent(event: PlannedGoodsAcceptanceContract.Event) {
        when (event) {
            is PlannedGoodsAcceptanceContract.Event.OnFetchNextDocument -> handleFetchNextDocument(event)
            is PlannedGoodsAcceptanceContract.Event.OnFetchOrderTx -> handleFetchOrderTx(event)
            is PlannedGoodsAcceptanceContract.Event.OnSaveWithCheckQuantity -> handleSaveWithCheckQuantity(event)
            is PlannedGoodsAcceptanceContract.Event.OnChangeSingleQuantityChecked -> handleChangeSingleQuantity(event)
            is PlannedGoodsAcceptanceContract.Event.OnFetchStockTransaction -> handleFetchStockTransaction(event)
            is PlannedGoodsAcceptanceContract.Event.OnUseConfirmedOverQuantity -> handleConfirmedOverQuantity(event)
            is PlannedGoodsAcceptanceContract.Event.OnDeliveredQuantityChanged -> handleChangedDeliveredQuantity(event)
        }
    }

    // region Handlers
    private fun handleFetchNextDocument(event: PlannedGoodsAcceptanceContract.Event.OnFetchNextDocument) {
        fetchNextOrderDocumentSeriesAndNumber(event.orderType, event.orderKind, event.documentSeries)
    }

    private fun handleFetchOrderTx(event: PlannedGoodsAcceptanceContract.Event.OnFetchOrderTx) {
        val addParams = buildAddOrderParams(event.orderTxs, event.barcode, currentState.isSingleQuantity)
        if (addParams == null) {
            setState { copy(addOrderTxParams = null) }
            setEffect { PlannedGoodsAcceptanceContract.Effect.ShowWarning("${event.barcode} numaralı barkoda ait ürün bilgisi bulunamadı.") }
            return
        }
        setState { copy(addOrderTxParams = addParams) }
    }

    private fun handleSaveWithCheckQuantity(event: PlannedGoodsAcceptanceContract.Event.OnSaveWithCheckQuantity) {
        val addParams = currentState.addOrderTxParams ?: return
        val orderTxs = event.selectedOrderTxs.filter { it.remainingQuantity != it.deliveredQuantity }
        val stockDoc = event.stockTxDocument
        val user = event.loggedUser

        val delivered = addParams.deliveredQty
        val totalRemaining = addParams.totalRemainingQty
        if (delivered > totalRemaining) {
            when (overQuantityPolicy) {
                OverQuantityPolicy.Reject -> {
                    setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError("Girilen miktar kalan toplam miktarı aştığı için işlem yapılamadı.") }
                    return
                }

                OverQuantityPolicy.AskUser -> {
                    setEffect { PlannedGoodsAcceptanceContract.Effect.ShowOverQuantityDialog }
                    return
                }

                OverQuantityPolicy.AutoNewOrder -> {
                    val basePortion = addParams.totalRemainingQty.coerceAtLeast(0.0)
                    val overPortion = (delivered - totalRemaining).coerceAtLeast(0.0)
                    val baseAllocations = quantityAllocator.allocate(basePortion, orderTxs)
                    viewModelScope.launch {
                        if (basePortion > 0 && baseAllocations.isNotEmpty()) persistAllocations(baseAllocations, addParams, stockDoc, user)

                        if (overPortion > 0) createNewOrderForOverQty(overPortion, orderTxs, addParams, user)
                        setState { copy(addOrderTxParams = null) }
                    }
                    return
                }
            }
        }

        val allocations = quantityAllocator.allocate(delivered, orderTxs)
        if (allocations.isEmpty()) {
            setEffect { PlannedGoodsAcceptanceContract.Effect.ShowWarning("Teslim alınacak miktar bulunamadı") }
            return
        }

        viewModelScope.launch {
            persistAllocations(allocations, addParams, stockDoc, user)
            setState { copy(addOrderTxParams = null) }
        }

    }

    private fun handleChangeSingleQuantity(event: PlannedGoodsAcceptanceContract.Event.OnChangeSingleQuantityChecked) {
        val current = currentState.addOrderTxParams
        val newDelivered = if (event.isChecked) 1.0 else current?.totalRemainingQty ?: 0.0
        setState { copy(isSingleQuantity = event.isChecked, addOrderTxParams = current?.copy(deliveredQty = newDelivered)) }
    }

    private fun handleFetchStockTransaction(event: PlannedGoodsAcceptanceContract.Event.OnFetchStockTransaction) {
        val stockDoc = event.stockTransactionDocument
        if (stockDoc == null) {
            setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError("Stok hareketi evrak bilgileri eksik olamaz") }
            return
        }
        fetchStockTransactionByDocument(
            stockDoc.documentSeries,
            stockDoc.documentNumber,
            stockDoc.transactionType,
            stockDoc.transactionKind,
            stockDoc.isNormalOrReturn,
            stockDoc.transactionDocumentType
        )
    }

    private fun handleConfirmedOverQuantity(event: PlannedGoodsAcceptanceContract.Event.OnUseConfirmedOverQuantity) {
        val (params, orderTxs, user, stockDoc) = event
        val delivered = params.deliveredQty
        val totalRemaining = params.totalRemainingQty

        // 1) Önce kalanlara dağıt
        val basePortion = delivered.coerceAtMost(totalRemaining).coerceAtLeast(0.0)
        // 2) Artan miktarı hesapla
        val overPortion = (delivered - totalRemaining).coerceAtLeast(0.0)

        // Sadece teslim alınabilecek satırlarla çalış
        val openOrderTxs = orderTxs.filter { it.remainingQuantity > it.deliveredQuantity }

        viewModelScope.launch {
            if (basePortion > 0) {
                val baseAllocations = quantityAllocator.allocate(basePortion, openOrderTxs)
                if (baseAllocations.isNotEmpty()) {
                    persistAllocations(baseAllocations, params, stockDoc, user)
                }
            }

            if (overPortion > 0) {
                createNewOrderForOverQty(overPortion, orderTxs, params, user)
            }

            setState { copy(addOrderTxParams = null) }
        }
    }

    private fun handleChangedDeliveredQuantity(event: PlannedGoodsAcceptanceContract.Event.OnDeliveredQuantityChanged) {
        setState {
            copy(
                addOrderTxParams = addOrderTxParams?.copy(
                    deliveredQty = event.deliveredQuantity
                )
            )
        }
    }
    // endregion

    // region Helpers
    private suspend fun persistAllocations(
        allocations: List<AllocatedDelivery>, params: AddOrderTransactionParams, stockDoc: StockTransactionDocumentUiModel, user: UserUiModel
    ) {
        for ((orderTx, delivered) in allocations) {
            val stockTx = stockTxFactory.from(orderTx, stockDoc, user, delivered)
            val stockSizeTxs = sizeTxFactory.forStock(params, delivered, orderTx.isColoredAndSized)
            val addStockReq = AddStockTransactionUseCase.Request(stockTransaction = stockTx, sizeTransactions = stockSizeTxs)
            addStockTransactionUseCase(addStockReq).awaitResult()

            val updatedOrderTx = orderTx.copy(deliveredQuantity = delivered)
            val orderLine = orderTxFactory.from(updatedOrderTx)
            val orderSizeTxs = sizeTxFactory.forOrder(params, delivered, orderTx.isColoredAndSized)
            val addOrderReq = AddOrderTransactionUseCase.Request(orderLine, orderSizeTxs)
            addOrderTransactionUseCase(addOrderReq).collectLatest { result ->
                when (result) {
                    is Result.Success -> setEffect { PlannedGoodsAcceptanceContract.Effect.ShowSuccess("Kayıt başarılı") }
                    is Result.Error -> setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError("Kayıt başarısız: ${result.message}") }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private suspend fun createNewOrderForOverQty(
        overQty: Double, orderTxs: List<OrderTransactionUiModel>, params: AddOrderTransactionParams, user: UserUiModel
    ) {
        if (overQty <= 0.0) return
        val lastOrderTx = orderTxs.maxByOrNull { it.orderDate } ?: return
        val next = currentState.nextDocumentSeriesAndNumber
        if (next == null) {
            setEffect { PlannedGoodsAcceptanceContract.Effect.ShowError("Yeni evrak için seri/sıra hazır değil.") }
            return
        }
        val synthetic = lastOrderTx.copy(
            rowNumber = 0,
            remainingQuantity = overQty,
            deliveredQuantity = 0.0,
            quantity = overQty,
            totalPrice = lastOrderTx.unitPrice * overQty,
            documentSeries = next.docSeries,
            documentNumber = next.docNumber,
            userCode = user.mikroFlyUserId
        )
        val line = orderTxFactory.from(synthetic)
        val sizeTxs = sizeTxFactory.forOrder(params, overQty, lastOrderTx.isColoredAndSized)
        val req = AddOrderTransactionUseCase.Request(line, sizeTxs)
        addOrderTransactionUseCase(req).collect { /* ignore; üst katman başarı mesajını gösterebilir */ }
    }

    private fun buildAddOrderParams(orderTxs: List<OrderTransactionUiModel>, barcode: String, isSingleQuantity: Boolean): AddOrderTransactionParams? {
        val group = orderTxs.filter { it.barcode == barcode }
        if (group.isEmpty()) return null
        val totalRemaining = group.sumOf { (it.remainingQuantity - it.deliveredQuantity).coerceAtLeast(0.0) }
        val totalQty = group.sumOf { it.remainingQuantity } // for display
        val delivered = if (isSingleQuantity) 1.0 else totalRemaining
        return AddOrderTransactionParams(
            barcode = barcode, stockName = group.first().stockName, totalQty = totalQty, totalRemainingQty = totalRemaining, deliveredQty = delivered
        )
    }

    private fun fetchNextOrderDocumentSeriesAndNumber(orderType: OrderTransactionTypes, orderKind: OrderTransactionKinds, documentSeries: String) {
        viewModelScope.launch {
            val request = GetNextOrderTransactionDocumentUseCase.Request(orderType, orderKind, documentSeries)

            getNextOrderTxDocUseCase(request).collect { result ->
                when (result) {
                    is Result.Loading -> {}

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
    // endregion

}