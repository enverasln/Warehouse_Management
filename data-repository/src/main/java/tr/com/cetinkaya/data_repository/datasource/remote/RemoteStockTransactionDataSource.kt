package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.data_repository.models.order.CheckStockTxDocDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.GetStockTransactionDocumentDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDocumentDataModel

interface RemoteStockTransactionDataSource {

    suspend fun checkStockTxDoc(stockTxDoc: StockTransactionDocumentDataModel, currentCode: String): CheckStockTxDocDataModel

    suspend fun sendStockTransaction(stockTransaction: StockTransactionDataModel): Boolean

    fun getNextStockTransactionDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isStockTransactionNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel>

    suspend fun isDocumentUsed(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int,
        companyCode: String? = null,
        paperNumber: String? = null,
    ): Boolean

    suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Int

    fun getStockTransactionDocumentByDocumentNumber(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType
    ): Flow<GetStockTransactionDocumentDataModel?>

    fun getStockTransactionDocumentByPaperNumberAndCurrentCode(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType
    ): Flow<GetStockTransactionDocumentDataModel?>
}