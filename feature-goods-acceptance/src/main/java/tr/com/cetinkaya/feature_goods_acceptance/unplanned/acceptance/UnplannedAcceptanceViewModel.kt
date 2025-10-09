package tr.com.cetinkaya.feature_goods_acceptance.unplanned.acceptance

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
import tr.com.cetinkaya.common.flow.awaitTerminal
import tr.com.cetinkaya.common.flow.withLoading
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.common.utils.DoubleExtensions.isNullOrZero
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.AddStockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.usecase.barcode.GetAssortmentBarcodesByStockCodeUseCase
import tr.com.cetinkaya.domain.usecase.barcode.GetBarcodeDefinitionByBarcodeUseCase
import tr.com.cetinkaya.domain.usecase.stock.GetStockBuyingConditionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.AddStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CheckDocumentIsUsableUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.DeleteStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.FinishStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetNextStockTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionsByDocumentUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.RemoveStockTransactionUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.RemoveTransferredDocumentUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import tr.com.cetinkaya.feature_common.dialog.global_dialog.DialogRequestRegistry
import tr.com.cetinkaya.feature_goods_acceptance.models.barcode_definition.BarcodeDefinitionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.models.barcode_definition.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.models.stock_transaction.StockTransactionUiModel
import tr.com.cetinkaya.feature_goods_acceptance.models.stock_transaction.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.models.user.UserUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.toDomainModel
import javax.inject.Inject

