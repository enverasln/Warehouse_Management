package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.CheckDocumentSeriesAndNumberDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionsByDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.user.UserDomainModel

interface StockTransactionRepository {

    fun addStockTransaction(
        barcode: String,
        quantity: Double,
        selectedDocuments: List<DocumentDomainModel>,
        stockTransactionDocument: StockTransactionDocumentDomainModel,
        loggedUser: UserDomainModel,
    ): Flow<Double>

    fun checkDocumentSeriesAndNumber(
        documentSeries: String,
        documentNumber: Int,
        companyCode: String,
        paperNumber: String,
        stockTransactionType: Int,
        stockTransactionKind: Int,
        documentType: Int,
        isNormalOrReturn: Int
    ): Flow<CheckDocumentSeriesAndNumberDomainModel>

    fun getStockTransactionsByDocument(
        transactionType: Byte,
        transactionKind: Byte,
        isNormalOrReturn: Byte,
        documentType: Byte,
        documentSeries: String,
        documentNumber: Int
    ) : Flow<List<GetStockTransactionsByDocumentDomainModel>>

    suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String)

    suspend fun sendStockTransaction(stockTransaction: StockTransactionDomainModel)

    fun getUnsyncedStockTransactions() : Flow<List<StockTransactionDomainModel>>

    fun getNextStockTransactionDocument(
        stockTransactionType: Byte,
        stockTransactionKind: Byte,
        isStockTransactionNormalOrReturn: Byte,
        stockTransactionDocumentType: Byte,
        documentSeries: String
    ) : Flow<StockTransactionDocumentDomainModel>
}