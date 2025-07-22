package tr.com.cetinkaya.feature_auth.login

import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.feature_common.UiEffect
import tr.com.cetinkaya.feature_common.UiEvent
import tr.com.cetinkaya.feature_common.UiState

sealed class Event : UiEvent {
    data class OnLoginButtonClick(val usernameOrEmail: String, val password: String) : Event()
}

data class State(
    val loginUiState: LoginUiState
) : UiState

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data class Success(val loggedUser: UserDomainModel) : LoginUiState()
    data object Loading : LoginUiState()
}

sealed class Effect : UiEffect {
    data class ShowError(val message: String) : Effect()
}
