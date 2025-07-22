package tr.com.cetinkaya

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_goods_transfer.databinding.FragmentGoodsTransferOperationsBinding

class GoodsTransferOperationsFragment : BaseFragment<FragmentGoodsTransferOperationsBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGoodsTransferOperationsBinding
        get() = FragmentGoodsTransferOperationsBinding::inflate

    override fun prepareView(savedInstanceState: Bundle?) {

    }


}