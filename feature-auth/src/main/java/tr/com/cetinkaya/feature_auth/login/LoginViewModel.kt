package tr.com.cetinkaya.feature_auth.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.domain.usecase.auth.LoginUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : BaseViewModel<Event, State, Effect>() {


    override fun createInitialState(): State {
        return State(loginUiState = LoginUiState.Idle)
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.OnLoginButtonClick -> {
                val (usernameOrEmail, password) = event
                login(usernameOrEmail, password)
            }
        }
    }


    private fun login(usernameOrEmail: String, password: String) {
        viewModelScope.launch {
            val request = LoginUseCase.Request(usernameOrEmail = usernameOrEmail, password = password)
            loginUseCase(request).onStart { emit(Result.Loading) }.collect { result ->
                    when (result) {
                        is Result.Loading -> setState { copy(loginUiState = LoginUiState.Loading) }

                        is Result.Success -> {
                            val user = result.data.loggedUser
                            setState { copy(loginUiState = LoginUiState.Success(loggedUser = user)) }
                        }

                        is Result.Error -> {
                            setEffect { Effect.ShowError(result.message) }
                            setState { copy(loginUiState = LoginUiState.Idle) }
                        }
                    }
                }
        }
    }

}



