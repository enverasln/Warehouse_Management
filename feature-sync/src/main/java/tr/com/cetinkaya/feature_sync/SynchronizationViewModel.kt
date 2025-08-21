package tr.com.cetinkaya.feature_sync

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.domain.usecase.order.GetUnsyncedOrderUseCase
import tr.com.cetinkaya.domain.usecase.order.TransferOrdersUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.GetUnsyncedStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.stock_transaction.TransferStockTransactionsUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.GetUntransferredDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.SyncAllDocumentsUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_sync.models.toUiModel
import javax.inject.Inject

@HiltViewModel
class SynchronizationViewModel @Inject constructor(
    private val getUnsyncedStockTransactionsUseCase: GetUnsyncedStockTransactionsUseCase,
    private val transferStockTransactionsUseCase: TransferStockTransactionsUseCase,
    private val getUnsyncedOrderUseCase: GetUnsyncedOrderUseCase,
    private val getUntransferredDocumentsUseCase: GetUntransferredDocumentsUseCase,
    private val syncOrdersUseCase: TransferOrdersUseCase,
    private val syncAllDocumentsUseCase: SyncAllDocumentsUseCase
) : BaseViewModel<SynchronizationContract.Event, SynchronizationContract.State, SynchronizationContract.Effect>() {


    override fun createInitialState(): SynchronizationContract.State = SynchronizationContract.State()


    override fun handleEvent(event: SynchronizationContract.Event) {
        when (event) {
            is SynchronizationContract.Event.OnStartSynchronization -> {
                viewModelScope.launch {

                    syncAllDocumentsUseCase(SyncAllDocumentsUseCase.Request()).collect()

                }

//                viewModelScope.launch {
//                    launch {
//                        getUnsyncedStockTransactionsUseCase(GetUnsyncedStockTransactionsUseCase.Request).collect { result ->
//                            when (result) {
//                                is Result.Success -> {
//                                    val stockTransactions = result.data.items
//
//                                    transferStockTransactionsUseCase(TransferStockTransactionsUseCase.Request(stockTransactions)).collect { transferResult ->
//                                        when (transferResult) {
//                                            is Result.Success -> {
//                                                setState {
//                                                    copy(messages = messages + transferResult.data.message)
//                                                }
//                                            }
//
//                                            is Result.Error -> {
//                                                setState {
//                                                    copy(messages = messages + transferResult.message)
//                                                }
//                                            }
//
//                                            else -> {}
//
//                                        }
//                                    }
//
//
//                                    stockTransactions.forEach {
//                                        Log.d("SynchronizationViewModel", "handleEvent: ${it.toString()}")
//                                    }
//
//
//                                }
//
//                                else -> {}
//
//                            }
//                        }
//                    }
//
//                    launch {
//                        getUnsyncedOrderUseCase(GetUnsyncedOrderUseCase.Request).collect { result ->
//                            when (result) {
//                                is Result.Success -> {
//                                    val orders = result.data.items
//                                    syncOrdersUseCase(TransferOrdersUseCase.Request(orders)).collect { transferResult ->
//                                        when (transferResult) {
//                                            is Result.Success -> {
//                                                setState {
//                                                    copy(messages = messages + transferResult.data.message)
//                                                }
//                                            }
//
//                                            is Result.Error -> {
//                                                setState {
//                                                    copy(messages = messages + transferResult.message)
//                                                }
//                                            }
//
//                                            else -> {}
//
//                                        }
//                                    }
//                                }
//
//                                else -> {}
//                            }
//
//                        }
//                    }
//                }
            }

            is SynchronizationContract.Event.OnFetchTransferredDocuments -> {
                viewModelScope.launch {
                    getUntransferredDocumentsUseCase(GetUntransferredDocumentsUseCase.Request).collect { result ->
                        when (result) {
                            is Result.Success -> {
                                setState {
                                    copy(documents = result.data.untransferredDocuments.map { it.toUiModel() })
                                }
                            }

                            else -> {}
                        }

                    }
                }


            }
        }
    }
}


