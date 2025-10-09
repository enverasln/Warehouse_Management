package tr.com.cetinkaya.feature_goods_acceptance.unplanned.search_company

import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState
import tr.com.cetinkaya.feature_goods_acceptance.models.user.UserUiModel
import tr.com.cetinkaya.feature_goods_acceptance.unplanned.model.current_account.CurrentAccountUiModel

sealed class Event : UiEvent {
    data class OnClickSearchButton(val title: String) : Event()
    data class OnSelectCurrentAccount(val currentAccount: CurrentAccountUiModel) : Event()

    data class OnClickStartGoodsAcceptanceButton(val loggedUser: UserUiModel?, val currentAccount: CurrentAccountUiModel?) : Event()

}

data class State(
    val loggedUser: UserUiModel? = null,
    val currentAccounts: List<CurrentAccountUiModel> = emptyList(),
    val selectedCurrentAccount: CurrentAccountUiModel? = null
) : UiState

sealed class Effect : UiEffect {
    data class NavigateToUnplannedAcceptance(val loggedUser: UserUiModel, val currentAccount: CurrentAccountUiModel) : Effect()
}

