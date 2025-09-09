package tr.com.cetinkaya.data_local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.data_local.db.entities.StockTransactionEntity
import tr.com.cetinkaya.data_local.models.stok_transaction.GetStockTransactionsByDocumentLocalModel

@Dao
interface StockTransactionDao {

    @Query(
        """
         SELECT 
            COALESCE(MAX(lineNumber), -1) + 1
        FROM 
            stock_transactions 
        WHERE 
            documentSeries = :documentSeries AND 
            documentNumber = :documentNumber AND 
            transactionType = :transactionType AND 
            transactionKind = :transactionKind AND 
            isNormalOrReturn = :isNormalOrReturn AND 
            transactionDocumentType = :transactionDocumentType"""
    )
    suspend fun getNextLineNumber(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): Long

    @Query(
        """
            SELECT
                *
            FROM
                stock_transactions
            WHERE
                transactionType = :transactionType AND
                transactionKind = :transactionKind AND
                isNormalOrReturn = :isNormalOrReturn AND
                transactionDocumentType = :transactionDocumentType AND
                documentSeries = :documentSeries AND
                documentNumber = :documentNumber AND
                syncStatus = :syncStatus
        """
    )
    fun getAllByDocumentAndSyncStatus(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: SyncStatus
    ) : List<StockTransactionEntity>

    @Query(
        """
            UPDATE 
                stock_transactions
            SET
                quantity = quantity + :deltaQuantity,
                totalPrice = totalPrice + :deltaTotalPrice,
                updatedAt = CAST(strftime('%s','now') AS INTEGER) * 1000
            WHERE
                transactionType = :transactionType AND
                transactionKind = :transactionKind AND
                isNormalOrReturn = :isNormalOrReturn AND
                transactionDocumentType = :transactionDocumentType AND
                documentSeries = :documentSeries AND
                documentNumber = :documentNumber AND
                stockCode = :stockCode
        """
    )
    suspend fun incrementIfExists(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        stockCode: String,
        deltaQuantity: Double,
        deltaTotalPrice: Double
    ): Int

    @Query(
        """
            SELECT 
                *
            FROM
                stock_transactions
            WHERE
                transactionType = :transactionType AND
                transactionKind = :transactionKind AND
                isNormalOrReturn = :isNormalOraReturn AND
                transactionDocumentType = :transactionDocumentType AND
                documentSeries = :documentSeries AND
                documentNumber = :documentNumber AND
                stockCode = :stockCode
        """
    )
    suspend fun getByDocumentAndStockCode(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOraReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        stockCode: String
    ) : StockTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOne(stockTransaction: StockTransactionEntity): Long


    @Query(
        """
        SELECT st.id, st.barcode, o.remainingQuantity AS quantity, st.quantity AS deliveredQuantity, o.stockName 
        FROM
            orders o 
                INNER JOIN stock_transactions st 
                    ON o.id = st.orderId AND o.barcode = st.barcode 
        WHERE 
            st.transactionType = :transactionType AND 
            st.transactionKind = :transactionKind AND 
            st.isNormalOrReturn = :isNormalOrReturn AND 
            st.transactionDocumentType = :documentType AND
            st.documentSeries = :documentSeries AND
            st.documentNumber = :documentNumber
        ORDER BY st.updatedAt DESC"""
    )
    fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentLocalModel>>

    @Query(
        """
        SELECT 
            *
        FROM 
            stock_transactions 
        WHERE 
            barcode = :barcode AND
            documentSeries = :documentSeries AND 
            documentNumber = :documentNumber AND
            orderId = :orderId
    """
    )
    suspend fun getStockTransactionByBarcode(barcode: String, documentSeries: String, documentNumber: Int, orderId: String): StockTransactionEntity?

    @Update
    suspend fun update(stockTransaction: StockTransactionEntity): Int

    @Update
    suspend fun updateAll(stockTransactions: List<StockTransactionEntity>) : Int

