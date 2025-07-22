package tr.com.cetinkaya.feature_goods_acceptance.planned.search_document

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.feature_common.BaseFragment
import tr.com.cetinkaya.feature_common.datepicker.DatePickerHelper
import tr.com.cetinkaya.feature_common.utils.DateMaskWatcher
import tr.com.cetinkaya.feature_goods_acceptance.R
import tr.com.cetinkaya.feature_goods_acceptance.databinding.FragmentSearchPlannedGoodsAcceptanceDocumentsBinding


@Suppress("UNCHECKED_CAST")
@AndroidEntryPoint
class SearchPlannedGoodsAcceptanceDocumentFragment : BaseFragment<FragmentSearchPlannedGoodsAcceptanceDocumentsBinding>() {

    override val bindLayout: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchPlannedGoodsAcceptanceDocumentsBinding
        get() = FragmentSearchPlannedGoodsAcceptanceDocumentsBinding::inflate

    private val _viewModel: SearchPlannedGoodsAcceptanceDocumentViewModel by viewModels()
    private lateinit var datePickerHelper: DatePickerHelper

    private val adapter = SearchPlannedGoodsAcceptanceDocumentAdapter {
        _viewModel.setEvent(Event.OnDocumentSelected(it))
    }


    @SuppressLint("SetTextI18n")
    override fun prepareView(savedInstanceState: Bundle?) {

        setupRecyclerView()
        setupInputs()
        setupButtons()
        setDefaultDocumentDate()
        observeState()
        observeEffect()

    }

    private fun setupRecyclerView() = binding.apply {
        rvDocuments.adapter = adapter
    }

    private fun setupInputs() = binding.apply {
        datePickerHelper = DatePickerHelper(parentFragmentManager, "planned_goods_document_search_date_picker")

        etDocumentDate.addTextChangedListener(DateMaskWatcher(binding.etDocumentDate))

        btnDatePicker.setOnClickListener {
            datePickerHelper.showDatePicker { formattedDate, _ ->
                setDocumentDate(formattedDate)
            }
        }
    }

    private fun setupButtons() = binding.apply {
        btnSearch.setOnClickListener {
            val documentDate = etDocumentDate.text.toString()
            val companyName = etCompanyName.text.toString()
            _viewModel.setEvent(Event.OnSearchButtonClick(documentDate = documentDate, companyName = companyName))
        }

        btnSelectAll.setOnClickListener {
            val allSelected = adapter.currentList.all { it.isSelected }
            _viewModel.setEvent(if (allSelected) Event.DeselectAllDocuments else Event.SelectAllDocuments)
        }

        btnBeginGoodsAcceptance.setOnClickListener {
            _viewModel.setEvent(Event.OnBeginGoodsAcceptance)
        }
    }


    private fun setDefaultDocumentDate() {
        val today = DateConverter.timestampToUi(System.currentTimeMillis())
        setDocumentDate(today)
    }

    private fun setDocumentDate(date: String) {
        binding.etDocumentDate.setText(date)
        binding.etDocumentDate.setSelection(date.length)
    }


    override fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                _viewModel.uiState.collect { uiState ->
                    when (val state = uiState.documentsState) {
                        is DocumentsState.Idle -> {
                            binding.tvEmptyState.visibility = View.GONE
                            binding.loadingContainer.visibility = View.GONE
                        }

                        is DocumentsState.Loading -> {
                            binding.tvEmptyState.visibility = View.VISIBLE
                            binding.loadingContainer.visibility = View.VISIBLE
                        }

                        is DocumentsState.Success -> {
                            if (state.documents.isEmpty()) {
                                binding.tvEmptyState.visibility = View.VISIBLE
                                binding.rvDocuments.visibility = View.GONE
                            } else {
                                binding.tvEmptyState.visibility = View.GONE
                                binding.rvDocuments.visibility = View.VISIBLE
                            }
                            binding.loadingContainer.visibility = View.GONE
                            adapter.submitList(state.documents)

                            binding.btnSelectAll.text = if (state.documents.all { it.isSelected }) getString(R.string.deselectAll)
                            else getString(R.string.selectAll)
                        }
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
                        is Effect.ShowError -> Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                        is Effect.ShowValidationError -> Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                        is Effect.NavigateGoodsAcceptance -> {
                            val action =
                                SearchPlannedGoodsAcceptanceDocumentFragmentDirections.actionSearchPlannedGoodsAcceptanceDocumentFragmentToPlannedGoodsAcceptanceContainerFragment(
                                    selectedDocuments = effect.documents.toTypedArray(), loggedUser = _viewModel.currentState.loggedUser
                                )
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
    }
}