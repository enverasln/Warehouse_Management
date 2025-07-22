package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.stocktransaction.GetStockTransactionsByDocumentDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel

interface LocalStockTransactionDataSource {

    suspend fun addStockTransaction(stockTransaction: StockTransactionDataModel)

    suspend fun getCountByDocuments(stockTransactionDocument: StockTransactionDocumentDataModel): Long

    fun getStockTransactionsByDocument(
        transactionType: Byte,
        transactionKind: Byte,
        isNormalOrReturn: Byte,
        documentType: Byte,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentDataModel>>

    suspend fun getStockTransactionByBarcode(barcode: String, documentSeries: String, documentNumber: Int) : StockTransactionDataModel?

    suspend fun updateStockTransaction(stockTransaction: StockTransactionDataModel) : Int

    suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String)

    fun getUnsyncedStockTransactions() : Flow<List<StockTransactionDataModel>>
}