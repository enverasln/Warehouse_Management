package tr.com.cetinkaya.data_local

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_local.db.dao.TransferredDocumentDao
import tr.com.cetinkaya.data_local.db.entities.TransferredDocumentEntity
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_local.source.LocalTransferredDocumentDataSourceImpl
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel

@OptIn(ExperimentalCoroutinesApi::class)
class LocalTransferredDocumentDataSourceImplTest {
    private lateinit var dao: TransferredDocumentDao
    private lateinit var dataSource: LocalTransferredDocumentDataSourceImpl

    @Before
    fun setUp() {
        dao = mockk()
        dataSource = LocalTransferredDocumentDataSourceImpl(dao)
    }

    @Test
    fun `add calls dao add and returns row id`() = runTest {
        // Arrange
        val dataModel = TransferredDocumentDataModel(
            id = 0,
            transferredDocumentType = TransferredDocumentTypes.WarehouseShipmentDocument,
            documentSeries = "AA",
            documentNumber = 123,
            synchronizationStatus = false,
            description = "Açıklama"
        )
        val entity = dataModel.toEntity()
        coEvery { dao.add(entity) } returns 55L

        // Act
        val result = dataSource.add(dataModel)

        // Assert
        assertEquals(55L, result)
        coVerify(exactly = 1) { dao.add(entity) }
    }

    @Test
    fun `delete calls dao delete and returns affected row count`() = runTest {
        // Arrange
        coEvery { dao.delete("BB", 456, TransferredDocumentTypes.WarehouseShipmentDocument) } returns 1

        // Act
        val result = dataSource.delete(
            transferredDocumentTypes = TransferredDocumentTypes.WarehouseShipmentDocument,
            documentSeries = "BB",
            documentNumber = 456
        )

        // Assert
        assertEquals(1, result)
        coVerify(exactly = 1) { dao.delete("BB", 456, TransferredDocumentTypes.WarehouseShipmentDocument) }
    }

    @Test
    fun `getUntransferredDocuments returns mapped data models`() = runTest {
        // Arrange
        val entityList = listOf(
            TransferredDocumentEntity(
                id = 1,
                transferredDocumentType = TransferredDocumentTypes.WarehouseShipmentDocument,
                documentSeries = "ZZ",
                documentNumber = 789,
                synchronizationStatus = false,
                description = "desc"
            )
        )
        every { dao.getUntransferredDocuments() } returns flowOf(entityList)

        // Act
        val result = dataSource.getUntransferredDocuments().first()

        // Assert
        assertEquals(1, result.size)
        val first = result.first()
        assertEquals(entityList.first().id, first.id)
        assertEquals(entityList.first().documentSeries, first.documentSeries)
        assertEquals(entityList.first().description, first.description)
        // Diğer alanlar için de karşılaştırma eklenebilir
    }
}