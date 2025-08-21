package tr.com.cetinkaya.data_local.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.data_local.db.dao.StockTransactionDao
import tr.com.cetinkaya.data_local.db.entities.toDataModel
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_local.models.stok_transaction.toDataModel
import tr.com.cetinkaya.data_repository.datasource.local.LocalStockTransactionDataSource
import tr.com.cetinkaya.data_repository.models.stocktransaction.GetStockTransactionsByDocumentDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel
import java.util.Date
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
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int
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

    override suspend fun updateStockTransactionSyncStatus(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: String
    ): Int {
        return stockTransactionDao.updateStockTransactionSyncStatus(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            syncStatus = syncStatus,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        )
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
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int
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

    override fun getNextStockTransactionDocument(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isStockTransactionNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel> {
        return stockTransactionDao.getNextStockTransactionDocument(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isStockTransactionNormalOrReturn = isStockTransactionNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries
        ).map { document ->


            var nextDocument = StockTransactionDocumentDataModel(
                documentDate = Date().time,
                documentSeries = documentSeries,
                documentNumber = 1,
                paperNumber = "",
                transactionType = transactionType,
                transactionKind = transactionKind,
                isNormalOrReturn = isStockTransactionNormalOrReturn,
                documentType = documentType
            )

            if (document != null) {
                nextDocument = nextDocument.copy(documentNumber = document.documentNumber + 1)
            }

            nextDocument

        }
    }

    override suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String
    ): Int {
        val documentNumber = stockTransactionDao.getNextAvailableDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries
        )

        return if(documentNumber == null) 1 else documentNumber + 1
    }

    override suspend fun markStockTransactionSynced(stockTransaction: StockTransactionDataModel) {
        stockTransactionDao.markStockTransactionSynced(stockTransaction.id, stockTransaction.barcode)
    }

    override suspend fun getUnsyncedStockTransactions(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int
    ): List<StockTransactionDataModel> {
        return stockTransactionDao.getUnsyncedStockTransactions(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = transactionDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        ).map {
            it.toDataModel()
        }
    }

    override suspend fun updateDocumentNumber(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    ) {
        stockTransactionDao.updateDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries,
            oldDocumentNumber = oldDocumentNumber,
            newDocumentNumber = newDocumentNumber
        )
    }
}