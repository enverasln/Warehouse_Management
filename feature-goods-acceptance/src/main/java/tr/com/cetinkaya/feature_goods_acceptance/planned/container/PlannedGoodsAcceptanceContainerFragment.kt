package tr.com.cetinkaya.feature_goods_acceptance.planned.container

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog.DocumentSeriesNumberDialogManager
import tr.com.cetinkaya.feature_goods_acceptance.R
import tr.com.cetinkaya.feature_goods_acceptance.databinding.FragmentPlannedGoodsAcceptanceContainerBinding
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance.PlannedGoodsAcceptanceFragment
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.list.PlannedGoodsAcceptanceListFragment
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel

@AndroidEntryPoint
class PlannedGoodsAcceptanceContainerFragment : BaseFragment<FragmentPlannedGoodsAcceptanceContainerBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPlannedGoodsAcceptanceContainerBinding
        get() = FragmentPlannedGoodsAcceptanceContainerBinding::inflate

    private val viewModel: PlannedGoodsAcceptanceContainerViewModel by hiltNavGraphViewModels(R.id.goods_acceptance_operation_graph)
    private lateinit var dialogManager: DocumentSeriesNumberDialogManager
    lateinit var viewPager: ViewPager2

    private val args: PlannedGoodsAcceptanceContainerFragmentArgs by navArgs()


    override fun prepareView(savedInstanceState: Bundle?) {
        val selectedDocuments = args.selectedDocuments.toList()
        val loggedUser = args.loggedUser

        viewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.Initialize(loggedUser = loggedUser, selectedDocuments = selectedDocuments))

        dialogManager = DocumentSeriesNumberDialogManager(this, onPositive = { date, series, number, paper ->
            val stockTransactionDocument = StockTransactionDocumentUiModel(date, series, number, paper, 0, 0, 0, 13)
            viewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.OnDocumentDialogConfirmed(stockTransactionDocument))
        }, onNegative = {
            if (isAdded) findNavController().popBackStack()
        })

        setupMenu()
        setupViewPager()
        setupTabLayout()
        observeState()
        observeEffect()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.OnStateReset)
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_goods_acceptance, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_finish_goods_acceptance -> {
                        showFinishAcceptanceConfirmationDialog()

                        true
                    }

                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupViewPager() {
        viewPager = binding.vpContainer
        viewPager.adapter = PlannedGoodsAcceptanceContainerViewPagerAdapter(
            childFragmentManager, lifecycle, listOf({ PlannedGoodsAcceptanceFragment() }, { PlannedGoodsAcceptanceListFragment() })
        )

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (binding.tlContainer.selectedTabPosition != position) {
                    binding.tlContainer.selectTab(binding.tlContainer.getTabAt(position))
                }
                viewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.TabChanged(position))
            }
        })
    }

    private fun setupTabLayout() {
        val tabLayout = binding.tlContainer
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_title_process)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_title_list)))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    // UI tarafından sayfayı anında değiştir
                    if (viewPager.currentItem != it.position) {
                        viewPager.currentItem = it.position
                    }

                    // ViewModel'a durumu bildir
                    viewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.TabChanged(it.position))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    override fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (viewPager.currentItem != state.currentTabIndex) {
                        viewPager.currentItem = state.currentTabIndex
                    }
                }
            }
        }
    }

    override fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is PlannedGoodsAcceptanceContainerContract.Effect.ShowError -> showErrorDialog(effect.message)
                        is PlannedGoodsAcceptanceContainerContract.Effect.ShowConfirmationDialog -> showConfirmationDialog(effect.message)
                        is PlannedGoodsAcceptanceContainerContract.Effect.ShowDocumentDialog -> showDocumentDialog()
                        is PlannedGoodsAcceptanceContainerContract.Effect.DismissDialog -> {
                            dialogManager.dismiss()
                            getAcceptanceFragment()?.onStartGoodsAcceptance()
                        }

                        is PlannedGoodsAcceptanceContainerContract.Effect.ShowSnackbar -> {
                            Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_SHORT).show()
                        }


                    }
                }
            }
        }
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.dialog_title_error)).setMessage(message)
            .setPositiveButton(getString(R.string.dialog_button_ok), null).show()
    }

    private fun showConfirmationDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.dialog_title_warning)).setMessage(message)
            .setPositiveButton(getString(R.string.dialog_button_yes)) { _, _ ->
                viewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.FetchProducts)
                val currentFragment = childFragmentManager.findFragmentByTag("${binding.vpContainer.currentItem}")
                if (currentFragment is PlannedGoodsAcceptanceFragment) {
                    currentFragment.onStartGoodsAcceptance()
                }
                dialogManager.dismiss()

                getAcceptanceFragment()?.onStartGoodsAcceptance()
            }.setNeutralButton(getString(R.string.dialog_button_cancel), null).show()
    }

    private fun showDocumentDialog() {
        val documentSeries = viewModel.currentState.loggedUser?.documentSeries ?: "-"
        dialogManager.showDialog(documentSeries)
    }

    private fun showFinishAcceptanceConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_warning))
            .setMessage(getString(R.string.finish_acceptance_confirmation_message))
            .setPositiveButton(getString(R.string.dialog_button_yes)) { _, _ ->
                getAcceptanceFragment()?.onUpdateOrderSyncStatus()
                viewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.OnFinishAcceptance)
                findNavController().popBackStack(R.id.goods_acceptance_operation_graph, inclusive = true)
            }.setNeutralButton(getString(R.string.dialog_button_cancel), null).show()
    }

    private fun getAcceptanceFragment(): PlannedGoodsAcceptanceFragment? {
        return childFragmentManager.fragments.filterIsInstance<PlannedGoodsAcceptanceFragment>().firstOrNull()
    }
}

