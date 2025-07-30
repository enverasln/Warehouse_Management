package tr.com.cetinkaya.feature_goods_transfer

import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_transfer.models.UserUiModel

class GoodsTransferOperationsContracts {
    sealed class Event: UiEvent {
        data object OnWarehouseGoodsTransferButtonClick : Event()

    }

    data class State(
        val loggedUser: UserUiModel? = null
    ) : UiState

    sealed class Effect : UiEffect {
        data object NavigateToWarehouseGoodsTransfer : Effect()
    }
}