package tr.com.cetinkaya.data_repository


import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_repository.datasource.local.LocalTransferredDocumentDataSource
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel
import tr.com.cetinkaya.data_repository.repository.TransferredDocumentRepositoryImpl

@OptIn(ExperimentalCoroutinesApi::class)
class TransferredDocumentRepositoryImplTest {

    private lateinit var dataSource: LocalTransferredDocumentDataSource
    private lateinit var repository: TransferredDocumentRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk()
        repository = TransferredDocumentRepositoryImpl(dataSource)
    }

    @Test
    fun `add calls dataSource add and returns id`() = runTest {
        // Arrange
        val expectedId = 100L
        val dataModel = TransferredDocumentDataModel(
            id = 0,
            transferredDocumentType = TransferredDocumentTypes.WAREHOUSE_TRANSFER,
            documentSeries = "AA",
            documentNumber = 123,
            synchronizationStatus = false,
            description = "Açıklama"
        )
        coEvery { dataSource.add(any()) } returns expectedId

        // Act
        val actualId = repository.add(
            transferredDocumentType = TransferredDocumentTypes.WAREHOUSE_TRANSFER,
            documentSeries = "AA",
            documentNumber = 123,
            synchronizationStatus = false,
            description = "Açıklama"
        )

        // Assert
        assertEquals(expectedId, actualId)
        coVerify(exactly = 1) {
            dataSource.add(match {
                it.transferredDocumentType == dataModel.transferredDocumentType &&
                        it.documentSeries == dataModel.documentSeries &&
                        it.documentNumber == dataModel.documentNumber &&
                        it.synchronizationStatus == dataModel.synchronizationStatus &&
                        it.description == dataModel.description
            })
        }
    }

    @Test
    fun `delete calls dataSource delete and returns affected row count`() = runTest {
        // Arrange
        coEvery { dataSource.delete(any(), any(), any()) } returns 1

        // Act
        val result = repository.delete(
            transferredDocumentTypes = TransferredDocumentTypes.WAREHOUSE_TRANSFER,
            documentSeries = "BB",
            documentNumber = 456
        )

        // Assert
        assertEquals(1, result)
        coVerify(exactly = 1) { dataSource.delete(TransferredDocumentTypes.WAREHOUSE_TRANSFER, "BB", 456) }
    }

    @Test
    fun `getUntransferredDocuments returns mapped domain models`() = runTest {
        // Arrange
        val dataModels = listOf(
            TransferredDocumentDataModel(
                id = 1,
                transferredDocumentType = TransferredDocumentTypes.WAREHOUSE_TRANSFER,
                documentSeries = "XX",
                documentNumber = 789,
                synchronizationStatus = false,
                description = "desc"
            )
        )
        every { dataSource.getUntransferredDocuments() } returns flowOf(dataModels)

        // Act
        val domainModels = repository.getUntransferredDocuments().first()

        // Assert
        assertEquals(1, domainModels.size)
        val model = domainModels.first()
        assertEquals(dataModels.first().id, model.id)
        assertEquals(dataModels.first().documentSeries, model.documentSeries)
        assertEquals(dataModels.first().description, model.description)
        // ve diğer alanlar...
    }
}