package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.data_repository.models.order.CheckDocumentIsUsableRepositoryModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel

interface RemoteStockTransactionDataSource {

    fun checkDocumentIsUsable(
        documentSeries: String,
        documentNumber: Int,
        companyCode: String,
        paperNumber: String,
        stockTransactionType: StockTransactionTypes,
        stockTransactionKind: StockTransactionKinds,
        documentType: StockTransactionDocumentTypes,
        isNormalOrReturn: Int
    ): Flow<CheckDocumentIsUsableRepositoryModel>

    suspend fun sendStockTransaction(stockTransaction: StockTransactionDataModel)

    fun getNextStockTransactionDocument(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isStockTransactionNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentTypes,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel>
}