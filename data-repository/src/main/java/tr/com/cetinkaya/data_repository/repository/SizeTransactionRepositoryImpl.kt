package tr.com.cetinkaya.data_repository.repository

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.data_repository.datasource.local.LocalSizeTransactionDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteSizeTransactionDataSource
import tr.com.cetinkaya.data_repository.models.size_transaction.toDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.toDomainModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.toDataModel
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.SizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import javax.inject.Inject

class SizeTransactionRepositoryImpl @Inject constructor(
    private val localSizeTransactionDataSource: LocalSizeTransactionDataSource,
    private val remoteSizeTransactionDataSource: RemoteSizeTransactionDataSource
) : SizeTransactionRepository {

    override suspend fun getAllByRefRecordIdAndSizeTransactionType(
        refRecordId: String, sizeTransactionType: SizeTransactionType
    ): List<SizeTransactionDomainModel>? {
        val sizeTransactions = localSizeTransactionDataSource.getAllByRefRecordIdAndSizeTransactionType(
            refRecordId = refRecordId, sizeTransactionType = sizeTransactionType
        )
        return sizeTransactions?.map { it.toDomainModel() }
    }

    override suspend fun getAllByStockTxDoc(stockTxDoc: StockTransactionDocumentDomainModel): List<SizeTransactionDomainModel> {
        val result = localSizeTransactionDataSource.getAllByStockTxDoc(stockTxDoc.toDataModel())
        return result.toDomainModel()
    }

    override suspend fun add(sizeTransaction: SizeTransactionDomainModel) {
        val toInsert = sizeTransaction.toDataModel()
        localSizeTransactionDataSource.insertOne(toInsert)
    }

    override suspend fun addAll(sizeTransactions: List<AddSizeTransactionDomainModel>): List<Long> {
        val toInsertRecords = sizeTransactions.toDataModel()
        return localSizeTransactionDataSource.addAll(toInsertRecords)
    }

    override suspend fun sendSizeTransaction(sizeTxs: List<SizeTransactionDomainModel>) : Boolean {
        val toSendSizeTransaction = sizeTxs.map { it.toDataModel() }
        return remoteSizeTransactionDataSource.sendSizeTransaction(toSendSizeTransaction)
    }

    override suspend fun markSizeTxAsSynced(sizeTxs: List<SizeTransactionDomainModel>) {
        localSizeTransactionDataSource.markSizeTransactionAsSynced(sizeTxs.toDataModel())
    }

    override suspend fun markPending(sizeTxs: List<SizeTransactionDomainModel>) {
        localSizeTransactionDataSource.markPending(sizeTxs.toDataModel())
    }
}