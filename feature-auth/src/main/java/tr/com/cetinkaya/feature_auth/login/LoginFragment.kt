package tr.com.cetinkaya.feature_auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.feature_auth.R
import tr.com.cetinkaya.feature_auth.databinding.FragmentLoginBinding
import tr.com.cetinkaya.feature_common.BaseFragment

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoginBinding
        get() = FragmentLoginBinding::inflate

    private val viewModel: LoginViewModel by viewModels()

    override fun prepareView(savedInstanceState: Bundle?) {
        binding.apply {
            btnLogin.setOnClickListener {
                val usernameOrEmail = etUsernameOrEmail.text.toString()
                val password = etPassword.text.toString()
                viewModel.setEvent(Event.OnLoginButtonClick(usernameOrEmail, password))
            }
        }
    }


    override fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (val loginUiState = state.loginUiState) {
                        is LoginUiState.Loading -> showLoading(true)
                        is LoginUiState.Success -> {
                            showLoading(false)
                            navigateToHome(loginUiState.loggedUser)
                        }

                        is LoginUiState.Idle -> showLoading(false)
                    }
                }
            }
        }
    }

    override fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is Effect.ShowError -> showToast(effect.message)
                    }
                }
            }
        }
    }


    private fun showLoading(show: Boolean) {
        binding.loadingContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome(user: UserDomainModel) {
        findNavController().navigate(R.id.action_loginFragment_to_home_graph)
    }


}