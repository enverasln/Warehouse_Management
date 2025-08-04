package tr.com.cetinkaya.domain

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.GetUntransferredDocumentsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class GetUntransferredDocumentsUseCaseTest {

    private lateinit var repository: TransferredDocumentRepository
    private lateinit var useCase: GetUntransferredDocumentsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetUntransferredDocumentsUseCase(
            UseCase.Configuration(), repository
        )
    }


    @Test
    fun `get untransferred documents returns Result_Success with documents`() = runTest {
        val documents = listOf(
            TransferredDocumentDomainModel(
                id = 1,
                transferredDocumentType = TransferredDocumentTypes.WarehouseShipmentDocument,
                documentSeries = "AA",
                documentNumber = 123,
                synchronizationStatus = false,
                description = "Açıklama"
            ),
            TransferredDocumentDomainModel(
                id = 2,
                transferredDocumentType = TransferredDocumentTypes.WarehouseShipmentDocument,
                documentSeries = "AA",
                documentNumber = 124,
                synchronizationStatus = false,
                description = "Açıklama"
            ),
            TransferredDocumentDomainModel(
                id = 3,
                transferredDocumentType = TransferredDocumentTypes.WarehouseShipmentDocument,
                documentSeries = "AA",
                documentNumber = 125,
                synchronizationStatus = false,
                description = "Açıklama"
            )
        )
        every { repository.getUntransferredDocuments() } returns flowOf(documents)

        val result = useCase(GetUntransferredDocumentsUseCase.Request).first()

        assertTrue(result is Result.Success)
        val success = result as Result.Success
        assertEquals(documents, success.data.untransferredDocuments)
    }

    @Test
    fun `get untransferred documents emits Result_Error on exception`() = runTest {
        every { repository.getUntransferredDocuments() } returns flow { throw RuntimeException("Veri hatası") }

        val result = useCase(GetUntransferredDocumentsUseCase.Request).first()

        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertEquals("Veri hatası", error.message)
    }

}