package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_goods_transfer.databinding.FragmentWarehouseGoodsTransferBinding

class WarehouseGoodsTransferFragment : BaseFragment<FragmentWarehouseGoodsTransferBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWarehouseGoodsTransferBinding
        get() = FragmentWarehouseGoodsTransferBinding::inflate

    override fun prepareView(savedInstanceState: Bundle?) {
        
    }

}