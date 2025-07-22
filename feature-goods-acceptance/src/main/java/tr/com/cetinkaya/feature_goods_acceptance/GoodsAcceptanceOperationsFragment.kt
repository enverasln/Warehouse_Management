package tr.com.cetinkaya.feature_goods_acceptance

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog.DocumentSeriesNumberDialogFragment
import tr.com.cetinkaya.feature_goods_acceptance.databinding.FragmentGoodsAcceptanceOperationsBinding


class GoodsAcceptanceOperationsFragment : BaseFragment<FragmentGoodsAcceptanceOperationsBinding>() {
    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGoodsAcceptanceOperationsBinding
        get() = FragmentGoodsAcceptanceOperationsBinding::inflate

    private lateinit var _dialog: DocumentSeriesNumberDialogFragment

    override fun prepareView(savedInstanceState: Bundle?) {


        binding.cvPlannedOrderAcceptance.setOnClickListener {
            findNavController().navigate(R.id.action_goodsAcceptanceOperationsFragment_to_searchPlannedGoodsAcceptanceDocumentFragment)
        }


    }

    override fun observeState() {

    }



}