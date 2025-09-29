package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import android.util.Log
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.TransferUnit
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.common.flow.awaitResult
import tr.com.cetinkaya.common.flow.awaitTerminal
import tr.com.cetinkaya.common.flow.withLoading
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.common.utils.DoubleExtensions.isNullOrZero
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.AddStockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.AddTransferredDocumentDomainModel
import tr.com.cetinkaya.domain.usecase.barcode.GetAssortmentBarcodesByStockCodeUseCase
import tr.com.cetinkaya.domain.usecase.barcode.GetBarcodeDefinitionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.stock.GetStockBuyingConditionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CheckDocumentIsUsableUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.DeleteStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.FinishStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetNextStockTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetTransferWareHouseNumberUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.RemoveStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.RemoveTransferredDocumentUseCase
import tr.com.cetinkaya.domain.usecase.warehouse.GetWarehousesUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import tr.com.cetinkaya.feature_common.dialog.global_dialog.DialogRequestRegistry
import tr.com.cetinkaya.feature_goods_transfer.models.UserUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.BarcodeDefinitionUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionUiModel
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
    private val getTransferWareHouseNumberUseCase: GetTransferWareHouseNumberUseCase,
    private val getStockTransactionsByDocumentUseCase: GetStockTransactionsByDocumentUseCase,
    private val deleteStockTransactionUseCase: DeleteStockTransactionUseCase,
    private val getAssortmentBarcodeByStockCodeUseCase: GetAssortmentBarcodesByStockCodeUseCase,
    appEventBus: AppEventBus,
    dialogRegister: DialogRequestRegistry
) : BaseViewModel<WarehouseGoodsTransferContract.Event, WarehouseGoodsTransferContract.State, WarehouseGoodsTransferContract.Effect>(
    appEventBus, dialogRegister
) {

    private enum class Field { BARCODE, USER, DEST_WAREHOUSE, DOCUMENT, QUANTITY }
    private data class FieldError(val field: Field, val message: String)

    private sealed class ValidationResult {
        data object Ok : ValidationResult()
        data class Fail(val errors: List<FieldError>) : ValidationResult()
    }

    private var docWatcherJob: Job? = null
    private var assortmentSearchJob: Job? = null
    private var exitConfirmJob: Job? = null
    private var deleteStockTxConfirm: Job? = null
    private var changeAssortmentBarcodeConfirm: Job? = null

    override fun createInitialState(): WarehouseGoodsTransferContract.State = WarehouseGoodsTransferContract.State()

    init {
        onFetchWarehouses()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun handleEvent(event: WarehouseGoodsTransferContract.Event) {
        when (event) {
            is WarehouseGoodsTransferContract.Event.OnInitialize -> onInitialize(event)
            is WarehouseGoodsTransferContract.Event.OnConfirmDocumentDialog -> onConfirmDocumentDialog(event)
            is WarehouseGoodsTransferContract.Event.OnBarcodeEntered -> onBarcodeEntered(event)
            is WarehouseGoodsTransferContract.Event.OnWarehouseSelected -> onWarehouseSelected(event)
            is WarehouseGoodsTransferContract.Event.OnQuantityChanged -> onQuantityChanged(event)
            is WarehouseGoodsTransferContract.Event.OnTransferredQuantityChanged -> updateTransferredQuantity(event.quantity)
            is WarehouseGoodsTransferContract.Event.OnUnitSelected -> updateSelectedUnit(event.selectedUnit)
            is WarehouseGoodsTransferContract.Event.OnSaveTransfer -> handleSaveTransfer()
            is WarehouseGoodsTransferContract.Event.OnClickFinish -> onClickFinish()
            is WarehouseGoodsTransferContract.Event.OnDocumentNumberChanged -> getStockTransactionDocumentByDocumentNumber(event)
            is WarehouseGoodsTransferContract.Event.OnClickGetAssortmentBarcodeIcon -> onGetAssortmentBarcodeIconClicked(event)
            is WarehouseGoodsTransferContract.Event.OnClickExit -> onExitClicked()
            is WarehouseGoodsTransferContract.Event.OnLongTapStockTx -> onStockTxLongTapped(event)
        }
    }

    private fun onGetAssortmentBarcodeIconClicked(event: WarehouseGoodsTransferContract.Event.OnClickGetAssortmentBarcodeIcon) {
        assortmentSearchJob?.cancel()
        assortmentSearchJob = viewModelScope.launch {
            searchAssortmentBarcode(event)
        }
    }

    private fun searchAssortmentBarcode(event: WarehouseGoodsTransferContract.Event.OnClickGetAssortmentBarcodeIcon) {
        val req = GetAssortmentBarcodesByStockCodeUseCase.Request(event.stockCode)

        getAssortmentBarcodeByStockCodeUseCase(req).withLoading().onEach { result ->
            when (result) {
                is Result.Loading -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
                }

                is Result.Success -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    val bc = result.data.barcode?.trim().orEmpty()
                    if (bc.isEmpty()) {
                        postGlobalError("Asorti barkodu bulunamadı")

                    } else {
                        if (changeAssortmentBarcodeConfirm?.isActive == true) return@onEach

                        changeAssortmentBarcodeConfirm = viewModelScope.launch {
                            val ok = askForConfirmation(
                                message = "Girilen barkod, asorti barkodu ile değiştirilecektir. İşlemi onaylıyor musunuz?",
                                title = "Onay",
                                positiveButtonText = "Evet",
                                negativeButtonText = "Hayır",
                                cancelable = false
                            )

                            if (ok) handleBarcodeEntered(bc)
                        }

                    }
                }

                is Result.Error -> {
                    // Hata: overlay’i kapat ve mesaj göster
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    postGlobalError(result.message)

                }
            }
        }.launchIn(viewModelScope)
    }

    private fun onStockTxLongTapped(event: WarehouseGoodsTransferContract.Event.OnLongTapStockTx) {
        if (deleteStockTxConfirm?.isActive == true) return
        deleteStockTxConfirm = viewModelScope.launch {
            val ok = askForDangerousConfirmation(
                title = "Dikkat",
                message = "Seçilen kayıt silinecektir.\n\n${event.stockTx.barcode} - ${event.stockTx.stockName}\n\n${event.stockTx.quantity} Adet",
                checkLabel = "Kaydı kalıcı olarak silmeyi onaylıyorum.",
                positiveButtonText = "Evet",
                negativeButtonText = "Hayır",
                cancelable = false
            )
            if (ok) handleDeleteStockTx(event.stockTx)
        }.also { job ->
            job.invokeOnCompletion { deleteStockTxConfirm = null }
        }
    }

    private fun onInitialize(event: WarehouseGoodsTransferContract.Event.OnInitialize) {
        handleInitialize(event.loggedUser)
    }

    private fun onConfirmDocumentDialog(event: WarehouseGoodsTransferContract.Event.OnConfirmDocumentDialog) {
        handleDocumentDialogConfirmed(event.stockTransactionDocument)
    }

    private fun onBarcodeEntered(event: WarehouseGoodsTransferContract.Event.OnBarcodeEntered) {
        val raw = event.barcode.trim()
        if (raw.isEmpty()) {
            postGlobalError("Barkod alanı boş bırakılamaz")
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
        if (loggedUser == null) {
            postGlobalError("Kullanıcı bilgilerine ulaşılamadı. Lütfen tekrar girişi yapınız.")
            setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
            return
        }
        setState { copy(loggedUser = loggedUser) }
        fetchNextStockTxDocument(loggedUser.documentSeries)
    }

    private fun handleBarcodeEntered(barcode: String) {
        fetchBarcodeDefinition(barcode)
    }

    private fun handleDeleteStockTx(stockTx: StockTransactionUiModel) {
        val req = DeleteStockTransactionUseCase.Request(stockTx.id)

        deleteStockTransactionUseCase(req).onEach { result ->
            when (result) {
                is Result.Loading -> setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
                is Result.Success -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    postGlobalSuccess("Kayıt başarı ile silindi")

                }

                is Result.Error -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    postGlobalError(result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun onExitClicked() {
        if (exitConfirmJob?.isActive == true) return // ikinci çağrıyı yut
        exitConfirmJob = viewModelScope.launch {
            val ok = askForDangerousConfirmation(
                title = "Dikkat",
                message = "Depo transferi evrağından çıkmaya çalışıyorsunuz.\n\nÇıkış yaptığınızda evrak silinecektir ve tekrar kurtarılamayacaktır.\nBu sayfadan çıkmak istediğinize emin misiniz?",
                checkLabel = "Evrağı kalıcı olarak silmeyi onaylıyorum.",
                positiveButtonText = "Evet",
                negativeButtonText = "Hayır",
                cancelable = false
            )
            if (ok) onWarehouseTransferCancelled()


        }.also { job ->
            job.invokeOnCompletion { exitConfirmJob = null }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun onWarehouseTransferCancelled() {
        val documentSeries = currentState.stockTxDoc?.documentSeries ?: return
        val documentNumber = currentState.stockTxDoc?.documentNumber ?: return

        val removeTransferredDocReq = RemoveTransferredDocumentUseCase.Request(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            transferredDocumentType = TransferredDocumentType.WarehouseShipmentDocument
        )

        val removeStockTxReq = RemoveStockTransactionUseCase.Request(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            transactionType = StockTransactionType.WarehouseTransfer,
            transactionKind = StockTransactionKind.InternalTransfer,
            isNormalOrReturn = 0,
            transactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote,
        )

        removeTransferredDocumentUseCase(removeTransferredDocReq).flatMapConcat {
            removeStockTransactionUseCase(removeStockTxReq)
        }.onEach { result ->
            when (result) {
                is Result.Loading -> setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }

                is Result.Success -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
                }

                is Result.Error -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    postGlobalError(result.message)
                    setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
                }
            }
        }.launchIn(viewModelScope)
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
        val stockTransactionDocument = state.stockTxDoc!!
        val selectedWarehouse = state.selectedWarehouse!!
        val selectedUnit = state.selectedUnit
        val barcodeDefinition = state.barcodeDefinition!!
        val baseQuantity = state.quantity

        viewModelScope.launch {
            setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
            val (adjustedBaseQty, totalQty) = computeTotalQuantity(selectedUnit, barcodeDefinition, baseQuantity)
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
                    val stockTransaction = buildStockTransaction(
                        stockTransactionDocument, barcodeDefinition, totalQty, selectedWarehouse, loggedUser, totalPrice, unitPrice
                    )
                    val sizeTx = createSizeTransactionsIfExist(barcodeDefinition, adjustedBaseQty)
                    val addReq = AddStockTransactionUseCase.Request(stockTransaction, sizeTx)
                    when (val addRes = addStockTransactionUseCase(addReq).awaitResult()) {
                        is Result.Success -> {
                            setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                            postGlobalSuccess("Kayıt başarıyla tamamlandı.")
                            setEffect { WarehouseGoodsTransferContract.Effect.RequestFocusOnBarcode }
                            setState { copy(barcodeDefinition = null, quantity = 1.0) }
                        }

                        is Result.Error -> {
                            setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                            postGlobalError(addRes.message)
                        }

                        is Result.Loading -> Unit
                    }
                }

                is Result.Error -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    postGlobalError(priceRes.message)
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

            if (state.stockTxDoc == null) add(FieldError(Field.DOCUMENT, "Evrak bilgisine ulaşılamadı."))

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

                postGlobalError(primary.message)

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
        val sizeFactor = if (barcodeDefinition.connectionType == 2.toByte()) {
            val sum = (barcodeDefinition.sizeBarcodes ?: emptyList()).sumOf { it.quantity }
            sum.takeIf { it > 0.0 } ?: 1.0
        } else 1.0
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
                        postGlobalError(result.message)
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

    private fun fetchNextStockTxDocument(docSeries: String) {
        val fetchStockDocument = GetNextStockTransactionDocumentUseCase.Request(
            stockTransactionType = StockTransactionType.WarehouseTransfer,
            stockTransactionKind = StockTransactionKind.InternalTransfer,
            isStockTransactionNormalOrReturn = 0,
            stockTransactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote,
            documentSeries = docSeries
        )
        viewModelScope.launch {
            val result = getNextStockTransactionDocumentUseCase(fetchStockDocument).awaitTerminal()

            if (result is Result.Success) {
                setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                val doc = result.data.stockTransactionDocument
                setEffect {
                    WarehouseGoodsTransferContract.Effect.ShowDocumentDialog(doc.documentSeries, doc.documentNumber)
                }
            }

            if (result is Result.Error) {
                postGlobalError(result.message)
                setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
            }

        }
    }

    private fun handleDocumentDialogConfirmed(stockTxDoc: StockTransactionDocumentUiModel?) {

        if (stockTxDoc == null) {
            postGlobalError("Doküman bilgisine ulaşılamadı.")
            return
        }
        val checkReq = CheckDocumentIsUsableUseCase.Request(stockTxDoc = stockTxDoc.toDomainModel(), currentCode = "")
        checkDocumentIsUsableUseCase(checkReq).onEach { result ->
            when (result) {
                is Result.Loading -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
                }

                is Result.Success -> {
                    val documentStatus = result.data.documentStatus

                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    if (documentStatus.isUsed == true && documentStatus.canBeUsed == false) {
                        setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(documentStatus.message) }
                        return@onEach
                    }
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissDialog }
                    setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(null) }
                    setState { copy(stockTxDoc = stockTxDoc) }
                    fetchStockTransactions(stockTxDoc)
                    val transferWarehouseNumberReq = GetTransferWareHouseNumberUseCase.Request(
                        documentSeries = stockTxDoc.documentSeries, documentNumber = stockTxDoc.documentNumber
                    )
                    getTransferWareHouseNumberUseCase(transferWarehouseNumberReq).withLoading().onEach { result ->
                        when (result) {
                            is Result.Loading -> {
                                setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
                            }

                            is Result.Success -> {
                                setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                                val selectedWarehouseNumber = result.data.warehouseNumber
                                selectedWarehouseNumber?.let {
                                    val selectedWarehouse = currentState.warehouses.firstOrNull { it.warehouseNumber == selectedWarehouseNumber }
                                    setState { copy(selectedWarehouse = selectedWarehouse) }
                                }

                                setState { copy(selectedWarehouse = currentState.warehouses.firstOrNull { it.warehouseNumber == result.data.warehouseNumber }) }
                            }

                            is Result.Error -> {}
                        }
                    }.launchIn(viewModelScope)

                }

                is Result.Error -> {
                    postGlobalError(result.message)
                    setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
                }
            }

        }.withLoading().launchIn(viewModelScope)

    }

    private fun fetchStockTransactions(doc: StockTransactionDocumentUiModel) {
        docWatcherJob?.cancel()
        val fetchReq = GetStockTransactionsByDocumentUseCase.Request(
            documentSeries = doc.documentSeries,
            documentNumber = doc.documentNumber,
            transactionType = StockTransactionType.WarehouseTransfer,
            transactionKind = StockTransactionKind.InternalTransfer,
            isNormalOrReturn = 0,
            transactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote
        )
        docWatcherJob = viewModelScope.launch {
            getStockTransactionsByDocumentUseCase(fetchReq).withLoading().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
                    }

                    is Result.Success -> {
                        setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                        val stockTransactions = result.data.stockTransactions.toUiModel()
                        setState { copy(stockTransactions = stockTransactions) }
                    }

                    is Result.Error -> {
                        postGlobalError(result.message)
                    }
                }
            }
        }
    }

    private fun onClickFinish() {
        viewModelScope.launch {
            val ok = askForConfirmation(
                title = "Onay",
                message = "Depo transfer işlemi tamamlanacaktır.\n\nİşlemi onaylıyor musunuz?",
                positiveButtonText = "Evet",
                negativeButtonText = "Hayır"
            )
            if (!ok) return@launch

            handleFinishTransfer()
        }
    }

    private fun handleFinishTransfer() {
        val stockTxDoc = currentState.stockTxDoc
        if (stockTxDoc == null) {
            postGlobalError("Depolar arası transfer evrağı bilgileri eksik.")
            setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
            return
        }
        val addTransferredDoc = AddTransferredDocumentDomainModel(
            transferredDocumentType = TransferredDocumentType.WarehouseShipmentDocument,
            documentSeries = stockTxDoc.documentSeries,
            documentNumber = stockTxDoc.documentNumber,
            currentCode = null,
            paperNumber = null
        )
        val request = FinishStockTransactionUseCase.Request(
            stockTxDoc = stockTxDoc.toDomainModel(), transferredDoc = addTransferredDoc
        )
        finishStockTransactionUseCase(request).onEach { result ->
            when (result) {
                is Result.Loading -> setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }

                is Result.Success -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    setEffect { WarehouseGoodsTransferContract.Effect.NavigateToMainMenu }
                }

                is Result.Error -> {
                    setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    postGlobalError(result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun getStockTransactionDocumentByDocumentNumber(event: WarehouseGoodsTransferContract.Event.OnDocumentNumberChanged) {
        if (event.stockTxDoc == null) {
            return
        }
        viewModelScope.launch {
            val checkReq = CheckDocumentIsUsableUseCase.Request(
                stockTxDoc = event.stockTxDoc.toDomainModel(), currentCode = ""
            )
            checkDocumentIsUsableUseCase(checkReq).withLoading().collect { result ->
                when (result) {
                    is Result.Loading -> setEffect { WarehouseGoodsTransferContract.Effect.ShowLoading }
                    is Result.Success -> {
                        val documentStatus = result.data.documentStatus
                        if (documentStatus.isUsed == true && documentStatus.canBeUsed == false) {
                            setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(documentStatus.message) }
                            return@collect
                        }
                        setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                        setEffect { WarehouseGoodsTransferContract.Effect.SetDialogBlockingError(null) }
                    }

                    is Result.Error -> {
                        postGlobalError(result.message)
                        setEffect { WarehouseGoodsTransferContract.Effect.DismissLoading }
                    }
                }
            }
        }
    }

}