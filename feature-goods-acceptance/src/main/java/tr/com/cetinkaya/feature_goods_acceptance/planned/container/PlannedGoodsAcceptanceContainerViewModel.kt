package tr.com.cetinkaya.feature_goods_acceptance.planned.container

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.common.flow.awaitResult
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.usecase.order_transaction.GetNextOrderTransactionDocumentUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.FetchAndSaveOrderTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.FinishOrderTransactionUseCase
import tr.com.cetinkaya.domain.usecase.order_transaction.GetOrderTxsByDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.CheckDocumentIsUsableUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetStockTransactionDocumentByDocumentNumberUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.PlannedGoodsAcceptanceContainerContract.Effect
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order_transaction.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.toDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel
import javax.inject.Inject

@HiltViewModel
class PlannedGoodsAcceptanceContainerViewModel @Inject constructor(
    private val syncPlannedGoodsAcceptanceProductsUseCase: FetchAndSaveOrderTransactionsUseCase,
    private val checkDocumentIsUsableUseCase: CheckDocumentIsUsableUseCase,
    private val finishOrderTransactionUseCase: FinishOrderTransactionUseCase,
    private val getStockTransactionDocumentByDocumentNumberUseCase: GetStockTransactionDocumentByDocumentNumberUseCase,
    private val getOrderTxsByDocumentsUseCase: GetOrderTxsByDocumentsUseCase,
    private val getNextOrderTxDocUseCase: GetNextOrderTransactionDocumentUseCase,
) : BaseViewModel<PlannedGoodsAcceptanceContainerContract.Event, PlannedGoodsAcceptanceContainerContract.State, Effect>() {
    private var orderTxsJob: Job? = null
    private var docLookupJob: Job? = null

    override fun createInitialState(): PlannedGoodsAcceptanceContainerContract.State = PlannedGoodsAcceptanceContainerContract.State()

    override fun handleEvent(event: PlannedGoodsAcceptanceContainerContract.Event) {
        when (event) {
            is PlannedGoodsAcceptanceContainerContract.Event.Initialize -> onInitialize(event.loggedUser, event.selectedDocuments)

            is PlannedGoodsAcceptanceContainerContract.Event.OnStateReset -> {
                cancelActiveJobs()
                setState { createInitialState() }
            }

            is PlannedGoodsAcceptanceContainerContract.Event.OnDocumentDialogConfirmed -> onDocumentDialogConfirmed(event.stockTransactionDocument)

            is PlannedGoodsAcceptanceContainerContract.Event.OnFinishAcceptance -> {
                if (currentState.isFinishingAcceptance) return
                setState { copy(isFinishingAcceptance = true) }
                onFinishAcceptance()
            }

            is PlannedGoodsAcceptanceContainerContract.Event.OnListItemDoubleTab -> {
                setState { copy(currentTabIndex = 0, tappedBarcode = event.orderTx.barcode) }
            }

            is PlannedGoodsAcceptanceContainerContract.Event.TabChanged -> setState { copy(currentTabIndex = event.index) }

            is PlannedGoodsAcceptanceContainerContract.Event.OnDocumentNumberChanged -> {
                startStockTxDocLookup(documentSeries = event.documentSeries, documentNumber = event.documentNumber)
            }
        }
    }

    // region Initialize & Dialog
    private fun onInitialize(loggedUser: UserUiModel?, selectedDocuments: List<DocumentUiModel>) {
        val firstDocument = selectedDocuments.firstOrNull() ?: return
        setState {
            copy(
                loggedUser = loggedUser,
                selectedDocuments = selectedDocuments,
                companyName = firstDocument.companyName,
                companyCode = firstDocument.companyCode
            )
        }
        setEffect { Effect.ShowDocumentDialog }
    }

    private fun onDocumentDialogConfirmed(stockTransactionDocument: StockTransactionDocumentUiModel) {
        setStockTransactionDocument(stockTransactionDocument)

        viewModelScope.launch {
            // Snapshot al: suspend aralarında bayat state kullanmayalım
            val snapshot = currentState
            val mappedStockTxDoc = stockTransactionDocument.toDomainModel()
            val currentCode = snapshot.selectedDocuments.firstOrNull()?.companyCode ?: return@launch

            // 1) Evrak kullanılabilir mi?
            when (val checkRes = checkDocumentIsUsableUseCase(
                CheckDocumentIsUsableUseCase.Request(mappedStockTxDoc, currentCode)
            ).awaitResult()) {
                is Result.Error -> {
                    setEffect { Effect.ShowError(checkRes.message) }
                    return@launch
                }

                is Result.Success -> {
                    if (checkRes.data.documentStatus.isDocumentNew) {
                        setEffect { Effect.DismissDialog }
                    } else {
                        setEffect { Effect.ShowConfirmationDialog(checkRes.data.documentStatus.message) }
                    }
                }

                else -> Unit
            }

            // 2) Bir sonraki OrderTx doc no
            val loggedUser = snapshot.loggedUser ?: return@launch
            when (val nextDocRes = getNextOrderTxDocUseCase(
                GetNextOrderTransactionDocumentUseCase.Request(
                    txType = OrderTransactionTypes.Supply, txKind = OrderTransactionKinds.NormalOrder, docSeries = loggedUser.newDocumentSeries
                )
            ).awaitResult()) {
                is Result.Success -> setState { copy(nextOrderTxDoc = nextDocRes.data.nextDocument) }
                is Result.Error -> setEffect { Effect.ShowError(nextDocRes.message) }
                else -> Unit
            }

            // 3) Seçilen belgeleri senkronize et
            val pairs = snapshot.selectedDocuments.map { it.documentSeries to it.documentNumber }
            when (val syncRes = syncPlannedGoodsAcceptanceProductsUseCase(
                FetchAndSaveOrderTransactionsUseCase.Request(pairs, loggedUser.warehouseNumber)
            ).awaitResult()) {
                is Result.Success<*> -> setEffect { Effect.ShowSnackbar("Sipariş başarıyla senkronize edildi") }
                is Result.Error -> {
                    setEffect { Effect.ShowError("Sipariş senkronizasyonu başarısız: ${syncRes.message}") }
                    return@launch
                }

                else -> Unit
            }

            // 4) Listeyi izle – tek abonelik
            subscribeOrderTxs(GetOrderTxsByDocumentsUseCase.Request(pairs, loggedUser.warehouseNumber))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startStockTxDocLookup(documentSeries: String, documentNumber: Int) {
        // Önce varsa eski job’ı iptal et
        docLookupJob?.cancel()

        val request = GetStockTransactionDocumentByDocumentNumberUseCase.Request(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            transactionType = StockTransactionType.Input,
            transactionKind = StockTransactionKind.Wholesale,
            isNormalOrReturn = 0,
            transactionDocumentType = StockTransactionDocumentType.EntryDispatchNote
        )

        // Kaynak akış
        val source = getStockTransactionDocumentByDocumentNumberUseCase(request)

        docLookupJob = source
            .map { result ->
                when (result) {
                    is Result.Success -> {
                        val doc = result.data.document
                        doc?.let { it.invoiceId to it.paperNumber } // key: Pair<String, String?>
                    }
                    else -> null
                }
            }
            .distinctUntilChanged()
            .flatMapLatest {
                getStockTransactionDocumentByDocumentNumberUseCase(request)
            }
            .onEach { result ->
                when (result) {
                    is Result.Loading -> Unit

                    is Result.Success -> {
                        val doc = result.data.document
                        if (doc == null) {
                            setEffect { Effect.SetDialogPaperNumber() }
                            return@onEach
                        }

                        if (doc.invoiceId != "00000000-0000-0000-0000-000000000000") {
                            setEffect {
                                Effect.SetDialogBlockingError(
                                    "Belge faturalaştırıldığı için devam edilemez.\nLütfen evrak no sırasını değiştiriniz."
                                )
                            }

                        }

                        setEffect {
                            Effect.SetDialogPaperNumber(doc.paperNumber)
                        }
                    }

                    is Result.Error -> {
                        setEffect { Effect.ShowError(result.message) }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // endregion

    // region Finish
    private fun onFinishAcceptance() {
        val nextOrderTxDoc = currentState.nextOrderTxDoc ?: return
        val stockTxDoc = currentState.stockTransactionDocument?.toDomainModel() ?: return

        val transferredDocs = listOf(
            TransferredDocumentDomainModel(
                id = 0,
                transferredDocumentType = TransferredDocumentType.NormalGivenOrder,
                documentSeries = nextOrderTxDoc.docSeries,
                documentNumber = nextOrderTxDoc.docNumber,
                currentCode = currentState.companyCode,
                paperNumber = "",
                synchronizationStatus = false,
                description = SyncStatus.PendingTransfer.description
            ), TransferredDocumentDomainModel(
                id = 0,
                transferredDocumentType = TransferredDocumentType.NormalPurchaseDispatch,
                documentSeries = stockTxDoc.documentSeries,
                documentNumber = stockTxDoc.documentNumber,
                currentCode = currentState.companyCode,
                paperNumber = stockTxDoc.paperNumber,
                synchronizationStatus = false,
                description = SyncStatus.PendingTransfer.description
            )
        )

        val finishReq = FinishOrderTransactionUseCase.Request(nextOrderTxDoc, stockTxDoc, transferredDocs)

        viewModelScope.launch {
            when (val res = finishOrderTransactionUseCase(finishReq).awaitResult()) {
                is Result.Success -> setEffect { Effect.CloseAcceptance }
                is Result.Error -> setEffect { Effect.ShowError(res.message) }
                else -> Unit
            }
            setState { copy(isFinishingAcceptance = false) }
        }
    }

    // endregion

    // region Helpers
    private fun subscribeOrderTxs(fetchReq: GetOrderTxsByDocumentsUseCase.Request) {
        orderTxsJob?.cancel()
        orderTxsJob = getOrderTxsByDocumentsUseCase(fetchReq).onEach { res ->
            when (res) {
                is Result.Success -> setState { copy(orderTxs = res.data.orderTxs.toUiModel()) }
                is Result.Error -> setEffect { Effect.ShowError(res.message) }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun setStockTransactionDocument(stockTransactionDocument: StockTransactionDocumentUiModel) {
        setState { copy(stockTransactionDocument = stockTransactionDocument) }
    }

    private fun cancelActiveJobs() {
        orderTxsJob?.cancel()
        orderTxsJob = null
        docLookupJob?.cancel()
        docLookupJob = null
    }

    // endregion
}