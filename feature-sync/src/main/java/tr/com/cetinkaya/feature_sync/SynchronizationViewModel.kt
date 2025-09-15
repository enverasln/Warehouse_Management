package tr.com.cetinkaya.feature_sync

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.domain.usecase.transferred_document.GetUntransferredDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.SyncAllDocumentsUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_sync.models.toUiModel
import javax.inject.Inject

@HiltViewModel
class SynchronizationViewModel @Inject constructor(
    private val getUntransferredDocumentsUseCase: GetUntransferredDocumentsUseCase,
    private val syncAllDocumentsUseCase: SyncAllDocumentsUseCase,
) : BaseViewModel<SynchronizationContract.Event, SynchronizationContract.State, SynchronizationContract.Effect>() {

    override fun createInitialState(): SynchronizationContract.State = SynchronizationContract.State()

    override fun handleEvent(event: SynchronizationContract.Event) {
        when (event) {
            is SynchronizationContract.Event.OnStartSynchronization -> {
                viewModelScope.launch {
                    syncAllDocumentsUseCase(SyncAllDocumentsUseCase.Request()).collect { result ->
                        when (result) {
                            is Result.Error -> {}
                            is Result.Success -> {
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

                            is Result.Loading -> {}
                        }
                    }
                }
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


