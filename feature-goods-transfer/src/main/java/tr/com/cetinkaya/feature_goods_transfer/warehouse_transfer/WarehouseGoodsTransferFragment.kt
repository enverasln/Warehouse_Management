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
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.feature_common.BackPressInterceptor
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_common.dialog.document_series_number_dialog.DocumentSeriesNumberDialogManager
import tr.com.cetinkaya.feature_common.snackbar.showErrorSnackbar
import tr.com.cetinkaya.feature_common.snackbar.showSuccessSnackbar
import tr.com.cetinkaya.feature_goods_transfer.R
import tr.com.cetinkaya.feature_goods_transfer.databinding.FragmentWarehouseGoodsTransferBinding
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.StockTransactionDocumentUiModel
import tr.com.cetinkaya.feature_goods_transfer.warehouse_transfer.models.WarehouseUiModel

@AndroidEntryPoint
class WarehouseGoodsTransferFragment : BaseFragment<FragmentWarehouseGoodsTransferBinding>(), BackPressInterceptor {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWarehouseGoodsTransferBinding
        get() = FragmentWarehouseGoodsTransferBinding::inflate

    private lateinit var dialogManager: DocumentSeriesNumberDialogManager
    private val _viewModel: WarehouseGoodsTransferViewModel by viewModels()
    private val args: WarehouseGoodsTransferFragmentArgs by navArgs()
    private var isUnitsAdapterSet = false
    private val _adapter = WarehouseGoodsTransferAdapter { product ->
        _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnSelectStockTransaction(product))
    }

    private var isExitDialogShowing = false
    private lateinit var backCallback: OnBackPressedCallback

    override fun prepareView(savedInstanceState: Bundle?) {
        focusBarcodeInput()
        initializeViewModel()
        setupDialogManager()
        setupListeners()

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = showExitConfirmationDialog()
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
                date,
                series,
                number,
                paper,
                StockTransactionType.WarehouseTransfer,
                StockTransactionKind.InternalTransfer,
                0,
                StockTransactionDocumentType.InterWarehouseShippingNote
            )
            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnDocumentDialogConfirmed(stockTransactionDocument))
        }, onNegative = {
            findNavController().popBackStack(R.id.goods_transfer_operations_nav_graph, inclusive = true)
        }, onDocumentNumberChanged = { documentSeries, documentNumber ->
            _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnDocumentNumberChanged(documentSeries, documentNumber))
        })
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
                if (text == currentBarcode && currentBarcode.isNotEmpty()) {
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
                binding.etBarcode.setText("")
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
                        showFinishAcceptanceConfirmationDialog()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showFinishAcceptanceConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.dialog_title_warning))
            .setMessage(getString(R.string.finish_warehouse_transfer)).setPositiveButton(getString(R.string.dialog_button_yes)) { _, _ ->
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnFinishWarehouseTransfer)
            }.setNeutralButton(getString(R.string.dialog_button_cancel), null).show()
    }

    private fun showExitConfirmationDialog() {
        if (isExitDialogShowing) return
        isExitDialogShowing = true

        backCallback.isEnabled = false

        val ctx = requireContext()
        val dp = resources.displayMetrics.density
        val padH = (24 * dp).toInt()
        val padV = (18 * dp).toInt()

        val messageView = TextView(ctx).apply {
            text = "Belge kalıcı olarak silinecektir.\nTekrar kurtarılamayacaktır.\n\nBu sayfadan çıkmak istediğinize emin misiniz?"
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
        }

        val checkBox = CheckBox(ctx).apply {
            text = "Kalıcı olarak silmeyi onaylıyorum"
        }

        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padH, padV, padH, padV)
            addView(
                messageView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            addView(
                checkBox, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val dialog = MaterialAlertDialogBuilder(ctx).setTitle("Dikkat").setIcon(R.drawable.ic_warning) // varsa
            .setView(container).setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .setPositiveButton("Evet, sil", null) // onShow’da handle edeceğiz
            .create()

        dialog.setOnShowListener {
            val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positive.isEnabled = false

            // “Destructive” vurgusu (isteğe bağlı)
            val errorColor = com.google.android.material.color.MaterialColors.getColor(
                positive, com.google.android.material.R.attr.colorError
            )
            positive.setTextColor(errorColor)

            checkBox.setOnCheckedChangeListener { _, checked ->
                positive.isEnabled = checked
            }

            positive.setOnClickListener {
                _viewModel.setEvent(WarehouseGoodsTransferContract.Event.OnCancelWarehouseTransfer)
                dialog.dismiss()
            }
        }

        dialog.setOnDismissListener {
            isExitDialogShowing = false
            if (isAdded) backCallback.isEnabled = true
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
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
        showExitConfirmationDialog()
        return true
    }


}