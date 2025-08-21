package tr.com.cetinkaya.domain

import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.repository.DocumentSyncRepository
import tr.com.cetinkaya.domain.usecase.UseCase
import tr.com.cetinkaya.domain.usecase.transferred_document.synchronization.SyncAllDocumentsUseCase
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SyncAllDocumentUseCaseTest {

    private lateinit var repository: DocumentSyncRepository
    private lateinit var useCase: SyncAllDocumentsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SyncAllDocumentsUseCase(
            UseCase.Configuration(), repository
        )
    }

    @Test
    fun `should synchronize unsynced documents and emit progress steps`() = runTest {
        val orderId = UUID.randomUUID().toString()
        val order = OrderDomainModel(
            id = orderId,
            orderDate = "2023-05-01",
            documentSeries = "DUD",
            documentNumber = 1,
            lineNumber = 0,
            stockCode = "0000001",
            currentCode = "0000001",
            quantity = 15.0,
            inputWarehouseNumber = 2,
            outputWarehouseNumber = 2,
            salesman = "Enver ASLAN",
            responsibilityCenter = "",
            userCode = 100,
            totalPrice = 15000.0,
            discount1 = 0.0,
            discount2 = 0.0,
            discount3 = 0.0,
            discount4 = 0.0,
            discount5 = 0.0,
            vatPointer = 1,
            orderId = "",
            price = 1000.0,
            paperNumber = "",
            companyNumber = 1,
            storeNumber = 1,
            barcode = "0000000000001",
            isColoredAndSized = false
        )

        val stockTransient = StockTransactionDomainModel(
            id = UUID.randomUUID().toString(),
            transactionType = StockTransactionTypes.Input,
            transactionKind = StockTransactionKinds.Wholesale,
            isNormalOrReturn = 0,
            documentType = StockTransactionDocumentTypes.EntryDispatchNote,
            documentDate = Calendar.getInstance().time.time,
            documentSeries = "DUD",
            documentNumber = 1,
            lineNumber = 0,
            stockCode = "0000001",
            stockName = "Product 1",
            companyCode = "0000001",
            quantity = 5.0,
            inputWarehouseNumber = 2,
            outputWarehouseNumber = 2,
            paymentPlanNumber = 0,
            salesman = "Enver ASLAN",
            responsibilityCenter = "",
            userCode = 100,
            totalPrice = 15000.0,
            discount1 = 0.0,
            discount2 = 0.0,
            discount3 = 0.0,
            discount4 = 0.0,
            discount5 = 0.0,
            taxPointer = 1,
            orderId = orderId,
            price = 1000.0,
            paperNumber = "DUD1",
            companyNumber = 0,
            storeNumber = 0,
            barcode = "0000000000001",
            isColoredAndSized = false,
            transportationStatus = 0,
            createdAt = Calendar.getInstance().time.time,
            updatedAt = Calendar.getInstance().time.time,
            synchronizationStatus = "Aktarılacak"
        )



    }
}