    @Query(
        """
            UPDATE stock_transactions
            SET syncStatus = :syncStatus
            WHERE documentSeries = :documentSeries AND documentNumber = :documentNumber
        """
    )
    suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: SyncStatus)

    @Query(
        """
        UPDATE stock_transactions
        SET syncStatus = :syncStatus
        WHERE 
            transactionType = :transactionType AND 
            transactionKind = :transactionKind AND 
            isNormalOrReturn = :isNormalOrReturn AND 
            transactionDocumentType = :documentType AND
            documentSeries = :documentSeries AND
            documentNumber = :documentNumber AND
            syncStatus = :oldSyncStatus
    """
    )
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

    @Query("SELECT * FROM stock_transactions WHERE syncStatus = :syncStatus")
    fun getBySyncStatus(syncStatus: SyncStatus): Flow<List<StockTransactionEntity>>


    @Query(
        """
        SELECT st.*
        FROM stock_transactions st
        WHERE 
            st.transactionType = :transactionType AND 
            st.transactionKind = :transactionKind AND 
            st.isNormalOrReturn = :isNormalOrReturn AND 
            st.transactionDocumentType = :documentType AND
            st.documentSeries = :documentSeries AND
            st.documentNumber = :documentNumber
        ORDER BY st.updatedAt DESC"""
    )
    fun getStockTransactionsByDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<StockTransactionEntity>>

    @Query(
        """
        SELECT st.* 
        FROM stock_transactions st
        WHERE 
            st.transactionType = :transactionType AND
            st.transactionKind = :transactionKind AND
            st.isNormalOrReturn = :isStockTransactionNormalOrReturn AND
            st.transactionDocumentType = :documentType AND
            st.documentSeries = :documentSeries
        ORDER BY st.documentNumber DESC
        LIMIT 1
    """
    )
    fun getNextStockTransactionDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isStockTransactionNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Flow<StockTransactionEntity?>

    @Query(
        """
        SELECT documentNumber 
        FROM stock_transactions st
        WHERE 
            st.transactionType = :transactionType AND
            st.transactionKind = :transactionKind AND
            st.isNormalOrReturn = :isNormalOrReturn AND
            st.transactionDocumentType = :documentType AND
            st.documentSeries = :documentSeries
        ORDER BY st.documentNumber DESC
        LIMIT 1
    """
    )
    suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Int?

    @Query(
        """
            UPDATE stock_transactions
            SET syncStatus = "Aktarıldı"
            WHERE id = :stockTransactionId AND barcode = :barcode
        """
    )
    suspend fun markStockTransactionSynced(stockTransactionId: String, barcode: String)

    @Query(
        """
            SELECT * FROM stock_transactions
            WHERE 
                transactionType = :transactionType AND
                transactionKind = :transactionKind AND
                isNormalOrReturn = :isNormalOrReturn AND
                transactionDocumentType = :transactionDocumentType AND
                documentSeries = :documentSeries AND
                documentNumber = :documentNumber            
        """
    )
    suspend fun getUnsyncedStockTransactions(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): List<StockTransactionEntity>

    @Query(
        """
            UPDATE stock_transactions
            SET documentNumber = :newDocumentNumber
            WHERE 
                transactionType = :transactionType AND
                transactionKind = :transactionKind AND
                isNormalOrReturn = :isNormalOrReturn AND
                transactionDocumentType = :documentType AND
                documentSeries = :documentSeries AND
                documentNumber = :oldDocumentNumber
        """
    )
    suspend fun updateDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    )

    @Query(
        """
            SELECT * FROM stock_transactions
            WHERE 
                stockCode = :stockCode AND
                transactionType = :transactionType AND
                transactionKind = :transactionKind AND
                isNormalOrReturn = :isNormalOrReturn AND
                transactionDocumentType = :transactionDocumentType AND
                documentSeries = :documentSeries AND
                documentNumber = :documentNumber            
        """
    )
    suspend fun getStockTransactionIdByStockCodeAndDocument(
        stockCode: String,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): StockTransactionEntity?

    @Delete
    suspend fun removeStockTransactions(stockTransaction: List<StockTransactionEntity>)

    @Query(
        """
        SELECT
            *
        FROM 
            stock_transactions
        WHERE 
            documentSeries = :documentSeries AND 
            documentNumber = :documentNumber AND 
            transactionType = :transactionType AND 
            transactionKind = :transactionKind AND 
            isNormalOrReturn = :isNormalOrReturn AND 
            transactionDocumentType = :transactionDocumentType
    """
    )
    suspend fun getStockTransactions(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): List<StockTransactionEntity>?


}


