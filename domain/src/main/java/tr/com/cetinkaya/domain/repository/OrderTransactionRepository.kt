package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.order.GetNextDocumentSeriesAndNumberDomainModel
import tr.com.cetinkaya.domain.model.order.GetProductByBarcodeDomainModel
import tr.com.cetinkaya.domain.model.order.ProductDomainModel
import tr.com.cetinkaya.domain.model.order_transaction.AddOrderTransactionDomainModel
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel

interface OrderTransactionRepository {

    suspend fun addWithSizeTransactions(orderTx: AddOrderTransactionDomainModel, sizeTx: List<AddSizeTransactionDomainModel>)
    suspend fun update(orderTx: OrderTransactionDomainModel)
    fun getPlannedGoodsAcceptanceDocuments(warehouseNumber: Int, companyName: String, documentDate: String): Flow<List<DocumentDomainModel>>
    fun getPlannedGoodsAcceptanceProducts(documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<Unit>
    fun getProductByBarcode(barcode: String, documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<GetProductByBarcodeDomainModel>
    fun getOrderTxsByDocuments(documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<List<OrderTransactionDomainModel>>
    suspend fun getOrderTransactionsByBarcodeAndDocuments(
        barcode: String, orderTxDocuments: List<Pair<String, Int>>, warehouseNumber: Int
    ): List<OrderTransactionDomainModel>

    fun observeLocalPlannedGoodsAcceptanceProducts(documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<List<ProductDomainModel>>
    fun fetchAndSaveOrderTransactions(documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<Unit>

    suspend fun getNextDocumentSeriesAndNumber(
        orderType: OrderTransactionTypes, orderKind: OrderTransactionKinds, documentSeries: String
    ): OrderTransactionDocumentDomainModel

    suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String)
    fun getUnsyncedOrders(): Flow<List<OrderTransactionDomainModel>>
    suspend fun sendOrder(orderTx: OrderTransactionDomainModel): Boolean
    suspend fun isDocumentAvailable(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String, documentNumber: Int
    ): Boolean

    suspend fun getUnsyncedOrdersByDocument(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, docSeries: String, docNumber: Int
    ): List<OrderTransactionDomainModel>

    suspend fun getNextAvailableDocumentNumber(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String
    ): Int

    suspend fun markOrderTransactionSynced(orderTx: OrderTransactionDomainModel)
    suspend fun updateOrderDocumentNumber(
        transactionType: OrderTransactionTypes,
        transactionKind: OrderTransactionKinds,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    )

    suspend fun countByDocumentSeriesAndNumber(documentSeries: String, documentNumber: Int): Int

    suspend fun markPending(orderTxDoc: OrderTransactionDocumentDomainModel) : Int
}