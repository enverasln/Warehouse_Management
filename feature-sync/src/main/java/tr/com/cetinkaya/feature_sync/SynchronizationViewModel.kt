package tr.com.cetinkaya.feature_sync

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.flow.withLoading
import tr.com.cetinkaya.domain.usecase.transferred_document.GetUntransferredDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.SyncAllDocumentsUseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.SyncProgress
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import tr.com.cetinkaya.feature_common.dialog.global_dialog.DialogRequestRegistry
import tr.com.cetinkaya.feature_sync.models.toUiModel
import javax.inject.Inject

@HiltViewModel
class SynchronizationViewModel @Inject constructor(
    private val getUntransferredDocumentsUseCase: GetUntransferredDocumentsUseCase,
    private val syncAllDocumentsUseCase: SyncAllDocumentsUseCase,
    appEventBus: AppEventBus,
    dialogRegister: DialogRequestRegistry
) : BaseViewModel<SynchronizationContract.Event, SynchronizationContract.State, SynchronizationContract.Effect>(appEventBus, dialogRegister) {

    override fun createInitialState(): SynchronizationContract.State = SynchronizationContract.State()

    override fun handleEvent(event: SynchronizationContract.Event) {
        when (event) {
            is SynchronizationContract.Event.OnStartSynchronization -> {
                viewModelScope.launch {
                    syncAllDocumentsUseCase(SyncAllDocumentsUseCase.Request()).withLoading().collect { result ->
                        when (result) {
                            is Result.Error -> {
                            }

                            is Result.Success -> {
                                when (val syncProgress = result.data.syncProgression) {
                                    is SyncProgress.Started -> setEffect { SynchronizationContract.Effect.ShowLoading }

                                    is SyncProgress.Completed -> setEffect { SynchronizationContract.Effect.DismissLoading }
                                    is SyncProgress.Error -> postGlobalSuccess(syncProgress.error)
                                    is SyncProgress.InProgress -> {}
                                }

                            }

                            is Result.Loading -> {
                                setEffect { SynchronizationContract.Effect.ShowLoading }
                            }
                        }
                    }
                }
            }

            is SynchronizationContract.Event.OnFetchTransferredDocuments -> {
                viewModelScope.launch {
                    getUntransferredDocumentsUseCase(GetUntransferredDocumentsUseCase.Request).withLoading().collect { result ->
                        when (result) {
                            is Result.Success -> {
                                setState {
                                    copy(documents = result.data.untransferredDocuments.map { it.toUiModel() })
                                }
                            }

                            is Result.Error -> {

                            }

                            is Result.Loading -> {

                            }
                        }

                    }
                }


            }
        }
    }
}


