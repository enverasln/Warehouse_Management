package tr.com.cetinkaya.feature_home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_home.databinding.FragmentMainMenuBinding


class MainMenuFragment : BaseFragment<FragmentMainMenuBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainMenuBinding
        get() = FragmentMainMenuBinding::inflate

    override fun prepareView(savedInstanceState: Bundle?) {
        binding.cvOrderOperations.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_goods_acceptance_operation_graph)
        }

        binding.cvTransferOperations.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_goods_transfer_operations_graph)
        }


        binding.cvSynchronization.setOnClickListener {
            findNavController().navigate(R.id.synchronizationFragment)
        }
    }



}