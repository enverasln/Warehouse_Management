package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
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
        stockTransactionType: StockTransactionTypes,
        stockTransactionKind: StockTransactionKinds,
        documentType: StockTransactionDocumentTypes,
        isNormalOrReturn: Int
    ): Flow<CheckDocumentSeriesAndNumberDomainModel>

    fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentDomainModel>>

    suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String)

    suspend fun updateStockTransactionSyncStatus(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: String
    ): Int

    suspend fun sendStockTransaction(stockTransaction: StockTransactionDomainModel)

    fun getUnsyncedStockTransactions(): Flow<List<StockTransactionDomainModel>>

    fun getNextStockTransactionDocument(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isStockTransactionNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String
    ): Flow<StockTransactionDocumentDomainModel>

    suspend fun addWarehouseGoodsTransfer(
        stockCode: String,
        stockName: String,
        barcode: String,
        quantity: Double,
        price: Double,
        stockTransactionDocument: StockTransactionDocumentDomainModel,
        inputWarehouseNumber: Int,
        outputWarehouseNumber: Int,
        responsibilityCenter: String,
        userCode: Int,
        taxPointer: Byte,
        isColorizedAndSized: Boolean
    )

    fun getStockTransactionsByDocument(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<StockTransactionDomainModel>>
}