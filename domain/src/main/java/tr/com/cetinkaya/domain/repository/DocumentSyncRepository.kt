package tr.com.cetinkaya.domain.repository

import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel

interface DocumentSyncRepository {
    suspend fun getUnsyncedDocuments(): List<TransferredDocumentDomainModel>
    suspend fun isOrderDocumentUsed(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String, documentNumber: Int
    ): Boolean
    suspend fun getUnsyncedOrders(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String, documentNumber: Int
    ): List<OrderDomainModel>
    suspend fun syncOrder(order: OrderDomainModel): Boolean
    suspend fun markOrderTransactionSynced(
        orderTransactionTypes: OrderTransactionTypes, orderTransactionKinds: OrderTransactionKinds, documentSeries: String, documentNumber: Int
    )
    suspend fun markTransferredDocumentSynced(transferredDocumentTypes: TransferredDocumentTypes, documentSeries: String, documentNumber: Int)
    suspend fun getMaxOrderNumber(transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String): Int
    suspend fun changeOrderNumber(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String, oldDocumentNumber: Int, newDocumentNumber:Int
    )
    suspend fun changeTransferredDocumentNumber(
        transferredDocumentTypes: TransferredDocumentTypes, documentSeries: String, oldDocumentNumber: Int, newDocumentNumber: Int

    )

    suspend fun isStockTransactionDocumentUsed(): Boolean
    suspend fun getMaxStockTransactionDocumentNumber(documentSeries: String): Int


    suspend fun syncStockTransaction(transaction: StockTransactionDomainModel): Boolean
    suspend fun markStockTransactionSynced(documentSeries: String, documentNumber: Int)

}