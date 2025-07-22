package tr.com.cetinkaya.feature_sync

import android.os.Bundle
import androidx.fragment.app.Fragment
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

    override fun prepareView(savedInstanceState: Bundle?) {
        binding.btnStartSync.setOnClickListener {
            viewModel.setEvent(SynchronizationContract.Event.OnStartSynchronization)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvMessages.text = state.messages.joinToString("\n")
                }
            }
        }
    }





}