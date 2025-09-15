package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.AddStockTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.CheckStockTxDocIsUsableDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionsByDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.AddTransferredDocumentDomainModel
import tr.com.cetinkaya.domain.model.user.UserDomainModel

interface StockTransactionRepository {
    fun addStockTransaction(
        barcode: String,
        quantity: Double,
        selectedDocuments: List<DocumentDomainModel>,
        stockTransactionDocument: StockTransactionDocumentDomainModel,
        loggedUser: UserDomainModel,
    ): Flow<Double>

    suspend fun add(stockTransaction: AddStockTransactionDomainModel): String

    suspend fun addWithSizeTransactions(
        stockTransaction: AddStockTransactionDomainModel,
        sizeTransactions: List<AddSizeTransactionDomainModel>
    ): String

    suspend fun finishStockTransaction(
        stockTransactionDocument: StockTransactionDocumentDomainModel,
        transferredDocument: AddTransferredDocumentDomainModel
    )

    suspend fun addAll(stockTransactions: List<StockTransactionDomainModel>): List<Long>

    suspend fun checkDocumentSeriesAndNumber(stockTxDoc: StockTransactionDocumentDomainModel, currentCode: String) : CheckStockTxDocIsUsableDomainModel

    fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentDomainModel>>

    suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: SyncStatus)

    suspend fun updateStockTransactionSyncStatus(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: SyncStatus,
        oldSyncStatus: SyncStatus,
    ): Int

    suspend fun sendStockTransaction(stockTransaction: StockTransactionDomainModel): Boolean

    fun getUnsyncedStockTransactions(): Flow<List<StockTransactionDomainModel>>

    fun getNextStockTransactionDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isStockTransactionNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
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
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<StockTransactionDomainModel>>


    suspend fun isDocumentUsed(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int,
        companyCode: String? = null,
        paperNumber: String? = null
    ): Boolean

    suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Int

    suspend fun updateDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    )

    suspend fun getUnsyncedStockTransactions(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): List<StockTransactionDomainModel>

    suspend fun markStockTransactionSynced(stockTransaction: StockTransactionDomainModel)

    fun getStockTransactionDocumentByDocumentNumber(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): Flow<GetStockTransactionDocumentDomainModel?>

    fun getStockTransactionDocumentByPaperNumberAndCurrentCode(
        documentSeries: String,
        documentNumber: Int,
        paperNumber: String,
        currentCode: String,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): Flow<GetStockTransactionDocumentDomainModel?>

    suspend fun updateStockTransaction(stockTransaction: StockTransactionDomainModel): Int

    suspend fun removeStockTransaction(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    )

    suspend fun countByDocument(stockTransactionDocument: StockTransactionDocumentDomainModel): Long

    suspend fun markPending(stockTxDoc: StockTransactionDocumentDomainModel) : Int

}