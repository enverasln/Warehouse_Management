package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.TransferUnit
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.common.flow.awaitResult
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.common.utils.DoubleExtensions.isNullOrZero
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.AddStockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.AddTransferredDocumentDomainModel
import tr.com.cetinkaya.domain.usecase.barcode.GetBarcodeDefinitionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.stock.GetStockBuyingConditionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CheckDocumentIsUsableUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.FinishStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetNextStockTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.RemoveStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.RemoveTransferredDocumentUseCase
import tr.com.cetinkaya.domain.usecase.warehouse.GetWarehousesUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_goods_transfer.models.UserUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.BarcodeDefinitionUiModel
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
    private val checkDocumentIsUsableUseCase: CheckDocumentIsUsableUseCase,
    private val removeStockTransactionUseCase: RemoveStockTransactionUseCase,
    private val removeTransferredDocumentUseCase: RemoveTransferredDocumentUseCase,
    private val addStockTransactionUseCase: AddStockTransactionUseCase,
    private val finishStockTransactionUseCase: FinishStockTransactionUseCase,
    private val getStockBuyingConditionUseCase: GetStockBuyingConditionUseCase,
    private val getStockTransactionsByDocumentUseCase: GetStockTransactionsByDocumentUseCase,
) : BaseViewModel<WarehouseGoodsTransferContract.Event, WarehouseGoodsTransferContract.State, WarehouseGoodsTransferContract.Effect>() {

    private enum class Field { BARCODE, USER, DEST_WAREHOUSE, DOCUMENT, QUANTITY }
    private data class FieldError(val field: Field, val message: String)

    private sealed class ValidationResult {
        data object Ok : ValidationResult()
        data class Fail(val errors: List<FieldError>) : ValidationResult()
    }

    private var docWatcherJob: Job? = null


    override fun createInitialState(): WarehouseGoodsTransferContract.State = WarehouseGoodsTransferContract.State()

    init {
        onFetchWarehouses()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun handleEvent(event: WarehouseGoodsTransferContract.Event) {
        when (event) {
            is WarehouseGoodsTransferContract.Event.OnInitialize -> onInitialize(event)
            is WarehouseGoodsTransferContract.Event.OnBarcodeEntered -> onBarcodeEntered(event)
            is WarehouseGoodsTransferContract.Event.OnWarehouseSelected -> onWarehouseSelected(event)
            is WarehouseGoodsTransferContract.Event.OnQuantityChanged -> onQuantityChanged(event)
            is WarehouseGoodsTransferContract.Event.OnDocumentDialogConfirmed -> handleDocumentDialogConfirmed(event.stockTransactionDocument)
            is WarehouseGoodsTransferContract.Event.OnTransferredQuantityChanged -> updateTransferredQuantity(event.quantity)
            is WarehouseGoodsTransferContract.Event.OnUnitSelected -> updateSelectedUnit(event.selectedUnit)
            is WarehouseGoodsTransferContract.Event.OnSaveTransfer -> handleSaveTransfer()
            is WarehouseGoodsTransferContract.Event.OnFinishWarehouseTransfer -> handleFinishTransfer()
            is WarehouseGoodsTransferContract.Event.OnDocumentNumberChanged -> getStockTransactionDocumentByDocumentNumber(
                event.documentSeries, event.documentNumber
            )

            is WarehouseGoodsTransferContract.Event.OnSelectStockTransaction -> {
                setState {
                    copy(
                        selectedStockTransaction = event.stockTransaction
                    )
                }
                setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnQuantity }
            }

            is WarehouseGoodsTransferContract.Event.OnCancelWarehouseTransfer -> {
                val documentSeries = currentState.stockTransactionDocument?.documentSeries ?: return
                val documentNumber = currentState.stockTransactionDocument?.documentNumber ?: return

                val removeTransferredDocumentRequest = RemoveTransferredDocumentUseCase.Request(
                    documentSeries = documentSeries,
                    documentNumber = documentNumber,
                    transferredDocumentType = TransferredDocumentTypes.WarehouseShipmentDocument
                )

                val removeStockTransactionUseCase = RemoveStockTransactionUseCase.Request(
                    documentSeries = documentSeries,
                    documentNumber = documentNumber,
                    transactionType = StockTransactionType.WarehouseTransfer,
                    transactionKind = StockTransactionKind.InternalTransfer,
                    isNormalOrReturn = 0,
                    transactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote,
                )
                viewModelScope.launch {
                    removeTransferredDocumentUseCase(removeTransferredDocumentRequest).flatMapConcat {
                        removeStockTransactionUseCase(removeStockTransactionUseCase)
                    }.collectLatest { result ->
                        when (result) {
                            is Result.Loading -> {

                            }

                            is Result.Success -> {
                                setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
                            }

                            is Result.Error -> {

                            }
                        }
                    }
                }
            }
        }
    }

    private fun onInitialize(event: WarehouseGoodsTransferContract.Event.OnInitialize) {
        handleInitialize(event.loggedUser)
    }

    private fun onBarcodeEntered(event: WarehouseGoodsTransferContract.Event.OnBarcodeEntered) {
        val raw = event.barcode.trim()
        if (raw.isEmpty()) {
            setEffect { WarehouseGoodsTransferContract.Effect.ShowError("Barkod boş olamaz.") }
            setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnBarcode }
            return
        }
        handleBarcodeEntered(raw)
    }

    private fun onWarehouseSelected(event: WarehouseGoodsTransferContract.Event.OnWarehouseSelected) {
        updateSelectedWarehouse(event.warehouse)
    }

    private fun onQuantityChanged(event: WarehouseGoodsTransferContract.Event.OnQuantityChanged) {
        val qty = event.quantity
        updateTransferredQuantity(qty)
    }

    private fun handleInitialize(loggedUser: UserUiModel?) {
        setState { copy(loggedUser = loggedUser) }
        fetchInitialStockDocument(loggedUser)
    }

    private fun handleBarcodeEntered(barcode: String) {
        fetchBarcodeDefinition(barcode)
    }

    private fun handleDocumentDialogConfirmed(stockTransactionDocument: StockTransactionDocumentUiModel?) {
        stockTransactionDocument?.let { doc ->
            viewModelScope.launch {
                checkDocumentIsUsableUseCase(
                    CheckDocumentIsUsableUseCase.Request(
                        documentSeries = doc.documentSeries,
                        documentNumber = doc.documentNumber,
                        companyCode = "",
                        paperNumber = "",
                        transactionType = doc.transactionType,
                        transactionKind = doc.transactionKind,
                        isNormalOrReturn = doc.isNormalOrReturn,
                        transactionDocumentType = doc.transactionDocumentType
                    )
                ).collect { result ->
                    when (result) {
                        is Result.Loading -> Unit
                        is Result.Success -> {
                            val documentStatus = result.data.documentStatus
                            if (documentStatus.isUsed == true && documentStatus.canBeUsed == false) {
                                setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(documentStatus.message) }
                                return@collect
                            }
                            setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(null) }
                            setState { copy(stockTransactionDocument = stockTransactionDocument) }
                            setEffect { WarehouseGoodsTransferContract.Effect.DismissDialog }
                            fetchStockTransaction(doc)
                        }

                        is Result.Error -> {
                            setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }
                        }
                    }
                }
            }
        }


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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleSaveTransfer() {
        val state = currentState

        // If user is editing/selected a row -> clear selection and refocus
        if (handleIfEditingSelected(state.selectedStockTransaction)) return

        val validationResult = validateForSave(state)
        if (!handleValidation(validationResult)) return

        // From here on, we can safely assume non-null inputs
        val loggedUser = state.loggedUser!!
        val stockTransactionDocument = state.stockTransactionDocument!!
        val selectedWarehouse = state.selectedWarehouse!!
        val selectedUnit = state.selectedUnit
        val barcodeDefinition = state.barcodeDefinition!!
        val baseQuantity = state.quantity

        viewModelScope.launch {
            setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }

            // Pure calculation: (no-side effect) : unit conversions, size factor, total quantity
            val (adjustedBaseQty, totalQty) = computeTotalQuantity(selectedUnit, barcodeDefinition, baseQuantity)

            // Fetch unit price via UseCase
            val priceReq = GetStockBuyingConditionUseCase.Request(
                currentCode = "",
                stockCode = barcodeDefinition.stockCode,
                date = DateConverter.uiToTimestamp(stockTransactionDocument.documentDate),
                warehouseNumber = loggedUser.warehouseNumber
            )

            when (val priceRes = getStockBuyingConditionUseCase(priceReq).awaitResult()) {
                is Result.Success -> {
                    val unitPrice = priceRes.data.stockBuyingConditionUseCase.grossPrice
                    val totalPrice = unitPrice * totalQty

                    // Build domain models (pure)
                    val stockTransaction = buildStockTransaction(
                        stockTransactionDocument, barcodeDefinition, totalQty, selectedWarehouse, loggedUser, totalPrice, unitPrice
                    )

                    val sizeTx = createSizeTransactionsIfExist(barcodeDefinition, adjustedBaseQty)

                    // Persist through UseCase
                    val addReq = AddStockTransactionUseCase.Request(stockTransaction, sizeTx)
                    when (val addRes = addStockTransactionUseCase(addReq).awaitResult()) {
                        is Result.Success -> {
                            setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                            // success feedback + focus back to barcode
                            setEffect { WarehouseGoodsTransferContract.Effect.ShowSuccess("Kayıt tamamlandı.") }
                            setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnBarcode }

                            // clear just the quantity and maybe selected row; keep document context
                            setState { copy(barcodeDefinition = null) }
                        }

                        is Result.Error -> {
                            setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                            setEffect { WarehouseGoodsTransferContract.Effect.ShowError(addRes.message) }
                        }

                        is Result.Loading -> Unit
                    }
                }

                is Result.Error -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    setEffect { WarehouseGoodsTransferContract.Effect.ShowError(priceRes.message) }
                }

                is Result.Loading -> Unit
            }
        }
    }

    /**
     * Returns true if handle the "editing selected row" case and caller should return.
     */
    private fun handleIfEditingSelected(selected: Any?): Boolean {
        if (selected != null) {
            setState { copy(selectedStockTransaction = null) }
            setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnBarcode }
            return true
        }
        return false
    }

    private fun validateForSave(state: WarehouseGoodsTransferContract.State): ValidationResult {
        val errors = buildList<FieldError> {
            if (state.barcodeDefinition == null) add(FieldError(Field.BARCODE, "Ürün bilgisine ulaşılamadı."))

            if (state.loggedUser == null) add(FieldError(Field.USER, "Kullanıcı bilgisine ulaşılamadı."))

            if (state.selectedWarehouse == null) add(FieldError(Field.DEST_WAREHOUSE, "Hedef depo seçilmedi."))

            if (state.stockTransactionDocument == null) add(FieldError(Field.DOCUMENT, "Evrak bilgisine ulaşılamadı."))

            if (state.quantity <= 0.0) add(FieldError(Field.QUANTITY, "Miktar 0'dan büyük olmalıdır."))

            if (state.loggedUser != null && state.selectedWarehouse != null && state.loggedUser.warehouseNumber == state.selectedWarehouse.warehouseNumber) add(
                FieldError(Field.DEST_WAREHOUSE, "Hedef depo, kaynak depo ile aynı olamaz.")
            )

            if (state.barcodeDefinition?.connectionType == 2.toByte() && state.barcodeDefinition.sizeBarcodes.isNullOrEmpty()) add(
                FieldError(
                    Field.BARCODE, "Bedenli ürün için beden listesi bulunamadı."
                )
            )

        }
        return if (errors.isEmpty()) ValidationResult.Ok else ValidationResult.Fail(errors)
    }

    private fun handleValidation(result: ValidationResult): Boolean {
        return when (result) {
            ValidationResult.Ok -> true
            is ValidationResult.Fail -> {
                val primary = result.errors.first()

                setEffect { WarehouseGoodsTransferContract.Effect.ShowError(primary.message) }

                when (primary.field) {
                    Field.BARCODE -> setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnBarcode }
                    Field.QUANTITY -> setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnQuantity }
                    Field.DOCUMENT -> setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(primary.message) }
                    Field.DEST_WAREHOUSE -> {}
                    Field.USER -> {}
                }

                false
            }
        }
    }

    /**
     * Compute total quantity by applying unit coefficient and optional size factor.
     * Keep this pure/deterministic for easy unit testing.
     */
    private fun computeTotalQuantity(
        selectedUnit: String, barcodeDefinition: BarcodeDefinitionUiModel, baseQuantity: Double
    ): Pair<Double, Double> {
        val unit = TransferUnit.fromDisplay(selectedUnit)
        val coef = coefFor(unit, barcodeDefinition)
        val adjustedBaseQty = baseQuantity * coef

        // if assortment barcode = true (connectionType == 2), derive size factor from size barcode list
        val sizeFactor = when {
            // Replace with your real property accessors:
            barcodeDefinition.connectionType == 2.toByte() -> {
                val sizeBarcodes = barcodeDefinition.sizeBarcodes ?: emptyList()
                if (sizeBarcodes.isEmpty()) 1.0 else sizeBarcodes.sumOf { it.quantity }
            }

            else -> 1.0
        }

        val totalQty = adjustedBaseQty * sizeFactor
        return adjustedBaseQty to totalQty
    }


    private fun createSizeTransactionsIfExist(
        barcodeDefinition: BarcodeDefinitionUiModel, adjustedBaseQty: Double
    ): List<AddSizeTransactionDomainModel> {
        val sizes: List<AddSizeTransactionDomainModel> = when {
            barcodeDefinition.connectionType == 2.toByte() -> (barcodeDefinition.sizeBarcodes ?: emptyList()).map { sb ->
                AddSizeTransactionDomainModel(
                    barcode = sb.barcode,
                    refRecordId = "",
                    sizeTransactionType = SizeTransactionType.StockTransaction,
                    quantity = sb.quantity * adjustedBaseQty
                )
            }

            barcodeDefinition.isColoredAndSized -> listOf(
                AddSizeTransactionDomainModel(
                    barcode = barcodeDefinition.barcode,
                    refRecordId = "",
                    sizeTransactionType = SizeTransactionType.StockTransaction,
                    quantity = adjustedBaseQty
                )
            )

            else -> emptyList()
        }
        return sizes
    }

    private fun buildStockTransaction(
        stockTransactionDocument: StockTransactionDocumentUiModel,
        barcodeDefinition: BarcodeDefinitionUiModel,
        totalQty: Double,
        selectedWarehouse: WarehouseUiModel,
        loggedUser: UserUiModel,
        totalPrice: Double,
        unitPrice: Double
    ): AddStockTransactionDomainModel {
        val stockTransaction = AddStockTransactionDomainModel(
            transactionType = StockTransactionType.WarehouseTransfer,
            transactionKind = StockTransactionKind.InternalTransfer,
            isNormalOrReturn = 0,
            transactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote,
            documentDate = DateConverter.uiToTimestamp(stockTransactionDocument.documentDate),
            documentSeries = stockTransactionDocument.documentSeries,
            documentNumber = stockTransactionDocument.documentNumber,
            lineNumber = 0,
            stockCode = barcodeDefinition.stockCode,
            stockName = barcodeDefinition.stockName,
            currentCode = "",
            quantity = totalQty,
            inputWarehouseNumber = selectedWarehouse.warehouseNumber,
            outputWarehouseNumber = loggedUser.warehouseNumber,
            paymentPlanNumber = 0,
            salesman = "",
            responsibilityCenter = loggedUser.warehouseNumber.toString(),
            userCode = loggedUser.mikroFlyUserId,
            totalPrice = totalPrice,
            discount1 = 0.0,
            discount2 = 0.0,
            discount3 = 0.0,
            discount4 = 0.0,
            discount5 = 0.0,
            taxPointer = 0,
            orderId = "",
            price = unitPrice,
            paperNumber = stockTransactionDocument.paperNumber,
            companyNumber = 0,
            storeNumber = 0,
            barcode = barcodeDefinition.barcode,
            isColoredAndSized = barcodeDefinition.isColoredAndSized,
            transportationStatus = 0
        )
        return stockTransaction
    }

    private fun coefFor(unit: TransferUnit, b: BarcodeDefinitionUiModel): Double = when (unit) {
        TransferUnit.Adet -> 1.0
        TransferUnit.Paket -> b.unit2Coefficient.takeUnless { it.isNullOrZero() } ?: 1.0
        TransferUnit.Koli -> b.unit3Coefficient.takeUnless { it.isNullOrZero() } ?: 1.0
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
                            .filter { it.warehouseNumber != currentState.loggedUser?.warehouseNumber }.sortedBy { it.name }
                        setState { copy(warehouses = warehouses, selectedWarehouse = warehouses.firstOrNull()) }
                    }

                    is Result.Loading -> {}
                    is Result.Error -> {}
                }
            }
        }
    }

    private fun fetchInitialStockDocument(loggedUser: UserUiModel?) {
        val documentSeries = loggedUser?.documentSeries ?: return

        viewModelScope.launch {
            val result = getNextStockTransactionDocumentUseCase(
                GetNextStockTransactionDocumentUseCase.Request(
                    stockTransactionType = StockTransactionType.WarehouseTransfer,
                    stockTransactionKind = StockTransactionKind.InternalTransfer,
                    isStockTransactionNormalOrReturn = 0,
                    stockTransactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote,
                    documentSeries = documentSeries
                )
            ).awaitResult()

            when (result) {
                is Result.Success -> {
                    val doc = result.data.stockTransactionDocument
                    setState { copy(stockTransactionDocument = doc.toUiModel()) }
                    setEffect {
                        WarehouseGoodsTransferContract.Effect.ShowDocumentDialog(
                            doc.documentSeries, doc.documentNumber
                        )
                    }
                }

                is Result.Error -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }
                }

                is Result.Loading -> Unit
            }
        }
    }

    private fun fetchStockTransaction(doc: StockTransactionDocumentUiModel) {
        docWatcherJob?.cancel()

        val fetchReq = GetStockTransactionsByDocumentUseCase.Request(
            transactionType = doc.transactionType,
            transactionKind = doc.transactionKind,
            isNormalOrReturn = doc.isNormalOrReturn,
            transactionDocumentType = doc.transactionDocumentType,
            documentSeries = doc.documentSeries,
            documentNumber = doc.documentNumber
        )

        docWatcherJob = viewModelScope.launch {
            getStockTransactionsByDocumentUseCase(fetchReq).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {}
                    is Result.Success -> {
                        val stockTransactions = result.data.stockTransactions.toUiModel()
                        setState { copy(transferredProducts = stockTransactions) }
                    }

                    is Result.Error -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }
                    }
                }
            }

        }
    }

    private fun handleFinishTransfer() {

        val (loggedUser, stockTransactionDocument, _, _, selectedWarehouse, units, selectedUnit, barcodeDefinition, quantity, selectedStockTransaction) = currentState

        if (stockTransactionDocument == null) {
            setEffect { WarehouseGoodsTransferContract.Effect.ShowError("Depolar arası transfer evrağı bilgileri eksik.") }
            return
        }

        val addTransferredDocumentDomainModel = AddTransferredDocumentDomainModel(
            transferredDocumentType = TransferredDocumentTypes.WarehouseShipmentDocument,
            documentSeries = stockTransactionDocument.documentSeries,
            documentNumber = stockTransactionDocument.documentNumber,
            currentCode = null,
            paperNumber = null
        )

        val request = FinishStockTransactionUseCase.Request(
            stockTransactionDocument = stockTransactionDocument.toDomainModel(), transferredDocument = addTransferredDocumentDomainModel
        )

        viewModelScope.launch {
            finishStockTransactionUseCase(request).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
                    }

                    is Result.Success -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                        setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
                    }

                    is Result.Error -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }
                    }
                }
            }
        }
    }

    private fun getStockTransactionDocumentByDocumentNumber(documentSeries: String, documentNumber: Int) {
        viewModelScope.launch {
            checkDocumentIsUsableUseCase(
                CheckDocumentIsUsableUseCase.Request(
                    documentSeries = documentSeries,
                    documentNumber = documentNumber,
                    companyCode = "",
                    paperNumber = "",
                    transactionType = StockTransactionType.WarehouseTransfer,
                    transactionKind = StockTransactionKind.InternalTransfer,
                    isNormalOrReturn = 0,
                    transactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote
                )
            ).collect { result ->
                when (result) {
                    is Result.Loading -> Unit
                    is Result.Success -> {
                        val documentStatus = result.data.documentStatus
                        if (documentStatus.isUsed == true && documentStatus.canBeUsed == false) {
                            setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(documentStatus.message) }
                            return@collect
                        }
                        setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(null) }
                    }

                    is Result.Error -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowError(result.message) }
                    }
                }
            }
        }
    }
}