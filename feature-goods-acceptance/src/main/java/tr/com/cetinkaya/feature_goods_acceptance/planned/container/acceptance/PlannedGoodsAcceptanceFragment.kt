package tr.com.cetinkaya.feature_goods_acceptance.planned.container.acceptance

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_common.snackbar.showErrorSnackbar
import tr.com.cetinkaya.feature_common.snackbar.showSuccessSnackbar
import tr.com.cetinkaya.feature_goods_acceptance.R
import tr.com.cetinkaya.feature_goods_acceptance.databinding.FragmentPlannedGoodsAcceptanceBinding
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.PlannedGoodsAcceptanceContainerContract
import tr.com.cetinkaya.feature_goods_acceptance.planned.container.PlannedGoodsAcceptanceContainerViewModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.stock_transaction.StockTransactionDocumentUiModel
import kotlin.math.log

@AndroidEntryPoint
class PlannedGoodsAcceptanceFragment : BaseFragment<FragmentPlannedGoodsAcceptanceBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPlannedGoodsAcceptanceBinding
        get() = FragmentPlannedGoodsAcceptanceBinding::inflate

    private val _sharedViewModel: PlannedGoodsAcceptanceContainerViewModel by hiltNavGraphViewModels(R.id.goods_acceptance_operation_graph)

    private val _viewModel: PlannedGoodsAcceptanceViewModel by viewModels()

    private val _adapter = PlannedGoodsAcceptanceAdapter()

    @OptIn(FlowPreview::class)
    override fun prepareView(savedInstanceState: Bundle?) {

        binding.rvTransactions.adapter = _adapter

        val sharedState = _sharedViewModel.currentState
        binding.etCompanyName.setText(sharedState.companyName)

        binding.cbSingleQuantity.setOnCheckedChangeListener { _, isChecked ->
            _viewModel.setEvent(PlannedGoodsAcceptanceContract.Event.OnChangeSingleQuantityChecked(isChecked))
        }

        binding.btnSave.setOnClickListener {
            val barcode = binding.etBarcode.text.toString().trim()
            val requiredQuantity = binding.etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val selectedDocuments = _sharedViewModel.currentState.selectedDocuments
            val stockTransactionDocument = _sharedViewModel.currentState.stockTransactionDocument ?: return@setOnClickListener
            val loggedUser = _sharedViewModel.currentState.loggedUser ?: return@setOnClickListener

            _viewModel.setEvent(
                PlannedGoodsAcceptanceContract.Event.OnSaveQuantityWithCheck(
                    barcode = barcode,
                    deliveredQuantity = requiredQuantity,
                    selectedDocuments = selectedDocuments,
                    stockTransactionDocument = stockTransactionDocument,
                    loggedUser = loggedUser
                )
            )
        }

        binding.etBarcode.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val barcode = binding.etBarcode.text.toString().trim()
                val selectedDocuments = _sharedViewModel.currentState.selectedDocuments
                val warehouseNumber = _sharedViewModel.currentState.loggedUser?.warehouseNumber ?: return@setOnEditorActionListener false
                if (barcode.isNotEmpty()) {
                    _viewModel.setEvent(
                        PlannedGoodsAcceptanceContract.Event.OnFetchProduct(
                            barcode = barcode, selectedDocuments = selectedDocuments, warehouseNumber = warehouseNumber
                        )
                    )
                }
                true
            } else {
                false
            }
        }

        binding.etBarcode.setOnFocusChangeListener { view, hasFocus ->

            if (!hasFocus) {
                val barcode = binding.etBarcode.text.toString().trim()
                val selectedDocuments = _sharedViewModel.currentState.selectedDocuments
                val warehouseNumber = _sharedViewModel.currentState.loggedUser?.warehouseNumber ?: return@setOnFocusChangeListener

                if (barcode.isNotEmpty()) {
//                    _sharedViewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.OnBarcodeEntered(barcode))
                    _viewModel.setEvent(

                        PlannedGoodsAcceptanceContract.Event.OnFetchProduct(
                            barcode = barcode, selectedDocuments = selectedDocuments, warehouseNumber = warehouseNumber
                        )
                    )
                }
            }
        }

        binding.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val quantity = s.toString().trim().toDoubleOrNull() ?: 0.0
                _viewModel.setEvent(PlannedGoodsAcceptanceContract.Event.OnDeliveredQuantityChanged(quantity))
            }
        })
    }

    override fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    _sharedViewModel.uiState.collect { uiState ->
                        if (_viewModel.currentState.fetchedProduct?.barcode != uiState.tappedBarcode) {
                            val selectedDocuments = _sharedViewModel.currentState.selectedDocuments
                            val warehouseNumber = _sharedViewModel.currentState.loggedUser?.warehouseNumber ?: return@collect
                            if (uiState.tappedBarcode.isNotEmpty()) {
                                _viewModel.setEvent(

                                    PlannedGoodsAcceptanceContract.Event.OnFetchProduct(
                                        barcode = uiState.tappedBarcode, selectedDocuments = selectedDocuments, warehouseNumber = warehouseNumber
                                    )
                                )
                            }
                        }
                    }
                }

                launch {
                    _viewModel.uiState.collect { uiState ->
                        if (uiState.fetchedProduct != null) {
                            if (uiState.fetchedProduct.barcode != binding.etBarcode.text.toString().trim()) {
                                binding.etBarcode.setText(uiState.fetchedProduct.barcode)
                            }
                            binding.etStockName.setText(uiState.fetchedProduct.stockName)
                            if (!binding.etQuantity.hasFocus()) {
                                val currentText = binding.etQuantity.text.toString()
                                val newText = uiState.deliveredQuantity.toString()
                                if (currentText != newText) {
                                    binding.etQuantity.setText(newText)
                                    binding.etQuantity.setSelection(newText.length)
                                }
                            }

                        } else {
                            binding.etBarcode.setText("")
                            binding.etStockName.setText("")
                            binding.etQuantity.setText("0.0")
                        }
                        _adapter.submitList(uiState.stockTransactions)

                        binding.etQuantity.isEnabled = !uiState.isSingleQuantity
                    }

                }

            }
        }
    }

    override fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is PlannedGoodsAcceptanceContract.Effect.ShowWarning -> {
                            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.dialog_title_warning)).setMessage(effect.message)
                                .setPositiveButton(getString(R.string.dialog_button_ok)) { _, _ ->
                                    binding.etBarcode.requestFocus()
                                    binding.etBarcode.selectAll()
                                }.show()
                        }

                        is PlannedGoodsAcceptanceContract.Effect.ShowSuccess -> {
                            binding.root.showSuccessSnackbar(effect.message)
                            _sharedViewModel.currentState.stockTransactionDocument
                            _viewModel.setEvent(PlannedGoodsAcceptanceContract.Event.OnFetchStockTransaction(_sharedViewModel.currentState.stockTransactionDocument))
                            _sharedViewModel.setState { copy(tappedBarcode = "") }
                            _sharedViewModel.setEvent(PlannedGoodsAcceptanceContainerContract.Event.FetchProducts)
                            binding.etBarcode.requestFocus()
                        }

                        is PlannedGoodsAcceptanceContract.Effect.ShowError -> {
                            binding.root.showErrorSnackbar(effect.message)
                        }

                        PlannedGoodsAcceptanceContract.Effect.ShowOverQuantityDialog -> {
                            MaterialAlertDialogBuilder(requireContext()).setTitle("Uyarı")
                                .setMessage("Girilen miktar kalan miktardan fazla. Fazla olan miktar için yeni sipariş kaydı oluşturulacaktır.İşlemi onaylıyor musunuz?")
                                .setPositiveButton("Evet") { _, _ ->
                                    val selectedDocuments = _sharedViewModel.currentState.selectedDocuments
                                    val loggedUser = _sharedViewModel.currentState.loggedUser ?: return@setPositiveButton
                                    val stockTransactionDocument = _sharedViewModel.currentState.stockTransactionDocument ?: return@setPositiveButton
                                    _viewModel.setEvent(PlannedGoodsAcceptanceContract.Event.OnUseConfirmedOverQuantity(selectedDocuments, loggedUser, stockTransactionDocument))
                                }.setNegativeButton("Hayır") { _, _ ->

                                }.show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun onStartGoodsAcceptance() {
        binding.etBarcode.requestFocus()
        binding.etBarcode.setText("")
        val sharedState = _sharedViewModel.currentState
        val loggedUser = sharedState.loggedUser ?: return
        _viewModel.setEvent(PlannedGoodsAcceptanceContract.Event.OnFetchNextDocument(OrderTransactionTypes.Supply, OrderTransactionKinds.NormalOrder, loggedUser.newDocumentSeries))
        _viewModel.setEvent(PlannedGoodsAcceptanceContract.Event.OnFetchStockTransaction(sharedState.stockTransactionDocument))
    }

    fun onUpdateOrderSyncStatus() {
        _viewModel.setEvent(PlannedGoodsAcceptanceContract.Event.OnUpdateOrderSyncStatus)
    }
}