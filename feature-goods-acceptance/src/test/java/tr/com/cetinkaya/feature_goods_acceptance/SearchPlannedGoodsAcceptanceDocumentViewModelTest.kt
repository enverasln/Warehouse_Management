package tr.com.cetinkaya.feature_goods_acceptance

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.usecase.auth.GetLoggedUserUseCase
import tr.com.cetinkaya.domain.usecase.order.GetPlannedGoodsAcceptanceDocumentsUseCase
import tr.com.cetinkaya.feature_goods_acceptance.planned.search_document.DocumentsState
import tr.com.cetinkaya.feature_goods_acceptance.planned.search_document.Effect
import tr.com.cetinkaya.feature_goods_acceptance.planned.search_document.Event
import tr.com.cetinkaya.feature_goods_acceptance.planned.search_document.SearchPlannedGoodsAcceptanceDocumentViewModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.DocumentUiModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.order.toDomainModel
import tr.com.cetinkaya.feature_goods_acceptance.planned.models.user.UserUiModel

@OptIn(ExperimentalCoroutinesApi::class)
class SearchPlannedGoodsAcceptanceDocumentViewModelTest {

    private val getDocumentsUseCase: GetPlannedGoodsAcceptanceDocumentsUseCase = mockk()
    private val getLoggedUserUseCase: GetLoggedUserUseCase = mockk(relaxed = true)
    private lateinit var viewModel: SearchPlannedGoodsAcceptanceDocumentViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        every { getLoggedUserUseCase(any()) } returns flow {
            emit(Result.Success(GetLoggedUserUseCase.Response(FakeModels.loggedUserDomainModel)))
        }

        viewModel = SearchPlannedGoodsAcceptanceDocumentViewModel(
            getDocumentsUseCase,
            getLoggedUserUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state contains loggedUser after fetch`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(FakeModels.loggedUserUiModel, state.loggedUser)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnSearchButtonClick triggers Loading and Success state`() = runTest {
        // Arrange
        coEvery {
            getDocumentsUseCase(any())
        } returns flow {
            emit(Result.Loading)
            emit(Result.Success(FakeModels.fakeDocumentsResponse))
        }

        // Act
        viewModel.setEvent(Event.OnSearchButtonClick("2025-06-25", "ABC"))

        // Assert (sadece documentsState'e odaklan)
        viewModel.uiState
            .map { it.documentsState } // sadece documentsState kısmını alıyoruz
            .filterNotNull() // null olanlar gelmesin
            .test {
                val loadingState = awaitItem()
                assertTrue(loadingState is DocumentsState.Loading)

                val successState = awaitItem()
                assertTrue(successState is DocumentsState.Success)

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun `OnBeginGoodsAcceptance with no selection triggers validation error`() = runTest {
        viewModel.setState {
            copy(documentsState = DocumentsState.Success(emptyList()))
        }

        viewModel.setEvent(Event.OnBeginGoodsAcceptance)

        viewModel.effect.test {
            val effect = awaitItem()
            assertTrue(effect is Effect.ShowValidationError)
            assertEquals("Lütfen en az bir belge seçiniz.", (effect as Effect.ShowValidationError).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnBeginGoodsAcceptance with mixed companies triggers validation error`() = runTest {
        val docs = listOf(
            FakeModels.documentUiModel.copy(isSelected = true, companyName = "Firma A"),
            FakeModels.documentUiModel.copy(isSelected = true, companyName = "Firma B")
        )

        viewModel.setState {
            copy(documentsState = DocumentsState.Success(docs))
        }

        viewModel.setEvent(Event.OnBeginGoodsAcceptance)

        viewModel.effect.test {
            val effect = awaitItem()
            assertTrue(effect is Effect.ShowValidationError)
            assertEquals("Seçilen evraklar aynı firmaya ait olmalıdır.", (effect as Effect.ShowValidationError).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnBeginGoodsAcceptance with valid selection triggers navigation effect`() = runTest {
        val docs = listOf(
            FakeModels.documentUiModel.copy(isSelected = true),
            FakeModels.documentUiModel.copy(isSelected = true)
        )

        viewModel.setState {
            copy(documentsState = DocumentsState.Success(docs))
        }

        viewModel.setEvent(Event.OnBeginGoodsAcceptance)

        viewModel.effect.test {
            val effect = awaitItem()
            assertTrue(effect is Effect.NavigateGoodsAcceptance)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

object FakeModels {
    val loggedUserDomainModel = UserDomainModel(
        username = "test",
        email = "test@test.com",
        warehouseName = "Main",
        warehouseNumber = 1,
        mikroFlyUserId = 123,
        documentSeries = "AB",
        newDocumentSeries = "AB"
    )

    val loggedUserUiModel = UserUiModel(
        username = "test",
        email = "test@test.com",
        warehouseName = "Main",
        warehouseNumber = 1,
        mikroFlyUserId = 123,
        documentSeries = "AB",
        newDocumentSeries = "AB"
    )

    val documentUiModel = DocumentUiModel(
        companyName = "ACME",
        companyCode = "001",
        documentSeries = "A",
        documentNumber = 1,
        documentDate = "25.06.2025",
        isSelected = false
    )

    val fakeDocumentsResponse = GetPlannedGoodsAcceptanceDocumentsUseCase.Response(
        documents = listOf(
            documentUiModel.toDomainModel().copy(warehouseNumber = 1),
            documentUiModel.copy(documentNumber = 2).toDomainModel().copy(warehouseNumber = 1)
        )
    )
}