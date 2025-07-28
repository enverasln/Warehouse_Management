package tr.com.cetinkaya.data_local.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.data_local.db.dao.StockTransactionDao
import tr.com.cetinkaya.data_local.db.entities.toDataModel
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_local.models.stok_transaction.toDataModel
import tr.com.cetinkaya.data_repository.datasource.local.LocalStockTransactionDataSource
import tr.com.cetinkaya.data_repository.models.stocktransaction.GetStockTransactionsByDocumentDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel
import javax.inject.Inject

class LocalStockTransactionDataSourceImpl @Inject constructor(
    private val stockTransactionDao: StockTransactionDao
) : LocalStockTransactionDataSource {

    override suspend fun addStockTransaction(stockTransaction: StockTransactionDataModel) {
        val stockTransactionLocalModel = stockTransaction.toEntity()

        stockTransactionDao.add(stockTransactionLocalModel)
    }

    override suspend fun getCountByDocuments(
        stockTransactionDocument: StockTransactionDocumentDataModel
    ): Long = stockTransactionDao.getCountByDocument(
        documentSeries = stockTransactionDocument.documentSeries,
        documentNumber = stockTransactionDocument.documentNumber,
        transactionType = stockTransactionDocument.transactionType,
        transactionKind = stockTransactionDocument.transactionKind,
        isNormalOrReturn = stockTransactionDocument.isNormalOrReturn,
        documentType = stockTransactionDocument.documentType,
    )

    override fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: Byte, transactionKind: Byte, isNormalOrReturn: Byte, documentType: Byte, documentSeries: String, documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentDataModel>> = stockTransactionDao.getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType = transactionType,
        transactionKind = transactionKind,
        isNormalOrReturn = isNormalOrReturn,
        documentType = documentType,
        documentSeries = documentSeries,
        documentNumber = documentNumber
    ).map {
        it.map { getStockTransactionsByDocumentLocalModel ->
            getStockTransactionsByDocumentLocalModel.toDataModel()
        }

    }

    override suspend fun getStockTransactionByBarcode(
        barcode: String, documentSeries: String, documentNumber: Int
    ): StockTransactionDataModel? {
        val stockTransaction = stockTransactionDao.getStockTransactionByBarcode(
            barcode = barcode, documentSeries = documentSeries, documentNumber = documentNumber
        )
        return stockTransaction?.toDataModel()
    }

    override suspend fun updateStockTransaction(stockTransaction: StockTransactionDataModel): Int {
        val stockTransactionEntity = stockTransaction.toEntity()
        return stockTransactionDao.updateStockTransaction(stockTransactionEntity)
    }

    override suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String) {
        stockTransactionDao.updateStockTransactionSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    override fun getUnsyncedStockTransactions(): Flow<List<StockTransactionDataModel>> {
        return stockTransactionDao.getBySyncStatus("Aktarılacak").map {
            it.map { data -> data.toDataModel() }
        }
    }

    override fun getStockTransactionsByDocument(
        transactionType: Byte, transactionKind: Byte, isNormalOrReturn: Byte, documentType: Byte, documentSeries: String, documentNumber: Int
    ): Flow<List<StockTransactionDataModel>> {
        return stockTransactionDao.getStockTransactionsByDocument(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        ).map {
            it.map { data -> data.toDataModel() }
        }
    }
}