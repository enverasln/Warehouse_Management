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
        isNormalOrReturn: Byte
    ): Flow<CheckDocumentIsUsableRepositoryModel>

    suspend fun sendStockTransaction(stockTransaction: StockTransactionDataModel) : Boolean

    fun getNextStockTransactionDocument(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isStockTransactionNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentTypes,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel>

    suspend fun isDocumentUsed(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int
    ): Boolean

    suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String
    ): Int
}