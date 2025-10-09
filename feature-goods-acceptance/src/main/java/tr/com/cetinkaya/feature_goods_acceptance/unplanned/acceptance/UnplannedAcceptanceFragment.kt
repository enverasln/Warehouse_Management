package tr.com.cetinkaya.feature_goods_acceptance.unplanned.acceptance

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
import android.view.inputmethod.InputMethodManager
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
import tr.com.cetinkaya.feature_goods_acceptance.R
import tr.com.cetinkaya.feature_goods_acceptance.databinding.FragmentUnplannedAcceptanceBinding
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel

@AndroidEntryPoint
class UnplannedAcceptanceFragment : BaseFragment<FragmentUnplannedAcceptanceBinding>(), BackPressInterceptor {

    companion object {
        val TAG = "UnplannedAcceptanceFragment"
    }

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUnplannedAcceptanceBinding
        get() = FragmentUnplannedAcceptanceBinding::inflate
    private lateinit var dialogManager: DocumentSeriesNumberDialogManager
    private val _viewModel: UnplannedAcceptanceViewModel by viewModels()
    private val args: UnplannedAcceptanceFragmentArgs by navArgs()
    private val _adapter = UnplannedAcceptanceAdapter { stockTx ->
        _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnLongTapStockTx(stockTx))
    }
    private var isUnitsAdapterSet = false

    private lateinit var backCallback: OnBackPressedCallback

    @SuppressLint("SetTextI18n")
    override fun prepareView(savedInstanceState: Bundle?) {
        setupMenu()
        setupListener()
        setupDialogManager()

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val stockDocTx = _viewModel.currentState.stockTxDoc!!
                _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnClickExit(stockDocTx))
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, backCallback
        )

        binding.apply {
            etCompanyName.setText("${args.selectedCompany.currentTitle1} ${args.selectedCompany.currentTitle2}")

        }
        val loggedUser = args.loggedUser!!
        val currentAccount = args.selectedCompany
        _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnInitialize(loggedUser, currentAccount))

        binding.tilBarcode.setEndIconOnClickListener {
            _viewModel.currentState.barcodeDef?.let {
                _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnClickAssortmentBarcodeIcon(it.stockCode, loggedUser.warehouseNumber))
            }
        }
    }

    private fun setupListener() {
        setupBarcodeListener()
        setupQuantityListener()
        binding.cbSingleQuantity.setOnCheckedChangeListener { _, isChecked ->
            _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnChangeSingleQuantityCheckStatus(isChecked))
            if (isChecked) binding.etQuantity.setText("1.0")
        }
        binding.rvTransactions.adapter = _adapter
    }

    private fun setupBarcodeListener() {
        binding.etBarcode.apply {
            setOnKeyListener { _, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (keyEvent.action == KeyEvent.ACTION_UP) {
                        commitBarcode()
                    }
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }

            setOnEditorActionListener { view, actionId, event ->
                val isEnterKey = event?.keyCode == KeyEvent.KEYCODE_ENTER
                val isImeDone = actionId == EditorInfo.IME_ACTION_DONE
                if (!(isEnterKey || isImeDone)) return@setOnEditorActionListener false
                if (event == null || event.action == KeyEvent.ACTION_UP) {
                    commitBarcode()
                    val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
                return@setOnEditorActionListener true
            }

            imeOptions = binding.etBarcode.imeOptions or EditorInfo.IME_FLAG_NO_ENTER_ACTION
        }
    }

    private fun setupQuantityListener() {
        // 1) HW klavye Enter’ını da tüket (özellikle barkod==miktar durumunda)
        binding.etQuantity.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val currentBarcode = _viewModel.currentState.barcodeDef?.barcode.orEmpty()
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
            val currentBarcode = _viewModel.currentState.barcodeDef?.barcode.orEmpty()

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
                    _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnQuantityChanged(0.0))
                }
                return@setOnEditorActionListener true
            }

            // Geçerli → Kaydet
            if (event == null || event.action == KeyEvent.ACTION_UP) {
                val stockTxDoc = _viewModel.currentState.stockTxDoc
                val currentAccCode = args.selectedCompany.currentCode
                val barcodeDef = _viewModel.currentState.barcodeDef
                val selectedUnit = _viewModel.currentState.selectedUnit
                val qty = _viewModel.currentState.quantity
                val user = args.loggedUser!!

                _viewModel.setEvent(
                    UnplannedAcceptanceContract.Event.OnSaveAcceptance(
                        stockTxDoc = stockTxDoc,
                        currentAccCode = currentAccCode,
                        barcodeDef = barcodeDef,
                        unit = selectedUnit,
                        quantity = qty,
                        user = user
                    )
                )
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

                val currentBarcode = _viewModel.currentState.barcodeDef?.barcode.orEmpty()
                val text = s?.toString()?.trim().orEmpty()

                // BARKODLA AYNIYSA event GÖNDERME (enter'da da kaydetmiyoruz)
                if (text == currentBarcode && currentBarcode.isNotEmpty()) return

                // boşsa 0.0 → VM validasyonu kaydı engeller
                val quantity = text.replace(',', '.').toDoubleOrNull() ?: 0.0
                _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnQuantityChanged(quantity))
            }
        })

        // 4) Ek güvenlik: IME’nin default enter davranışını kapat
        binding.etQuantity.imeOptions = binding.etQuantity.imeOptions or EditorInfo.IME_FLAG_NO_ENTER_ACTION
    }

    private fun commitBarcode() {
        val barcode = binding.etBarcode.text?.toString()?.trim().orEmpty()
        if (barcode.isEmpty()) {
            binding.etBarcode.requestFocus()
            return
        }
        _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnBarcodeEntered(barcode, args.loggedUser?.warehouseNumber ?: 0))
    }

    private fun setupDialogManager() {
        dialogManager = DocumentSeriesNumberDialogManager(fragment = this, onPositive = { date, series, number, paper ->
            val unplannedAcceptance = StockTransactionDocumentUiModel(
                documentDate = date,
                documentSeries = series,
                documentNumber = number,
                paperNumber = paper,
                transactionType = StockTransactionType.Input,
                transactionKind = StockTransactionKind.Wholesale,
                isNormalOrReturn = 0,
                transactionDocumentType = StockTransactionDocumentType.EntryDispatchNote
            )
            _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnConfirmDocumentDialog(unplannedAcceptance, currentAccount = args.selectedCompany))
        }, onNegative = {
            _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnCancelDocumentDialog)
        }, onDocumentNumberChanged = { docSeries, documentNumber -> })
    }

    private fun setupUnitsAdapter(uiState: UnplannedAcceptanceContract.State) {
        if (!isUnitsAdapterSet) {
            val unitsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, uiState.units)
            unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.tvUnit.setAdapter(unitsAdapter)
            binding.tvUnit.setText(uiState.selectedUnit, false)
            isUnitsAdapterSet = true
        }
    }

    private fun updateViews(uiState: UnplannedAcceptanceContract.State) {
        binding.apply {
            etStockName.setTextIfChanged(uiState.barcodeDef?.stockName.orEmpty())
            etBarcode.setTextIfChanged(uiState.barcodeDef?.barcode.orEmpty())

            val oldFirst = _viewModel.previousState?.stockTransactions?.firstOrNull()
            val newFirst = uiState.stockTransactions.firstOrNull()
            val shouldScrollTop = oldFirst != null && newFirst != null && oldFirst.barcode != newFirst.barcode

            _adapter.submitList(uiState.stockTransactions) {
                if (shouldScrollTop) binding.rvTransactions.smoothScrollToPosition(0)
            }

            if (!etQuantity.hasFocus() && etQuantity.text.toString() != uiState.quantity.toString()) {
                etQuantity.setTextIfChanged(uiState.quantity.toString())
            }
            binding.etQuantity.isEnabled = !uiState.singleQuantityStatus
        }

    }

    override fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.uiState.collectLatest { uiState ->
                    setupUnitsAdapter(uiState)
                    updateViews(uiState)
                }
            }
        }
    }

    override fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is UnplannedAcceptanceContract.Effect.ShowDocumentDialog -> {
                            dialogManager.showDialog(effect.docSeries, effect.docNumber)
                        }

                        is UnplannedAcceptanceContract.Effect.DismissDocumentDialog -> {
                            dialogManager.dismiss()
                        }

                        is UnplannedAcceptanceContract.Effect.RequestFocusOnBarcode -> {
                            binding.etBarcode.requestFocus()
                            binding.etBarcode.selectAll()
                        }

                        is UnplannedAcceptanceContract.Effect.RequestFocusOnQuantity -> {
                            binding.etQuantity.post {
                                binding.etQuantity.requestFocus()
                                binding.etQuantity.selectAll()
                                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.showSoftInput(binding.etQuantity, InputMethodManager.SHOW_IMPLICIT)
                            }
                        }

                        is UnplannedAcceptanceContract.Effect.ShowLoading -> {
                            binding.loadingContainer.visibility = View.VISIBLE
                        }

                        is UnplannedAcceptanceContract.Effect.DismissLoading -> {
                            binding.loadingContainer.visibility = View.GONE
                        }

                        is UnplannedAcceptanceContract.Effect.NavigateToMainMenu -> {
                            findNavController().popBackStack(R.id.goods_acceptance_operation_graph, inclusive = true)
                        }

                        is UnplannedAcceptanceContract.Effect.NavigateToSearchCompany -> {
                            findNavController().popBackStack()
                        }

                        is UnplannedAcceptanceContract.Effect.TriggerAutoSave -> {
                            val stockTxDoc = _viewModel.currentState.stockTxDoc
                            val currentAccCode = args.selectedCompany.currentCode
                            val barcodeDef = _viewModel.currentState.barcodeDef
                            val selectedUnit = _viewModel.currentState.selectedUnit
                            val qty = _viewModel.currentState.quantity
                            val user = args.loggedUser
                            _viewModel.setEvent(
                                UnplannedAcceptanceContract.Event.OnSaveAcceptance(
                                    stockTxDoc = stockTxDoc,
                                    currentAccCode = currentAccCode,
                                    barcodeDef = barcodeDef,
                                    unit = selectedUnit,
                                    quantity = qty,
                                    user = user
                                )
                            )
                            val imm =
                                requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

                        }

                    }
                }
            }
        }
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
                        val stockDocTx = _viewModel.currentState.stockTxDoc!!
                        val currentAccCode = args.selectedCompany.currentCode
                        _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnClickFinish(stockDocTx, currentAccCode))
                        true
                    }

                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onToolbarBackButtonPressed(): Boolean {
        val stockDocTx = _viewModel.currentState.stockTxDoc!!
        _viewModel.setEvent(UnplannedAcceptanceContract.Event.OnClickExit(stockDocTx))
        return true
    }

}