package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.order.GetNextDocumentSeriesAndNumberDomainModel
import tr.com.cetinkaya.domain.model.order.GetProductByBarcodeDomainModel
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.model.order.ProductDomainModel

interface OrderRepository {
    fun getPlannedGoodsAcceptanceDocuments(warehouseNumber: Int, companyName: String, documentDate: String): Flow<List<DocumentDomainModel>>

    fun getPlannedGoodsAcceptanceProducts(documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<List<ProductDomainModel>>

    fun getProductByBarcode(barcode: String, documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<GetProductByBarcodeDomainModel>

    fun observeLocalPlannedGoodsAcceptanceProducts(documents: List<Pair<String, Int>>, warehouseNumber: Int): Flow<List<ProductDomainModel>>

    suspend fun syncPlannedGoodsAcceptanceProducts(documents: List<Pair<String, Int>>, warehouseNumber: Int)

    suspend fun addOrder(
        newOrderDocumentSeries: String,
        newOrderDocumentNumber: Int,
        barcode: String,
        quantity: Double,
        warehouseNumber: Int,
        documents: List<Pair<String, Int>>
    )

    suspend fun getNextDocumentSeriesAndNumber(
        orderType: OrderTransactionTypes,
        orderKind: OrderTransactionKinds,
        documentSeries: String
    ): GetNextDocumentSeriesAndNumberDomainModel

    suspend fun updateOrderSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String)

    fun getUnsyncedOrders(): Flow<List<OrderDomainModel>>

    suspend fun sendOrder(order: OrderDomainModel): Boolean

    suspend fun isDocumentUsed(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String, documentNumber: Int
    ): Boolean

    suspend fun getUnsyncedOrdersByDocument(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String, documentNumber: Int
    ): List<OrderDomainModel>

    suspend fun getNextAvailableDocumentNumber(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String
    ): Int

    suspend fun markOrderTransactionSynced(order: OrderDomainModel)

    suspend fun updateOrderDocumentNumber(
        transactionType: OrderTransactionTypes,
        transactionKind: OrderTransactionKinds,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    )
}