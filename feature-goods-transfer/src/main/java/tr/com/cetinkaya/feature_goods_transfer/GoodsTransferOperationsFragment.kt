package tr.com.cetinkaya.feature_goods_transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_goods_transfer.databinding.FragmentGoodsTransferOperationsBinding

@AndroidEntryPoint
class GoodsTransferOperationsFragment : BaseFragment<FragmentGoodsTransferOperationsBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGoodsTransferOperationsBinding
        get() = FragmentGoodsTransferOperationsBinding::inflate

    private val _viewModel: GoodsTransferOperationsViewModel by viewModels()

    override fun prepareView(savedInstanceState: Bundle?) {
        binding.cvWarehouseGoodsTransfer.setOnClickListener {
            _viewModel.setEvent(GoodsTransferOperationsContracts.Event.OnWarehouseGoodsTransferButtonClick)

        }
    }


    override fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is GoodsTransferOperationsContracts.Effect.NavigateToWarehouseGoodsTransfer -> {

                            val loggedUser = _viewModel.currentState.loggedUser

                            if (loggedUser == null)  TODO("Kullanıcı bilgisi yer almadığı için uygulamayı sonladırıp yeniden başlacak")

                            val action = GoodsTransferOperationsFragmentDirections.actionGoodsTransferOperationsFragmentToWarehouseGoodsTransferFragment(loggedUser)
                            findNavController().navigate(action)


                        }
                    }


                }
            }
        }
    }


}