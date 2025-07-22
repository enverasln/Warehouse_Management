package tr.com.cetinkaya

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_goods_transfer.databinding.FragmentGoodsTransferOperationsBinding

class GoodsTransferOperationsFragment : BaseFragment<FragmentGoodsTransferOperationsBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGoodsTransferOperationsBinding
        get() = FragmentGoodsTransferOperationsBinding::inflate

    override fun prepareView(savedInstanceState: Bundle?) {
        binding.cvWarehouseGoodsTransfer.setOnClickListener {
            findNavController().navigate(GoodsTransferOperationsFragmentDirections.actionGoodsTransferOperationsFragmentToWarehouseGoodsTransferFragment())

        }
    }


}