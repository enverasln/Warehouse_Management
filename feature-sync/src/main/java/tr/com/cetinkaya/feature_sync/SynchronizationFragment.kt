package tr.com.cetinkaya.feature_sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_sync.databinding.FragmentSynchronizationBinding

@AndroidEntryPoint
class SynchronizationFragment : BaseFragment<FragmentSynchronizationBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSynchronizationBinding
        get() = FragmentSynchronizationBinding::inflate

    private val viewModel: SynchronizationViewModel by viewModels()
    private val _adapter = TransferredDocumentAdapter()

    override fun prepareView(savedInstanceState: Bundle?) {
        viewModel.setEvent(SynchronizationContract.Event.OnFetchTransferredDocuments)
        binding.btnStartSync.setOnClickListener {
            viewModel.setEvent(SynchronizationContract.Event.OnStartSynchronization)
        }
        binding.rvTransferredDocuments.adapter = _adapter

    }


    override fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvMessages.text = state.messages.joinToString("\n")
                    _adapter.submitList(state.documents)
                }
            }
        }
    }

    override fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when(effect) {
                        is SynchronizationContract.Effect.ShowLoading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnStartSync.isEnabled = false
                        }
                        is SynchronizationContract.Effect.DismissLoading -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnStartSync.isEnabled = true
                        }
                    }

                }
            }
        }
    }


}