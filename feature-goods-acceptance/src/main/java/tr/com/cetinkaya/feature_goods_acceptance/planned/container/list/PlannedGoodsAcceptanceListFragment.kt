package tr.com.cetinkaya.feature_goods_acceptance.planned.container.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_goods_acceptance.R
import tr.com.cetinkaya.feature_goods_acceptance.databinding.FragmentPlannedGoodsAcceptanceListBinding
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.PlannedGoodsAcceptanceContainerContract
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.PlannedGoodsAcceptanceContainerViewModel


@AndroidEntryPoint
class PlannedGoodsAcceptanceListFragment : BaseFragment<FragmentPlannedGoodsAcceptanceListBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPlannedGoodsAcceptanceListBinding
        get() = FragmentPlannedGoodsAcceptanceListBinding::inflate

    private val _sharedViewModel: PlannedGoodsAcceptanceContainerViewModel by hiltNavGraphViewModels(R.id.goods_acceptance_operation_graph)

    private val _adapter = PlannedGoodsAcceptanceListAdapter { product ->
        product?.let {
            _sharedViewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.OnProductDoubleTab(it))
        }
    }

    override fun prepareView(savedInstanceState: Bundle?) {
        binding.rvPlannedGoodsAcceptanceProducts.adapter = _adapter

    }

    override fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                _sharedViewModel.uiState.collect { uiState ->

                    _adapter.submitList(uiState.products)


                    binding.tvCompanyName.text = uiState.companyName
                }
            }
        }
    }

}