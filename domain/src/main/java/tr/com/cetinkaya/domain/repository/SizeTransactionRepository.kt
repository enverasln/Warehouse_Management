package tr.com.cetinkaya.domain.repository

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.SizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel

interface SizeTransactionRepository {
    suspend fun getAllByRefRecordIdAndSizeTransactionType(
        refRecordId: String, sizeTransactionType: SizeTransactionType
    ): List<SizeTransactionDomainModel>?

    suspend fun getAllByStockTxDoc(stockTxDoc: StockTransactionDocumentDomainModel): List<SizeTransactionDomainModel>
    suspend fun add(sizeTransaction: SizeTransactionDomainModel)
    suspend fun addAll(sizeTransactions: List<AddSizeTransactionDomainModel>): List<Long>
    suspend fun sendSizeTransaction(sizeTxs: List<SizeTransactionDomainModel>): Boolean
    suspend fun markSizeTxAsSynced(sizeTxs: List<SizeTransactionDomainModel>)
    suspend fun markPending(sizeTxs: List<SizeTransactionDomainModel>)
}