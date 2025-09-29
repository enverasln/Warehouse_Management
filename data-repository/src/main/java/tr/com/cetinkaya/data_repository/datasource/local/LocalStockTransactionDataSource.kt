package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.data_repository.models.size_transaction.AddSizeTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.AddStockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.GetStockTransactionsByDocumentDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.GetWarehouseTransferByDocumentDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDocumentDataModel
import tr.com.cetinkaya.data_repository.models.transferred_document.AddTransferredDocumentDataModel

interface LocalStockTransactionDataSource {

    suspend fun addStockTransaction(stockTransaction: StockTransactionDataModel)

    suspend fun finishStockTransaction(
        stockTransactionDocument: StockTransactionDocumentDataModel,
        transferredDocument: AddTransferredDocumentDataModel
    )

    suspend fun upsertOrIncrement(stockTransactions: List<StockTransactionDataModel>): List<Long>

    suspend fun insertOrIncrement(stockTransaction: AddStockTransactionDataModel): String

    suspend fun addWithSizeTransaction(stockTransaction: AddStockTransactionDataModel, sizeTransactions: List<AddSizeTransactionDataModel>): String

    suspend fun getNextLineNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Long

    fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentDataModel>>

    suspend fun getStockTransactionByBarcode(
        barcode: String,
        documentSeries: String,
        documentNumber: Int,
        orderId: String
    ): StockTransactionDataModel?

    suspend fun update(stockTransaction: StockTransactionDataModel): Int

    suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: SyncStatus)

    suspend fun updateStockTransactionSyncStatus(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: SyncStatus,
        oldSyncStatus: SyncStatus
    ): Int

    fun getUnsyncedStockTransactions(): Flow<List<StockTransactionDataModel>>

    fun getStockTransactionsByDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<StockTransactionDataModel>>

    fun getNextStockTransactionDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isStockTransactionNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel>

    suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Int

    suspend fun markStockTransactionSynced(stockTransaction: StockTransactionDataModel)

    suspend fun getUnsyncedStockTransactions(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): List<StockTransactionDataModel>


    suspend fun updateDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    )

    suspend fun getStockTransactionByStockCodeAndDocument(
        stockCode: String,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int

    ): StockTransactionDataModel?

    suspend fun removeStockTransaction(removedStockTransactions: List<StockTransactionDataModel>)

    suspend fun getStockTransactions(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): List<StockTransactionDataModel>

    suspend fun markPending(stockTxDoc: StockTransactionDocumentDataModel): Int

    fun getWarehouseTransfersByDocument(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ) : Flow<List<GetWarehouseTransferByDocumentDataModel>>

    suspend fun getTransferWarehouseNumber(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): Int?

    suspend fun deleteStockTransactionById(stockTxId: String)
}