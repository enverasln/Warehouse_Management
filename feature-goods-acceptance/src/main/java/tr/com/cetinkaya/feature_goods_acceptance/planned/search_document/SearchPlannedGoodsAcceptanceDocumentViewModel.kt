package tr.com.cetinkaya.feature_goods_acceptance.planned.search_document

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.domain.usecase.auth.GetLoggedUserUseCase
import tr.com.cetinkaya.domain.usecase.order.GetPlannedGoodsAcceptanceDocumentsUseCase
import tr.com.cetinkaya.feature_common.BaseViewModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.toUiModel
import javax.inject.Inject


@HiltViewModel
class SearchPlannedGoodsAcceptanceDocumentViewModel @Inject constructor(
    private val getPlannedGoodsAcceptanceDocumentsUseCase: GetPlannedGoodsAcceptanceDocumentsUseCase,
    private val getLoggedUserUseCase: GetLoggedUserUseCase
) : BaseViewModel<Event, State, Effect>() {

    override fun createInitialState(): State = State(
        documentsState = DocumentsState.Idle, loggedUser = null
    )

    init {
        fetchLoggedUser()
    }

    override fun handleEvent(event: Event) {
        when (event) {
            is Event.OnSearchButtonClick -> {
                val warehouseNumber = currentState.loggedUser?.warehouseNumber ?: return
                fetchDocuments(documentDate = event.documentDate, companyName = event.companyName, warehouseNumber = warehouseNumber)
            }

            is Event.OnDocumentSelected -> {

                updateDocuments {
                    if (it == event.selectedDocument) it.copy(isSelected = !it.isSelected) else it
                }
            }

            is Event.SelectAllDocuments -> updateDocuments { it.copy(isSelected = true) }

            is Event.DeselectAllDocuments -> updateDocuments { it.copy(isSelected = false) }

            is Event.OnBeginGoodsAcceptance -> handleBeginGoodsAcceptance()
        }
    }

    private fun fetchDocuments(documentDate: String, companyName: String, warehouseNumber: Int) {
        viewModelScope.launch {
            getPlannedGoodsAcceptanceDocumentsUseCase(
                GetPlannedGoodsAcceptanceDocumentsUseCase.Request(
                    warehouseNumber, companyName, documentDate
                )
            ).onStart {
                emit(Result.Loading)
            }.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val documents = result.data.documents.map { document ->
                            DocumentUiModel(
                                companyName = document.companyName,
                                companyCode = document.companyCode,
                                documentSeries = document.documentSeries,
                                documentNumber = document.documentNumber,
                                documentDate = DateConverter.timestampToUi(document.documentDate.time),
                                isSelected = false
                            )
                        }
                        setState { copy(documentsState = DocumentsState.Success(documents = documents)) }
                    }

                    is Result.Loading -> {
                        setState { copy(documentsState = DocumentsState.Loading) }
                    }

                    is Result.Error -> {
                        setEffect { Effect.ShowError(result.message) }
                        setState { copy(documentsState = DocumentsState.Idle) }
                    }
                }
            }
        }
    }

    private fun fetchLoggedUser() {
        viewModelScope.launch {
            getLoggedUserUseCase(GetLoggedUserUseCase.Request).onStart {
                emit(Result.Loading)
            }.collect { result ->
                when (result) {
                    is Result.Loading -> {
                        setState { copy(documentsState = DocumentsState.Loading) }
                    }

                    is Result.Success -> {
                        val userUiModel = result.data.user.toUiModel()
                        setState { copy(loggedUser = userUiModel, documentsState = DocumentsState.Idle) }
                    }

                    is Result.Error -> {
                        setEffect { Effect.ShowError(result.message) }
                        setState { copy(documentsState = DocumentsState.Idle) }
                    }
                }
            }
        }
    }

    private fun updateDocuments(transform: (DocumentUiModel) -> DocumentUiModel) {
        val docs = (currentState.documentsState as? DocumentsState.Success)?.documents ?: return
        val updatedDocs = docs.map { transform(it) }
        setState { copy(documentsState = DocumentsState.Success(updatedDocs)) }
    }

    private fun handleBeginGoodsAcceptance() {
        val docs = (currentState.documentsState as? DocumentsState.Success)?.documents ?: return
        val selectedDocs = docs.filter { it.isSelected }

        if (selectedDocs.isEmpty()) {
            setEffect { Effect.ShowValidationError("Lütfen en az bir belge seçiniz.") }
            return
        }

        val allSameCompany = selectedDocs.map { it.companyName }.distinct().size == 1

        if (!allSameCompany) {
            setEffect { Effect.ShowValidationError("Seçilen evraklar aynı firmaya ait olmalıdır.") }
            return
        }

        setEffect { Effect.NavigateGoodsAcceptance(selectedDocs.toSet()) }
    }
}



