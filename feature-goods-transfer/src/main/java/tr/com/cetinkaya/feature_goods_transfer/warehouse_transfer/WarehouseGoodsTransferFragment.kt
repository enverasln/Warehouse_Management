package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog.DocumentSeriesNumberDialogManager
import tr.com.cetinkaya.feature_common.snackbar.showErrorSnackbar
import tr.com.cetinkaya.feature_common.snackbar.showSuccessSnackbar
import tr.com.cetinkaya.feature_goods_transfer.R
import tr.com.cetinkaya.feature_goods_transfer.databinding.FragmentWarehouseGoodsTransferBinding
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.WarehouseUiModel

@AndroidEntryPoint
class WarehouseGoodsTransferFragment : BaseFragment<FragmentWarehouseGoodsTransferBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWarehouseGoodsTransferBinding
        get() = FragmentWarehouseGoodsTransferBinding::inflate

    private lateinit var dialogManager: DocumentSeriesNumberDialogManager
    private val _viewModel: WarehouseGoodsTransferViewModel by viewModels()
    private val args: WarehouseGoodsTransferFragmentArgs by navArgs()
    private var isUnitsAdapterSet = false
    private val _adapter = WarehouseGoodsTransferAdapter()

    override fun prepareView(savedInstanceState: Bundle?) {
        focusBarcodeInput()
        initializeViewModel()
        setupDialogManager()
        setupListeners()
    }

    private fun focusBarcodeInput() {
        binding.etBarcode.requestFocus()
        binding.etBarcode.selectAll()
    }

    private fun initializeViewModel() {
        val loggedUser = args.loggedUser
        _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnInitialize(loggedUser = loggedUser))

    }

    private fun setupDialogManager() {
        dialogManager = DocumentSeriesNumberDialogManager(
            fragment = this,
            onPositive = { date, series, number, paper ->
                val stockTransactionDocument = StockTransactionDocumentUiModel(date, series, number, paper, StockTransactionTypes.WarehouseTransfer,
                    StockTransactionKinds.InternalTransfer, 0, StockTransactionDocumentTypes.InterWarehouseShippingNote)
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnDocumentDialogConfirmed(stockTransactionDocument))
            }, onNegative = {
                findNavController().popBackStack(R.id.goods_transfer_operations_nav_graph, inclusive = true)
            }
        )
    }

    private fun setupListeners() {
        setupWarehouseSelectionListener()
        setupUnitSelectionListener()
        setupBarcodeListener()
        setupQuantityListener()
        setupMenu()

        binding.rvGoodsTransfers.adapter = _adapter
    }

    private fun setupWarehouseSelectionListener() {
        binding.tvDestinationWarehouse.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selected = parent.getItemAtPosition(position) as WarehouseUiModel
            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnWarehouseSelected(selected))
        }

    }

    private fun setupBarcodeListener() {
        binding.etBarcode.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnBarcodeEntered(binding.etBarcode.text.toString()))
                true
            } else false
        }
    }

    private fun setupQuantityListener() {
        binding.etQuantity.setOnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnSaveTransfer)

                // Klavyeyi kapat
                val imm =
                    requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                true
            } else false
        }

        binding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val quantity = s.toString().trim().toDoubleOrNull() ?: 1.0
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnTransferredQuantityChanged(quantity))
            }
        })
    }

    private fun setupUnitSelectionListener() {
        binding.tvUnit.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selected = parent.getItemAtPosition(position) as String
            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnUnitSelected(selected))
        }
    }


    override fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.uiState.collectLatest { uiState ->
                    setupUnitsAdapter(uiState)
                    setupWarehousesAdapter(uiState)
                    updateViews(uiState)
                }
            }
        }
    }

    private fun setupUnitsAdapter(uiState: WarehouseGoodsTransferContract.State) {
        if (!isUnitsAdapterSet) {
            val unitsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uiState.units)
            unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.tvUnit.setAdapter(unitsAdapter)
            binding.tvUnit.setText(uiState.selectedUnit, false)
            isUnitsAdapterSet = true
        }
    }

    private fun setupWarehousesAdapter(uiState: WarehouseGoodsTransferContract.State) {

        if (_viewModel.previousState?.warehouses != uiState.warehouses) {
            setWarehouseAdapter(uiState.warehouses, uiState.selectedWarehouse)
        }

        binding.tilDestinationWarehouse.isEnabled = uiState.warehouses.isNotEmpty()
        binding.tvDestinationWarehouse.setText(uiState.selectedWarehouse?.name, false)

        if (uiState.selectedWarehouse == null && uiState.warehouses.isNotEmpty()) {
            _viewModel.setState { copy(selectedWarehouse = uiState.warehouses.first()) }
        }
    }

    private fun setWarehouseAdapter(warehouses: List<WarehouseUiModel>, selectedWarehouse: WarehouseUiModel?) {
        val warehousesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, warehouses)
        warehousesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.tvDestinationWarehouse.setAdapter(warehousesAdapter)
        binding.tvDestinationWarehouse.setText(selectedWarehouse?.name, false)
    }

    private fun updateViews(uiState: WarehouseGoodsTransferContract.State) {
        binding.etStockName.setText(uiState.barcodeDefinition?.stockName)
        _adapter.submitList(uiState.transferredProducts)
        if (!binding.etQuantity.hasFocus() && binding.etQuantity.text.toString() != uiState.quantity.toString()) {
            binding.etQuantity.setText(uiState.quantity.toString())
            binding.etQuantity.selectAll()
        }
    }

    override fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.effect.collectLatest { effect ->
                    handleEffect(effect)
                }
            }
        }
    }

    private fun handleEffect(effect: WarehouseGoodsTransferContract.Effect) {
        when (effect) {
            is WarehouseGoodsTransferContract.Effect.ShowDocumentDialog -> dialogManager.showDialog(effect.documentSeries, effect.documentNumber)
            is WarehouseGoodsTransferContract.Effect.DismissDialog -> dialogManager.dismiss()
            is WarehouseGoodsTransferContract.Effect.RequestFocusOnBarcode -> {
                binding.etBarcode.requestFocus()
                binding.etBarcode.selectAll()
            }

            is WarehouseGoodsTransferContract.Effect.ShowError -> binding.root.showErrorSnackbar(effect.message)
            is WarehouseGoodsTransferContract.Effect.ShowSuccess -> binding.root.showSuccessSnackbar(effect.message)
            is WarehouseGoodsTransferContract.Effect.ShowLoading -> {}
            is WarehouseGoodsTransferContract.Effect.DismissLoading -> {}
            is WarehouseGoodsTransferContract.Effect.NavigateToMainMenu -> {
                findNavController().popBackStack(R.id.goods_transfer_operations_nav_graph, inclusive = true)
            }

            is WarehouseGoodsTransferContract.Effect.RequestFocusOnQuantity -> {
                binding.etQuantity.post {
                    binding.etQuantity.requestFocus()
                    val imm =
                        requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.showSoftInput(binding.etQuantity, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                }

            }
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_warehouse_transfer, menu)
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

    private fun showFinishAcceptanceConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_warning))
            .setMessage(getString(R.string.finish_warehouse_transfer))
            .setPositiveButton(getString(R.string.dialog_button_yes)) { _, _ ->
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnFinishWarehouseTransfer)
            }.setNeutralButton(getString(R.string.dialog_button_cancel), null).show()
    }
}