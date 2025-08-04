package tr.com.cetinkaya.domain

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tr.com.cetinkaya.common.Result
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.AddTransferredDocumentUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransferredDocumentUseCaseTest {
    private lateinit var repository: TransferredDocumentRepository
    private lateinit var useCase: AddTransferredDocumentUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = AddTransferredDocumentUseCase(
            UseCase.Configuration(), repository
        )
    }

    @Test
    fun `add transferred document return inserted id wrapper interface Result_Success`() = runTest {
        // Arrange
        val expectedId = 42L
        val request = AddTransferredDocumentUseCase.Request(
            transferredDocumentTypes = TransferredDocumentTypes.WarehouseShipmentDocument,
            documentSeries = "TEST",
            documentNumber = 123,
            synchronizationStatus = false,
            description = "Aktarılacak"
        )

        coEvery {
            repository.add(
                request.transferredDocumentTypes,
                request.documentSeries,
                request.documentNumber,
                request.synchronizationStatus,
                request.description
            )
        } returns expectedId

        // Act
        val result = useCase(request).first()

        // Assert
        assertTrue(result is Result.Success)
        val success = result as Result.Success
        assertEquals(42L, success.data.id)
    }


    @Test
    fun `add transferred document emits Result_Error on repository exception`() = runTest {
        val request = AddTransferredDocumentUseCase.Request(
            transferredDocumentTypes = TransferredDocumentTypes.WarehouseShipmentDocument,
            documentSeries = "AA",
            documentNumber = 123,
            synchronizationStatus = false,
            description = "Açıklama"
        )

        coEvery { repository.add(any(), any(), any(), any(), any()) } throws RuntimeException("Hata var")

        val result = useCase(request).first()

        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertEquals("Hata var", error.message)
    }
}
