package tr.com.cetinkaya.feature_goods_acceptance.unplanned.search_company

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_goods_acceptance.databinding.FragmentSearchCompanyBinding

@AndroidEntryPoint
class SearchCompanyFragment : BaseFragment<FragmentSearchCompanyBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchCompanyBinding
        get() = FragmentSearchCompanyBinding::inflate

    private val _viewModel: SearchCompanyViewModel by viewModels()

    private val _adapter = SearchCompanyAdapter {
        _viewModel.setEvent(Event.OnSelectCurrentAccount(it!!))
    }

    override fun prepareView(savedInstanceState: Bundle?) {
        binding.apply {
            btnSearch.setOnClickListener {
                val currentTitle = etCurrentTitle.text.toString()
                _viewModel.setEvent(Event.OnClickSearchButton(currentTitle))
            }

            btnStartGoodsAcceptance.setOnClickListener {
                val loggedUser = _viewModel.currentState.loggedUser
                val selectedCurrentAccount = _viewModel.currentState.selectedCurrentAccount
                _viewModel.setEvent(Event.OnClickStartGoodsAcceptanceButton(loggedUser, selectedCurrentAccount))
            }
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() = binding.apply {
        rvCurrents.adapter = _adapter
    }

    override fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.uiState.collect { uiState ->
                    _adapter.submitList(uiState.currentAccounts)
                }
            }

        }
    }

    override fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.effect.collect { effect ->
                    when (effect) {
                        is Effect.NavigateToUnplannedAcceptance -> {
                            val action = SearchCompanyFragmentDirections.actionSearchCompanyFragmentToUnplannedAcceptanceFragment(
                                effect.loggedUser, effect.currentAccount
                            )
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
    }
}