@HiltViewModel
class UnplannedAcceptanceViewModel @Inject constructor(
    private val getNextStockTxDocUseCase: GetNextStockTransactionDocumentUseCase,
    private val checkDocumentIsUsableUseCase: CheckDocumentIsUsableUseCase,
    private val getBarcodeDefinitionByBarcodeUseCase: GetBarcodeDefinitionByBarcodeUseCase,
    private val getStockBuyingConditionUseCase: GetStockBuyingConditionUseCase,
    private val addStockTxUseCase: AddStockTransactionUseCase,
    private val getStockTxsByDocUseCase: GetStockTransactionsByDocumentUseCase,
    private val getAssortmentBarcodesByStockCodeUseCase: GetAssortmentBarcodesByStockCodeUseCase,
    private val finishStockTxUseCase: FinishStockTransactionUseCase,
    private val removeStockTransactionUseCase: RemoveStockTransactionUseCase,
    private val removeTransferredDocumentUseCase: RemoveTransferredDocumentUseCase,
    private val deleteStockTxUseCase: DeleteStockTransactionUseCase,
    appEventBus: AppEventBus,
    dialogRegister: DialogRequestRegistry
) : BaseViewModel<UnplannedAcceptanceContract.Event, UnplannedAcceptanceContract.State, UnplannedAcceptanceContract.Effect>(
    appEventBus, dialogRegister
) {
    companion object {
        private const val TAG = "UnplannedAcceptanceViewModel"
    }

    private var fetchNextDocSeriesJob: Job? = null
    private var checkStockTxDocJob: Job? = null
    private var fetchStockTxsByDocJob: Job? = null
    private var fetchAssortmentBarcodeJob: Job? = null
    private var changeAssortmentBarcodeConfirmJob: Job? = null
    private var finishStockTxJob: Job? = null
    private var exitConfirmJob: Job? = null
    private var deleteStockTxJob: Job? = null
    private var saveJob: Job? = null
    private var barcodeJob: Job? = null

    override fun createInitialState(): UnplannedAcceptanceContract.State = UnplannedAcceptanceContract.State()

    override fun handleEvent(event: UnplannedAcceptanceContract.Event) {
        when (event) {
            is UnplannedAcceptanceContract.Event.OnInitialize -> onInitialize(event)
            is UnplannedAcceptanceContract.Event.OnConfirmDocumentDialog -> onConfirmDocumentDialog(event)
            is UnplannedAcceptanceContract.Event.OnCancelDocumentDialog -> setEffect { UnplannedAcceptanceContract.Effect.NavigateToMainMenu }
            is UnplannedAcceptanceContract.Event.OnBarcodeEntered -> onBarcodeEntered(event)
            is UnplannedAcceptanceContract.Event.OnSaveAcceptance -> onSaveAcceptance(event)
            is UnplannedAcceptanceContract.Event.OnQuantityChanged -> onQuantityChanged(event)
            is UnplannedAcceptanceContract.Event.OnClickAssortmentBarcodeIcon -> onClickAssortmentBarcodeIcon(event)
            is UnplannedAcceptanceContract.Event.OnClickFinish -> onClickFinish(event)
            is UnplannedAcceptanceContract.Event.OnClickExit -> onClickExit(event)
            is UnplannedAcceptanceContract.Event.OnLongTapStockTx -> onLongTapStockTx(event)
            is UnplannedAcceptanceContract.Event.OnChangeSingleQuantityCheckStatus -> onChangeSingleQuantityCheckStatus(event)
        }
    }

    private fun onChangeSingleQuantityCheckStatus(event: UnplannedAcceptanceContract.Event.OnChangeSingleQuantityCheckStatus) {
        setState { copy(singleQuantityStatus = event.isChecked, quantity = 1.0, barcodeDef = null) }
        setEffect { UnplannedAcceptanceContract.Effect.RequestFocusOnBarcode }
    }

    private fun onClickAssortmentBarcodeIcon(event: UnplannedAcceptanceContract.Event.OnClickAssortmentBarcodeIcon) {
        val req = GetAssortmentBarcodesByStockCodeUseCase.Request(event.stockCode)
        fetchAssortmentBarcodeJob = viewModelScope.launch {
            getAssortmentBarcodesByStockCodeUseCase(req).withLoading().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        setEffect { UnplannedAcceptanceContract.Effect.ShowLoading }
                    }

                    is Result.Success -> {
                        setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                        val bc = result.data.barcode.trim()
                        if (bc.isEmpty()) {
                            postGlobalError("Asorti barkodu bulunamadı")
                            return@collectLatest
                        }

                        if (changeAssortmentBarcodeConfirmJob?.isActive == true) return@collectLatest
                        val ok = askForConfirmation(
                            message = "Girilen barkod, asorti barkodu ile değiştirilecektir. İşlemi onaylıyor musunuz?",
                            title = "Onay",
                            positiveButtonText = "Evet",
                            negativeButtonText = "Hayır",
                            cancelable = false
                        )

                        if (ok) fetchBarcodeDefinition(bc, event.warehouseNumber)
                    }

                    is Result.Error -> {
                        setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    }
                }
            }

        }
    }

    private fun onLongTapStockTx(event: UnplannedAcceptanceContract.Event.OnLongTapStockTx) {
        if (deleteStockTxJob?.isActive == true) return
        deleteStockTxJob = viewModelScope.launch {
            val ok = askForDangerousConfirmation(
                title = "Dikkat",
                message = "Seçilen kayıt silinecektir.\n\n${event.stockTx.barcode} - ${event.stockTx.stockName}\n\n${event.stockTx.quantity} Adet",
                checkLabel = "Kaydı  kalıcı olarak silmeyi onaylıyorum.",
                positiveButtonText = "Evet",
                negativeButtonText = "Hayır",
                cancelable = false
            )
            if (ok) handleDeleteStockTx(event.stockTx)
        }.also { job ->
            job.invokeOnCompletion { deleteStockTxJob = null }
        }
    }

    private fun handleDeleteStockTx(stockTx: StockTransactionUiModel) {
        val req = DeleteStockTransactionUseCase.Request(stockTx.id)
        deleteStockTxUseCase(req).withLoading().onEach { result ->
            when (result) {
                is Result.Loading -> setEffect { UnplannedAcceptanceContract.Effect.ShowLoading }
                is Result.Success -> {
                    setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    postGlobalSuccessSuspending("Kayıt başarı ile silindi.")
                }

                is Result.Error -> {
                    setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    postGlobalErrorSuspending(result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun onInitialize(event: UnplannedAcceptanceContract.Event.OnInitialize) {
        val loggedUser = event.loggedUser
        val currentAccount = event.currentAccount

        if (loggedUser == null) {
            postGlobalError("Kullanıcı bilgilerine ulaşılamadı.\n\n Lütfen tekrar giriş yapınız.")
            setEffect { UnplannedAcceptanceContract.Effect.NavigateToMainMenu }
            return
        }

        if (currentAccount == null) {
            postGlobalError("Cari hesap bilgilerine ulaşılamadı. Lütfen cari hesap seçiniz.")
            setEffect { UnplannedAcceptanceContract.Effect.NavigateToSearchCompany }
            return
        }

        fetchNextStockTxDoc(loggedUser.documentSeries)
    }

    private fun fetchNextStockTxDoc(docSeries: String) {
        fetchNextDocSeriesJob?.cancel()
        val fetchReq = GetNextStockTransactionDocumentUseCase.Request(
            stockTransactionType = StockTransactionType.Input,
            stockTransactionKind = StockTransactionKind.Wholesale,
            isStockTransactionNormalOrReturn = 0,
            stockTransactionDocumentType = StockTransactionDocumentType.EntryDispatchNote,
            documentSeries = docSeries
        )
        fetchNextDocSeriesJob = viewModelScope.launch {
            val result = getNextStockTxDocUseCase(fetchReq).awaitTerminal()

            if (result is Result.Success) {
                val doc = result.data.stockTransactionDocument
                setEffect {
                    UnplannedAcceptanceContract.Effect.ShowDocumentDialog(doc.documentSeries, doc.documentNumber)
                }
            }
        }
    }

    private fun onConfirmDocumentDialog(event: UnplannedAcceptanceContract.Event.OnConfirmDocumentDialog) {
        val stockTxDoc = event.stockTxDoc

        if (stockTxDoc == null) {
            postGlobalError("Doküman bilgisine ulaşılamadı.")
            setEffect { UnplannedAcceptanceContract.Effect.NavigateToSearchCompany }
            return
        }

        val checkReq = CheckDocumentIsUsableUseCase.Request(stockTxDoc = stockTxDoc.toDomainModel(), currentCode = event.currentAccount.currentCode)

        checkStockTxDocJob?.cancel()
        checkStockTxDocJob = viewModelScope.launch {
            checkDocumentIsUsableUseCase(checkReq).withLoading().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        setEffect { UnplannedAcceptanceContract.Effect.ShowLoading }
                    }

                    is Result.Success -> {
                        setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                        val documentStatus = result.data.documentStatus

                        if (documentStatus.isUsed == true && documentStatus.canBeUsed == false) {
                            postGlobalErrorSuspending(documentStatus.message)
                            return@collectLatest
                        }

                        setEffect { UnplannedAcceptanceContract.Effect.DismissDocumentDialog }
                        setState { copy(stockTxDoc = stockTxDoc) }
                        fetchStockTxsByDoc(stockTxDoc)
                        setEffect { UnplannedAcceptanceContract.Effect.RequestFocusOnBarcode }

                    }

                    is Result.Error -> {
                        askForWarning(result.message, "Uyarı", "Tamam", false)
                        setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    }
                }

            }
        }


    }

    private fun fetchStockTxsByDoc(doc: StockTransactionDocumentUiModel) {
        fetchStockTxsByDocJob?.cancel()
        val fetchReq = GetStockTransactionsByDocumentUseCase.Request(
            documentSeries = doc.documentSeries,
            documentNumber = doc.documentNumber,
            transactionType = doc.transactionType,
            transactionKind = doc.transactionKind,
            isNormalOrReturn = doc.isNormalOrReturn,
            transactionDocumentType = doc.transactionDocumentType
        )

        fetchStockTxsByDocJob = viewModelScope.launch {
            getStockTxsByDocUseCase(fetchReq).withLoading().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        setEffect { UnplannedAcceptanceContract.Effect.ShowLoading }
                    }

                    is Result.Success -> {
                        setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                        val stockTxs = result.data.stockTransactions.toUiModel()
                        setState { copy(stockTransactions = stockTxs) }
                    }

                    is Result.Error -> {
                        postGlobalErrorSuspending(result.message)
                    }
                }
            }
        }
    }

    private fun onClickFinish(event: UnplannedAcceptanceContract.Event.OnClickFinish) {
        viewModelScope.launch {
            val ok = askForConfirmation(
                title = "Onay",
                message = "Depo transfer işlemi tamamlanacaktır.\n\nİşlemi onaylıyor musunuz?",
                positiveButtonText = "Evet",
                negativeButtonText = "Hayır"
            )

            if (!ok) return@launch
            handleFinishTransfer(event.stockDocTx, event.currentAccCode)
        }
    }

    private fun handleFinishTransfer(stockTxDoc: StockTransactionDocumentUiModel, currentAccCode: String) {
        if (finishStockTxJob?.isActive == true) return
        val addTransferredDoc = TransferredDocumentDomainModel(
            id = 0,
            transferredDocumentType = TransferredDocumentType.NormalPurchaseDispatch,
            documentSeries = stockTxDoc.documentSeries,
            documentNumber = stockTxDoc.documentNumber,
            currentCode = currentAccCode,
            paperNumber = stockTxDoc.paperNumber,
            synchronizationStatus = false,
            description = "Aktarılacak"
        )
        val finishReq = FinishStockTransactionUseCase.Request(stockTxDoc.toDomainModel(), addTransferredDoc)

        finishStockTxJob = finishStockTxUseCase(finishReq).withLoading().onEach { result ->
            when (result) {
                is Result.Loading -> setEffect { UnplannedAcceptanceContract.Effect.ShowLoading }
                is Result.Success -> {
                    setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    postGlobalSuccessSuspending("İşlem başarılı.")
                    setEffect { UnplannedAcceptanceContract.Effect.NavigateToMainMenu }
                }

                is Result.Error -> {
                    setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    postGlobalErrorSuspending(result.message)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun onClickExit(event: UnplannedAcceptanceContract.Event.OnClickExit) {
        if (exitConfirmJob?.isActive == true) return
        exitConfirmJob = viewModelScope.launch {
            val ok = askForDangerousConfirmation(
                title = "Dikkat",
                message = "Mal kabul evrağından çıkmaya çalışıyorsunuz.\n\nÇıkış yaptığınızda evrak silinecektir ve tekrar kurtarılamayacaktır.\nBu sayfadan çıkmak istediğinize emin misiniz?",
                checkLabel = "Evrağı kalıcı olarak silmeyi onaylıyorum.",
                positiveButtonText = "Evet",
                negativeButtonText = "Hayır",
                cancelable = false
            )

            if (ok) handleUnplannedAcceptanceCancel(event.stockDocTx)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleUnplannedAcceptanceCancel(stockTxDoc: StockTransactionDocumentUiModel) {
        val removeTransferredDocReq = RemoveTransferredDocumentUseCase.Request(
            documentSeries = stockTxDoc.documentSeries,
            documentNumber = stockTxDoc.documentNumber,
            transferredDocumentType = TransferredDocumentType.NormalPurchaseDispatch
        )

        val removeStockTxReq = RemoveStockTransactionUseCase.Request(
            documentSeries = stockTxDoc.documentSeries,
            documentNumber = stockTxDoc.documentNumber,
            transactionType = stockTxDoc.transactionType,
            transactionKind = stockTxDoc.transactionKind,
            isNormalOrReturn = stockTxDoc.isNormalOrReturn,
            transactionDocumentType = stockTxDoc.transactionDocumentType
        )

        removeTransferredDocumentUseCase(removeTransferredDocReq).flatMapConcat {
            removeStockTransactionUseCase(removeStockTxReq)
        }.onEach { result ->
            when (result) {
                is Result.Loading -> setEffect { UnplannedAcceptanceContract.Effect.ShowLoading }

                is Result.Success -> {
                    setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    postGlobalErrorSuspending("Belge başarı ile silindi.")
                    setEffect { UnplannedAcceptanceContract.Effect.NavigateToSearchCompany }
                }

                is Result.Error -> {
                    setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    postGlobalError(result.message)
                    setEffect { UnplannedAcceptanceContract.Effect.NavigateToMainMenu }
                }
            }
        }.launchIn(viewModelScope)

    }

    private fun onBarcodeEntered(event: UnplannedAcceptanceContract.Event.OnBarcodeEntered) {
        val barcode = event.barcode.trim()
        if (barcode.isEmpty()) {
            postGlobalError("Barkod alanı boş bırakılamaz.")
            setEffect { UnplannedAcceptanceContract.Effect.RequestFocusOnBarcode }
            return
        }
        fetchBarcodeDefinition(barcode, event.warehouseNumber)
    }

    private fun fetchBarcodeDefinition(
        barcode: String, warehouseNumber: Int
    ) {
        barcodeJob?.cancel()
        barcodeJob = viewModelScope.launch {
            val fetchBarcodeDefReq = GetBarcodeDefinitionByBarcodeUseCase.Request(barcode, warehouseNumber)
            getBarcodeDefinitionByBarcodeUseCase(fetchBarcodeDefReq).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        setEffect { UnplannedAcceptanceContract.Effect.ShowLoading }
                    }

                    is Result.Success -> {
                        setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                        val barcodeDef = result.data.barcodeDefinition.toUiModel()
                        setState { copy(barcodeDef = barcodeDef, quantity = 1.0) }
                        if (currentState.singleQuantityStatus && currentState.stockTxDoc != null) {
                            setEffect { UnplannedAcceptanceContract.Effect.TriggerAutoSave }
                            return@collectLatest
                        }
                        setEffect { UnplannedAcceptanceContract.Effect.RequestFocusOnQuantity }
                    }

                    is Result.Error -> {
                        postGlobalErrorSuspending(result.message)
                        setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                        setEffect { UnplannedAcceptanceContract.Effect.RequestFocusOnBarcode }
                    }
                }
            }
        }
    }

    private fun onSaveAcceptance(event: UnplannedAcceptanceContract.Event.OnSaveAcceptance) {
        if(saveJob?.isActive == true) return
        val stockTxDoc = event.stockTxDoc
        if (event.stockTxDoc == null) {
            postGlobalError("Stok hareketleri bilgisi bulunamadı. Bu sebeple işleme devam edilemez.")
            return
        }

        val barcodeDef = event.barcodeDef
        if (event.barcodeDef == null) {
            postGlobalError("Barkod bilgisi bulunamadı. Bu sebeple işleme devam edilemez.")
            return
        }

        val user = event.user
        if (event.user == null) {
            postGlobalError("Kullanıcı bilgisi bulunamadı. Bu sebeple işleme devam edilemez.")
            return
        }

        setEffect { UnplannedAcceptanceContract.Effect.ShowLoading }

        val unit = event.unit
        val baseQuantity = event.quantity
        val currentCode = event.currentAccCode

        saveJob = viewModelScope.launch {
            val (adjustedQty, totalQty) = computeTotalQuantity(unit, barcodeDef, baseQuantity)

            val priceReq = GetStockBuyingConditionUseCase.Request(
                currentCode = currentCode,
                stockCode = barcodeDef.stockCode,
                date = DateConverter.uiToTimestamp(stockTxDoc.documentDate),
                warehouseNumber = user.warehouseNumber
            )

            when (val priceRes = getStockBuyingConditionUseCase(priceReq).awaitTerminal()) {
                is Result.Success -> {
                    val unitPrice = priceRes.data.stockBuyingCondition.grossPrice
                    val totalPrice = unitPrice * totalQty
                    val stockTx = buildStockTransaction(
                        stockTransactionDocument = stockTxDoc,
                        barcodeDefinition = barcodeDef,
                        totalQty = totalQty,
                        loggedUser = user,
                        currentAccCode = currentCode,
                        totalPrice = totalPrice,
                        unitPrice = unitPrice
                    )

                    val sizeTx = createSizeTransactionsIfExist(barcodeDef, adjustedQty)

                    val addReq = AddStockTransactionUseCase.Request(stockTx, sizeTx)

                    when (val addRes = addStockTxUseCase(addReq).awaitTerminal()) {
                        is Result.Success -> {
                            setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                            postGlobalSuccessSuspending("Kayıt işlemi başarılı.")
                            setEffect { UnplannedAcceptanceContract.Effect.RequestFocusOnBarcode }
                            setState { copy(barcodeDef = null, quantity = 1.0) }
                        }

                        is Result.Error -> {
                            setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                            postGlobalErrorSuspending(addRes.message)
                        }

                        is Result.Loading -> Unit
                    }
                }

                is Result.Error -> {
                    setEffect { UnplannedAcceptanceContract.Effect.DismissLoading }
                    postGlobalErrorSuspending(priceRes.message)
                }

                is Result.Loading -> Unit
            }
        }

    }

    private fun onQuantityChanged(event: UnplannedAcceptanceContract.Event.OnQuantityChanged) {
        val qty = event.quantity
        setState { copy(quantity = qty) }
    }

    private fun computeTotalQuantity(selectedUnit: String, barcodeDef: BarcodeDefinitionUiModel, baseQuantity: Double): Pair<Double, Double> {
        val unit = TransferUnit.fromDisplay(selectedUnit)
        val coef = coefFor(unit, barcodeDef)
        val adjustedQuantity = baseQuantity * coef

        val sizeFactor = if (barcodeDef.connectionType == 2.toByte()) {
            val sum = ((barcodeDef.sizeBarcodes) ?: emptyList()).sumOf { it.quantity }
            sum.takeIf { it > 0.0 } ?: 1.0
        } else 1.0
        val totalQty = adjustedQuantity * sizeFactor
        return adjustedQuantity to totalQty

    }

    private fun coefFor(unit: TransferUnit, barcodeDef: BarcodeDefinitionUiModel): Double = when (unit) {
        TransferUnit.Adet -> 1.0
        TransferUnit.Paket -> barcodeDef.unit2Coefficient.takeUnless { it.isNullOrZero() } ?: 1.0
        TransferUnit.Koli -> barcodeDef.unit3Coefficient.takeUnless { it.isNullOrZero() } ?: 1.0
    }

    private fun buildStockTransaction(
        stockTransactionDocument: StockTransactionDocumentUiModel,
        barcodeDefinition: BarcodeDefinitionUiModel,
        totalQty: Double,
        loggedUser: UserUiModel,
        currentAccCode: String,
        totalPrice: Double,
        unitPrice: Double
    ): AddStockTransactionDomainModel {
        val stockTransaction = AddStockTransactionDomainModel(
            transactionType = stockTransactionDocument.transactionType,
            transactionKind = stockTransactionDocument.transactionKind,
            isNormalOrReturn = 0,
            transactionDocumentType = stockTransactionDocument.transactionDocumentType,
            documentDate = DateConverter.uiToTimestamp(stockTransactionDocument.documentDate),
            documentSeries = stockTransactionDocument.documentSeries,
            documentNumber = stockTransactionDocument.documentNumber,
            lineNumber = 0,
            stockCode = barcodeDefinition.stockCode,
            stockName = barcodeDefinition.stockName,
            currentCode = currentAccCode,
            quantity = totalQty,
            inputWarehouseNumber = loggedUser.warehouseNumber,
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
}