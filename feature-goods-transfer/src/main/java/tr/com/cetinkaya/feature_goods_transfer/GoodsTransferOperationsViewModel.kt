package tr.com.cetinkaya.feature_goods_transfer

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.domain.usecase.auth.GetLoggedUserUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_goods_transfer.models.toUiModel
import javax.inject.Inject

@HiltViewModel
class GoodsTransferOperationsViewModel @Inject constructor(
    private val getLoggedUserUseCase: GetLoggedUserUseCase,
) : BaseViewModel<GoodsTransferOperationsContracts.Event, GoodsTransferOperationsContracts.State, GoodsTransferOperationsContracts.Effect>() {

    override fun createInitialState(): GoodsTransferOperationsContracts.State = GoodsTransferOperationsContracts.State()

    init {
        viewModelScope.launch {
            getLoggedUserUseCase(GetLoggedUserUseCase.Request).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {}
                    is Result.Success -> {
                        setState { copy(loggedUser = result.data.user.toUiModel()) }
                    }

                    is Result.Error -> {}
                }
            }
        }
    }

    override fun handleEvent(event: GoodsTransferOperationsContracts.Event) {

        when (event) {
            is GoodsTransferOperationsContracts.Event.OnWarehouseGoodsTransferButtonClick -> {
                setEffect { GoodsTransferOperationsContracts.Effect.NavigateToWarehouseGoodsTransfer }
            }
        }

    }
}



