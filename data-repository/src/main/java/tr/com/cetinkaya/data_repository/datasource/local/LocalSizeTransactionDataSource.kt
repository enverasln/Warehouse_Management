package tr.com.cetinkaya.data_repository.datasource.local

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.data_repository.models.size_transaction.AddSizeTransactionDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDocumentDataModel

interface LocalSizeTransactionDataSource {

    suspend fun insertOne(sizeTransaction: SizeTransactionDataModel): Long

    suspend fun addAll(sizeTransactions: List<AddSizeTransactionDataModel>): List<Long>

    suspend fun getAllByRefRecordIdAndSizeTransactionType(
        refRecordId: String,
        sizeTransactionType: SizeTransactionType
    ): List<SizeTransactionDataModel>?

    suspend fun getAllByStockTxDoc(stockTxDoc: StockTransactionDocumentDataModel): List<SizeTransactionDataModel>

    suspend fun markSizeTransactionAsSynced(sizeTxs: List<SizeTransactionDataModel>)

    suspend fun markPending(sizeTxs: List<SizeTransactionDataModel>)

}