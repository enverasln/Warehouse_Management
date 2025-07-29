package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.common.utils.DoubleExtensions.isNullOrZero
import tr.com.cetinkaya.domain.usecase.barcode.GetBarcodeDefinitionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddWarehouseGoodsTransferUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetNextStockTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.UpdateStockTransactionSyncStatusUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.AddTransferredDocumentUseCase
import tr.com.cetinkaya.domain.usecase.warehouse.GetWarehousesUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_goods_transfer.models.UserUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.WarehouseUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.toDomainModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.toUiModel
import javax.inject.Inject

@HiltViewModel
class WarehouseGoodsTransferViewModel @Inject constructor(
    private val getWarehousesUseCase: GetWarehousesUseCase,
    private val getBarcodeDefinitionByBarcodeUseCase: GetBarcodeDefinitionByBarcodeUseCase,
    private val getNextStockTransactionDocumentUseCase: GetNextStockTransactionDocumentUseCase,
    private val addWarehouseGoodsTransferUseCase: AddWarehouseGoodsTransferUseCase,
    private val getStockTransactionsByDocumentUseCase: GetStockTransactionsByDocumentUseCase,
    private val updateStockTransactionSyncStatusUseCase: UpdateStockTransactionSyncStatusUseCase,
    private val addTransferredDocumentUseCase: AddTransferredDocumentUseCase
) : BaseViewModel<WarehouseGoodsTransferContract.Event, WarehouseGoodsTransferContract.State, WarehouseGoodsTransferContract.Effect>() {

    override fun createInitialState(): WarehouseGoodsTransferContract.State = WarehouseGoodsTransferContract.State()

    init {
        onFetchWarehouses()
    }

    override fun handleEvent(event: WarehouseGoodsTransferContract.Event) {
        when (event) {
            is WarehouseGoodsTransferContract.Event.OnInitialize -> handleInitialize(event.loggedUser)
            is WarehouseGoodsTransferContract.Event.OnBarcodeEntered -> handleBarcodeEntered(event.barcode)
            is WarehouseGoodsTransferContract.Event.OnDocumentDialogConfirmed -> handleDocumentDialogConfirmed(event.stockTransactionDocument)
            is WarehouseGoodsTransferContract.Event.OnTransferredQuantityChanged -> updateTransferredQuantity(event.quantity)
            is WarehouseGoodsTransferContract.Event.OnWarehouseSelected -> updateSelectedWarehouse(event.warehouse)
            is WarehouseGoodsTransferContract.Event.OnUnitSelected -> updateSelectedUnit(event.selectedUnit)
            is WarehouseGoodsTransferContract.Event.OnSaveTransfer -> handleSaveTransfer()
            is WarehouseGoodsTransferContract.Event.OnFinishWarehouseTransfer -> handleFinishTransfer()
        }
    }

    private fun observeStockTransactions(stockTransactionDocument: StockTransactionDocumentUiModel) {

        viewModelScope.launch {
            getStockTransactionsByDocumentUseCase(
                GetStockTransactionsByDocumentUseCase.Request(
                    transactionType = stockTransactionDocument.transactionType,
                    transactionKind = stockTransactionDocument.transactionKind,
                    isNormalOrReturn = stockTransactionDocument.isNormalOrReturn,
                    documentType = stockTransactionDocument.documentType,
                    documentSeries = stockTransactionDocument.documentSeries,
                    documentNumber = stockTransactionDocument.documentNumber
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val stockTransactions = result.data.stockTransactions.map { it -> it.toUiModel() }
                        setState { copy(transferredProducts = stockTransactions) }
                    }

                    is Result.Loading -> {}
                    is Result.Error -> {}
                }


            }
        }
    }

    private fun handleInitialize(loggedUser: UserUiModel?) {
        setState { copy(loggedUser = loggedUser) }
        fetchInitialStockDocument(loggedUser)
    }

    private fun handleBarcodeEntered(barcode: String) {
        fetchBarcodeDefinition(barcode)
    }

    private fun handleDocumentDialogConfirmed(stockTransactionDocument: StockTransactionDocumentUiModel?) {
        setState { copy(stockTransactionDocument = stockTransactionDocument) }
        setEffect { WarehouseGoodsTransferContract.Effect.DismissDialog }
        observeStockTransactions(stockTransactionDocument!!)
    }

    private fun updateTransferredQuantity(quantity: Double) {
        setState { copy(quantity = quantity) }
    }

    private fun updateSelectedWarehouse(warehouse: WarehouseUiModel) {
        setState { copy(selectedWarehouse = warehouse) }
    }

    private fun updateSelectedUnit(selectedUnit: String) {
        setState { copy(selectedUnit = selectedUnit) }
    }

    private fun handleSaveTransfer() {
        val state = currentState

        val error = validateTransferState(state)

        if (error != null) {
            setEffect { WarehouseGoodsTransferContract.Effect.ShowError(error) }
            return
        }

        val (loggedUser, stockTransactionDocument, _, _, selectedWarehouse, _, _, barcodeDefinition) = state

        val transferredQuantity = calculateTransferredQuantity(state)
        val unitPrice = barcodeDefinition!!.price1
        val totalPrice = transferredQuantity * unitPrice

        performSaveTransfer(
            barcodeDefinition = barcodeDefinition,
            stockTransactionDocument = stockTransactionDocument!!,
            loggedUser = loggedUser!!,
            selectedWarehouse = selectedWarehouse!!,
            transferredQuantity = transferredQuantity,
            totalPrice = totalPrice
        )
    }

    private fun fetchBarcodeDefinition(barcode: String) {
        Log.d("WarehouseGoodsTransferViewModel", "fetchBarcodeDefinition: $barcode")
        viewModelScope.launch {
            val warehouseNumber = currentState.loggedUser?.warehouseNumber
            if (warehouseNumber == null) return@launch
            getBarcodeDefinitionByBarcodeUseCase(GetBarcodeDefinitionByBarcodeUseCase.Request(barcode, warehouseNumber)).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {

                    }

                    is Result.Success -> {
                        val barcodeDefinition = result.data.barcodeDefinition.toUiModel()
                        setState { copy(barcodeDefinition = barcodeDefinition, quantity = 1.0) }
                        setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnQuantity }
                    }

                    is Result.Error -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }
                        setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnBarcode }
                    }
                }
            }
        }
    }

    private fun onFetchWarehouses() {
        viewModelScope.launch {
            getWarehousesUseCase(GetWarehousesUseCase.Request).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val warehouses = result.data.warehouses.map { it.toUiModel() }.filter { it.isActive }
                            .filter { it.number != currentState.loggedUser?.warehouseNumber }
                        setState { copy(warehouses = warehouses, selectedWarehouse = warehouses.firstOrNull()) }
                    }

                    is Result.Loading -> {}
                    is Result.Error -> {}
                }
            }
        }
    }

    private fun fetchInitialStockDocument(loggedUser: UserUiModel?) {
        viewModelScope.launch {
            val documentSeries = loggedUser?.documentSeries ?: return@launch
            getNextStockTransactionDocumentUseCase(
                GetNextStockTransactionDocumentUseCase.Request(
                    stockTransactionType = 2,
                    stockTransactionKind = 6,
                    isStockTransactionNormalOrReturn = 0,
                    stockTransactionDocumentType = 17,
                    documentSeries = documentSeries
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Error -> {

                    }

                    is Result.Loading -> {

                    }

                    is Result.Success -> {
                        setState { copy(stockTransactionDocument = result.data.stockTransactionDocument.toUiModel()) }
                        val stockTransactionDocument = result.data.stockTransactionDocument
                        setEffect {
                            WarehouseGoodsTransferContract.Effect.ShowDocumentDialog(
                                stockTransactionDocument.documentSeries,
                                stockTransactionDocument.documentNumber,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun validateTransferState(state: WarehouseGoodsTransferContract.State): String? {
        return when {
            state.barcodeDefinition == null -> "Ürün bilgisi eksik"
            state.stockTransactionDocument == null -> "Depolar arası transfer evrak bilgileri eksik"
            state.loggedUser == null -> "Kullanıcı bilgisi eksik"
            state.selectedWarehouse == null -> "Hedef depo seçilmedi"
            else -> null
        }
    }

    private fun calculateTransferredQuantity(state: WarehouseGoodsTransferContract.State): Double {
        val barcodeDefinition = state.barcodeDefinition ?: return state.quantity
        return when (state.selectedUnit) {
            "Adet" -> state.quantity
            "Paket" -> {
                val coef = barcodeDefinition.unit2Coefficient
                if (coef.isNullOrZero()) state.quantity else state.quantity * coef
            }

            "Koli" -> {
                val coef = barcodeDefinition.unit3Coefficient
                if (coef.isNullOrZero()) state.quantity else state.quantity * coef
            }

            else -> state.quantity
        }
    }

    private fun performSaveTransfer(
        barcodeDefinition: tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.BarcodeDefinitionUiModel,
        stockTransactionDocument: StockTransactionDocumentUiModel,
        loggedUser: UserUiModel,
        selectedWarehouse: WarehouseUiModel,
        transferredQuantity: Double,
        totalPrice: Double
    ) {
        viewModelScope.launch {
            addWarehouseGoodsTransferUseCase(
                AddWarehouseGoodsTransferUseCase.Request(
                    stockCode = barcodeDefinition.stockCode,
                    stockName = barcodeDefinition.stockName,
                    barcode = barcodeDefinition.barcode,
                    quantity = transferredQuantity,
                    price = totalPrice,
                    stockTransactionDocument = stockTransactionDocument.toDomainModel(),
                    inputWarehouseNumber = selectedWarehouse.number,
                    outputWarehouseNumber = loggedUser.warehouseNumber,
                    responsibilityCenter = loggedUser.warehouseNumber.toString(),
                    userCode = loggedUser.mikroFlyUserId,
                    taxPointer = 0,
                    isColorizedAndSized = barcodeDefinition.isColoredAndSized
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowSuccess("Bilgiler başarı ile kayıt edildi") }
                        setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnBarcode }
                        observeStockTransactions(currentState.stockTransactionDocument!!)
                    }

                    is Result.Error -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }

                    }

                    is Result.Loading -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
                    }
                }
            }
        }
    }

    private fun handleFinishTransfer() {
        viewModelScope.launch {
            val documentSeries = currentState.stockTransactionDocument?.documentSeries ?: return@launch
            val documentNumber = currentState.stockTransactionDocument?.documentNumber ?: return@launch
            updateStockTransactionSyncStatusUseCase(
                UpdateStockTransactionSyncStatusUseCase.Request(
                    documentSeries, documentNumber, "Aktarılacak"
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {

                    }

                    is Result.Success -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
                        addTransferredDocument(documentSeries, documentNumber)
                    }

                    is Result.Error -> {
                        // Herhangi bir hata ile karşılaşılırsa kayıt tekrar yeni kayıt olarak işaretlenir.
                        updateStockTransactionSyncStatusUseCase(
                            UpdateStockTransactionSyncStatusUseCase.Request(
                                documentSeries, documentNumber, "Yeni Kayıt"
                            )
                        ).collect()
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }
                    }
                }

            }

        }
    }

    private fun addTransferredDocument(documentSeries: String, documentNumber: Int) {
        viewModelScope.launch {
            addTransferredDocumentUseCase(
                AddTransferredDocumentUseCase.Request(
                    transferredDocumentTypes = TransferredDocumentTypes.WAREHOUSE_TRANSFER,
                    documentSeries = documentSeries,
                    documentNumber = documentNumber,
                    description = "Aktarılacak",
                    synchronizationStatus = false
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {

                    }

                    is Result.Success -> {

                    }

                    is Result.Error -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }
                    }

                }
            }
        }

    }
}