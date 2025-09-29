package tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.feature_common.BackPressInterceptor
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog.DocumentSeriesNumberDialogManager
import tr.com.cetinkaya.feature_common.snackbar.showErrorSnackbar
import tr.com.cetinkaya.feature_common.utils.setTextIfChanged
import tr.com.cetinkaya.feature_goods_transfer.R
import tr.com.cetinkaya.feature_goods_transfer.databinding.FragmentWarehouseGoodsTransferBinding
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.WarehouseUiModel

@AndroidEntryPoint
@SuppressLint("ResourceType")
class WarehouseGoodsTransferFragment : BaseFragment<FragmentWarehouseGoodsTransferBinding>(), BackPressInterceptor {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWarehouseGoodsTransferBinding
        get() = FragmentWarehouseGoodsTransferBinding::inflate
    private lateinit var dialogManager: DocumentSeriesNumberDialogManager
    private val _viewModel: WarehouseGoodsTransferViewModel by viewModels()
    private val args: WarehouseGoodsTransferFragmentArgs by navArgs()
    private var isUnitsAdapterSet = false
    private val _adapter = WarehouseGoodsTransferAdapter(onItemClick = null, onItemLongClick = { stockTx ->
        _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnLongTapStockTx(stockTx))
    })
    private lateinit var backCallback: OnBackPressedCallback

    override fun prepareView(savedInstanceState: Bundle?) {
        focusBarcodeInput()
        initializeViewModel()
        setupDialogManager()
        setupListeners()

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnClickExit)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, backCallback
        )
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
        dialogManager = DocumentSeriesNumberDialogManager(fragment = this, onPositive = { date, series, number, paper ->
            val stockTransactionDocument = StockTransactionDocumentUiModel(
                documentDate = date,
                documentSeries = series,
                documentNumber = number,
                paperNumber = paper,
                transactionType = StockTransactionType.WarehouseTransfer,
                transactionKind = StockTransactionKind.InternalTransfer,
                isNormalOrReturn = 0,
                transactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote
            )
            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnConfirmDocumentDialog(stockTransactionDocument))
        }, onNegative = {
            findNavController().popBackStack(R.id.goods_transfer_operations_nav_graph, inclusive = true)
        }, onDocumentNumberChanged = { docSeries, docNumber ->
            val currentDoc = _viewModel.currentState.stockTxDoc
            val tempStockTxDoc = currentDoc?.copy(
                documentSeries = docSeries, documentNumber = docNumber
            )

            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnDocumentNumberChanged(tempStockTxDoc))
        })
    }

    private fun setupListeners() {
        setupWarehouseSelectionListener()
        setupUnitSelectionListener()
        setupBarcodeListener()
        setupQuantityListener()
        setupMenu()
        binding.rvGoodsTransfers.adapter = _adapter

        binding.tilBarcode.setEndIconOnClickListener {
            _viewModel.currentState.barcodeDefinition?.let {
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnClickGetAssortmentBarcodeIcon(it.stockCode))
            }
        }
    }

    private fun setupWarehouseSelectionListener() {
        binding.tvDestinationWarehouse.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selected = parent.getItemAtPosition(position) as WarehouseUiModel
            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnWarehouseSelected(selected))
        }
    }

    private fun setupBarcodeListener() {
        binding.etBarcode.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (keyEvent.action == KeyEvent.ACTION_UP) {
                    commitBarcode()
                }
                return@setOnKeyListener true
            }
            false
        }

        binding.etBarcode.setOnEditorActionListener { view, actionId, event ->
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER
            val isImeDone = actionId == EditorInfo.IME_ACTION_DONE
            if (!(isEnterKey || isImeDone)) return@setOnEditorActionListener false

            if (event == null || event.action == KeyEvent.ACTION_UP) {
                commitBarcode()

                binding.etQuantity.requestFocus()
                binding.etQuantity.selectAll()
                val imm =
                    requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            true
        }

        binding.etBarcode.imeOptions = binding.etBarcode.imeOptions or EditorInfo.IME_FLAG_NO_ENTER_ACTION
    }

    private fun setupQuantityListener() {
        // 1) HW klavye Enter’ını da tüket (özellikle barkod==miktar durumunda)
        binding.etQuantity.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val currentBarcode = _viewModel.currentState.barcodeDefinition?.barcode.orEmpty()
                val text = binding.etQuantity.text?.toString()?.trim().orEmpty()
                if (text.contains(currentBarcode) && currentBarcode.isNotEmpty()) {
                    if (keyEvent.action == KeyEvent.ACTION_UP) {
                        binding.root.showErrorSnackbar("Miktar alanına barkod okutuldu.\nLütfen doğru miktarı girininiz.")
                        binding.etQuantity.requestFocus()
                        binding.etQuantity.selectAll()
                    }
                    return@setOnKeyListener true // Enter'ı tamamen tüket
                }
            }
            false
        }

        // 2) IME Done / Enter
        binding.etQuantity.setOnEditorActionListener { view, actionId, event ->
            val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER
            val isImeDone = actionId == EditorInfo.IME_ACTION_DONE
            if (!(isImeDone || isEnterKey)) return@setOnEditorActionListener false

            val raw = view.text?.toString()?.trim().orEmpty()
            val currentBarcode = _viewModel.currentState.barcodeDefinition?.barcode.orEmpty()

            // Barkod miktar alanına gelmişse kaydetme, odak miktarda kalsın
            if (raw == currentBarcode && currentBarcode.isNotEmpty()) {
                if (event == null || event.action == KeyEvent.ACTION_UP) {
                    binding.root.showErrorSnackbar("Miktar alanına barkod okutuldu. Lütfen miktarı girin.")
                    binding.etQuantity.requestFocus()
                    binding.etQuantity.selectAll()
                }
                return@setOnEditorActionListener true // tüket
            }

            // Geçersiz miktar kontrolü (isteğe bağlı ama önerilir)
            val q = raw.replace(',', '.').toDoubleOrNull() ?: 0.0
            if (q <= 0.0) {
                if (event == null || event.action == KeyEvent.ACTION_UP) {
                    binding.root.showErrorSnackbar("Miktar 0'dan büyük olmalıdır.")
                    binding.etQuantity.requestFocus()
                    binding.etQuantity.selectAll()
                    _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnQuantityChanged(0.0))
                }
                return@setOnEditorActionListener true
            }

            // Geçerli → Kaydet
            if (event == null || event.action == KeyEvent.ACTION_UP) {
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnSaveTransfer)
                val imm =
                    requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            true
        }

        // 3) TextWatcher (ters şartı düzeltildi)
        binding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // programatik setText tetiklerini yoksay
                if (!binding.etQuantity.hasFocus()) return

                val currentBarcode = _viewModel.currentState.barcodeDefinition?.barcode.orEmpty()
                val text = s?.toString()?.trim().orEmpty()

                // BARKODLA AYNIYSA event GÖNDERME (enter'da da kaydetmiyoruz)
                if (text == currentBarcode && currentBarcode.isNotEmpty()) return

                // boşsa 0.0 → VM validasyonu kaydı engeller
                val quantity = text.replace(',', '.').toDoubleOrNull() ?: 0.0
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnQuantityChanged(quantity))
            }
        })

        // 4) Ek güvenlik: IME’nin default enter davranışını kapat
        binding.etQuantity.imeOptions = binding.etQuantity.imeOptions or EditorInfo.IME_FLAG_NO_ENTER_ACTION
    }

    private fun setupUnitSelectionListener() {
        binding.tvUnit.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selected = parent.getItemAtPosition(position) as String
            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnUnitSelected(selected))
        }
    }

    override fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.uiState.collectLatest { uiState ->
                    setupUnitsAdapter(uiState)
                    setupWarehousesAdapter(uiState)
                    updateViews(uiState)
                    if (uiState.selectedStockTransaction != null) {
                        binding.etBarcode.text = Editable.Factory.getInstance().newEditable(uiState.selectedStockTransaction.barcode)
                        binding.etStockName.text = Editable.Factory.getInstance().newEditable(uiState.selectedStockTransaction.stockName)
                        binding.etQuantity.text = Editable.Factory.getInstance().newEditable(uiState.selectedStockTransaction.quantity.toString())
                    }
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
            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnWarehouseSelected(uiState.warehouses.first()))
        }
    }

    private fun setWarehouseAdapter(warehouses: List<WarehouseUiModel>, selectedWarehouse: WarehouseUiModel?) {
        val warehousesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, warehouses)
        warehousesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.tvDestinationWarehouse.setAdapter(warehousesAdapter)
        binding.tvDestinationWarehouse.setText(selectedWarehouse?.name, false)
    }

    private fun updateViews(uiState: WarehouseGoodsTransferContract.State) {
        binding.etStockName.setTextIfChanged(uiState.barcodeDefinition?.stockName.orEmpty())
        binding.etBarcode.setTextIfChanged(uiState.barcodeDefinition?.barcode.orEmpty())

        val oldFirst = _viewModel.previousState?.stockTransactions?.firstOrNull()
        val newFirst = uiState.stockTransactions.firstOrNull()
        val shouldScrollTop = oldFirst != null && newFirst != null && oldFirst.barcode != newFirst.barcode

        _adapter.submitList(uiState.stockTransactions) {
            if (shouldScrollTop) {
                binding.rvGoodsTransfers.smoothScrollToPosition(0)
            }
        }

        if (!binding.etQuantity.hasFocus() && binding.etQuantity.text.toString() != uiState.quantity.toString()) {
            binding.etQuantity.setTextIfChanged(uiState.quantity.toString())
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
                binding.etBarcode.setText("")
            }

            is WarehouseGoodsTransferContract.Effect.ShowLoading -> {
                binding.loadingContainer.visibility = View.VISIBLE
            }

            is WarehouseGoodsTransferContract.Effect.DismissLoading -> {
                binding.loadingContainer.visibility = View.GONE
            }

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

            is WarehouseGoodsTransferContract.Effect.SetDialogBlockingError -> {
                dialogManager.setBlockingErrorOnDialog(effect.message)
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
                        _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnClickFinish)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun commitBarcode() {
        val barcode = binding.etBarcode.text?.toString()?.trim().orEmpty()
        if (barcode.isEmpty()) {
            binding.etBarcode.requestFocus()
            return
        }
        _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnBarcodeEntered(barcode))
    }

    override fun onToolbarBackButtonPressed(): Boolean {
        _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnClickExit)
        return true
    }
}