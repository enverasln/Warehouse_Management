package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.order.CheckDocumentIsUsableRepositoryModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel

interface RemoteStockTransactionDataSource {

    fun checkDocumentIsUsable(
        documentSeries: String,
        documentNumber: Int,
        companyCode: String,
        paperNumber: String,
        stockTransactionType: Int,
        stockTransactionKind: Int,
        documentType: Int,
        isNormalOrReturn: Int
    ): Flow<CheckDocumentIsUsableRepositoryModel>

    suspend fun sendStockTransaction(stockTransaction: StockTransactionDataModel)

    fun getNextStockTransactionDocument(
        stockTransactionType: Byte,
        stockTransactionKind: Byte,
        isStockTransactionNormalOrReturn: Byte,
        stockTransactionDocumentType: Byte,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel>
}