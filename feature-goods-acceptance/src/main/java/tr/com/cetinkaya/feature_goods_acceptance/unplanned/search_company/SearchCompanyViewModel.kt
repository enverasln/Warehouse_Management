package tr.com.cetinkaya.feature_goods_acceptance.unplanned.search_company

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.flow.awaitResult
import tr.com.cetinkaya.common.flow.withLoading
import tr.com.cetinkaya.domain.usecase.auth.GetLoggedUserUseCase
import tr.com.cetinkaya.domain.usecase.current_account.GetCurrentAccountByTitleUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_common.app_effect.AppEventBus
import tr.com.cetinkaya.feature_common.dialog.global_dialog.DialogRequestRegistry
import tr.com.cetinkaya.feature_goods_acceptance.models.user.toUiModel
import tr.com.cetinkaya.feature_goods_acceptance.unplanned.model.current_account.toUiModel
import javax.inject.Inject

@HiltViewModel
class SearchCompanyViewModel @Inject constructor(
    private val getCurrentAccountByTitleUseCase: GetCurrentAccountByTitleUseCase,
    private val getLoggedUserUseCase: GetLoggedUserUseCase,
    appEventBus: AppEventBus,
    dialogRegister: DialogRequestRegistry,
) : BaseViewModel<Event, State, Effect>(appEventBus, dialogRegister) {

    private var doSearchJob: Job? = null
    private var doFetchLoggedUser: Job? = null

    override fun createInitialState(): State = State()

    init {
        fetchLoggedUser()
    }


    override fun handleEvent(event: Event) {
        when (event) {
            is Event.OnClickSearchButton -> {
                val fetchReq = GetCurrentAccountByTitleUseCase.Request(event.title)
                doSearchJob?.cancel()
                doSearchJob = viewModelScope.launch {
                    getCurrentAccountByTitleUseCase(fetchReq).withLoading().collect { result ->
                        when (result) {
                            is Result.Loading -> {}

                            is Result.Success -> {
                                val currentAccounts = result.data.currentAccounts
                                val mappedCurrentAccounts = currentAccounts.toUiModel()
                                setState { copy(currentAccounts = mappedCurrentAccounts) }
                            }

                            is Result.Error -> {
                                askForWarning(
                                    title = "Dikkat", message =  result.message, cancelable = false)
                            }
                        }

                    }

                }
            }

            is Event.OnSelectCurrentAccount -> {
                val selectedCurrentAccount = event.currentAccount.copy(isSelected = !event.currentAccount.isSelected)
                val currentAccounts = currentState.currentAccounts
                val updatedCurrentAccounts =
                    currentAccounts.map { if (it.currentCode == event.currentAccount.currentCode) selectedCurrentAccount else it.copy(isSelected = false) }

                if (selectedCurrentAccount.isSelected) {
                    setState { copy(currentAccounts = updatedCurrentAccounts, selectedCurrentAccount = selectedCurrentAccount) }
                } else {
                    setState { copy(currentAccounts = updatedCurrentAccounts, selectedCurrentAccount = null) }
                }

            }

            is Event.OnClickStartGoodsAcceptanceButton -> {
                if(event.loggedUser ==  null) {
                    postGlobalError("Kullanıcı bilgilerine ulaşılamadı.\n\n Lütfen uygulamayı kapatıp yeniden başlatınız.")
                    return
                }

                if(event.currentAccount == null) {
                    postGlobalError("Devam edebilmek için lütfen bir cari hesap seçiniz.")
                    return
                }

                setEffect { Effect.NavigateToUnplannedAcceptance(event.loggedUser, event.currentAccount) }
            }
        }
    }

    private fun fetchLoggedUser() {
        doFetchLoggedUser?.cancel()
        val fetchReq = GetLoggedUserUseCase.Request
        doFetchLoggedUser = viewModelScope.launch {
            val result = getLoggedUserUseCase(fetchReq).withLoading().awaitResult()
            if (result is Result.Success) {
                val loggedUser = result.data.user.toUiModel()
                setState { copy(loggedUser = loggedUser) }
            }

            if (result is Result.Error) {
                postGlobalErrorSuspending(result.message)
            }

        }
    }
}