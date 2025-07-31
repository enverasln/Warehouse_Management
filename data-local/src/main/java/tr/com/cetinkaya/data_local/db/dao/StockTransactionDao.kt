package tr.com.cetinkaya.data_local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.data_local.db.entities.StockTransactionEntity
import tr.com.cetinkaya.data_local.models.stok_transaction.GetStockTransactionsByDocumentLocalModel

@Dao
interface StockTransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(stockTransaction: StockTransactionEntity)

    @Query("SELECT COUNT(*) FROM stock_transactions WHERE documentSeries = :documentSeries AND documentNumber = :documentNumber AND transactionType = :transactionType AND transactionKind = :transactionKind AND isNormalOrReturn = :isNormalOrReturn AND documentType = :documentType")
    suspend fun getCountByDocument(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: Byte
    ): Long

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
            st.documentType = :documentType AND
            st.documentSeries = :documentSeries AND
            st.documentNumber = :documentNumber
        ORDER BY st.updatedAt DESC"""
    )
    fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: Byte,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentLocalModel>>

    @Query(
        """
        SELECT * FROM stock_transactions WHERE barcode = :barcode AND documentSeries = :documentSeries AND documentNumber = :documentNumber
    """
    )
    suspend fun getStockTransactionByBarcode(barcode: String, documentSeries: String, documentNumber: Int): StockTransactionEntity?

    @Update
    suspend fun updateStockTransaction(stockTransaction: StockTransactionEntity): Int

    @Query(
        """
            UPDATE stock_transactions
            SET synchronizationStatus = :syncStatus
            WHERE documentSeries = :documentSeries AND documentNumber = :documentNumber
        """
    )
    suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String)

    @Query(
        """
        UPDATE stock_transactions
        SET synchronizationStatus = :syncStatus
        WHERE 
            transactionType = :transactionType AND 
            transactionKind = :transactionKind AND 
            isNormalOrReturn = :isNormalOrReturn AND 
            documentType = :documentType AND
            documentSeries = :documentSeries AND
            documentNumber = :documentNumber
    """
    )
    suspend fun updateStockTransactionSyncStatus(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: Byte,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: String
    ): Int

    @Query("SELECT * FROM stock_transactions WHERE synchronizationStatus = :syncStatus")
    fun getBySyncStatus(syncStatus: String): Flow<List<StockTransactionEntity>>


    @Query(
        """
        SELECT st.*
        FROM stock_transactions st
        WHERE 
            st.transactionType = :transactionType AND 
            st.transactionKind = :transactionKind AND 
            st.isNormalOrReturn = :isNormalOrReturn AND 
            st.documentType = :documentType AND
            st.documentSeries = :documentSeries AND
            st.documentNumber = :documentNumber
        ORDER BY st.updatedAt DESC"""
    )
    fun getStockTransactionsByDocument(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: Byte,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<StockTransactionEntity>>

    @Query(
        """
        SELECT st.* 
        FROM stock_transactions st
        WHERE 
            st.transactionType = :stockTransactionType AND
            st.transactionKind = :stockTransactionKind AND
            st.isNormalOrReturn = :isStockTransactionNormalOrReturn AND
            st.documentType = :stockTransactionDocumentType AND
            st.documentSeries = :documentSeries
        ORDER BY st.documentNumber DESC
        LIMIT 1
    """
    )
    fun getNextStockTransactionDocument(
        stockTransactionType: StockTransactionTypes,
        stockTransactionKind: StockTransactionKinds,
        isStockTransactionNormalOrReturn: Byte,
        stockTransactionDocumentType: Byte,
        documentSeries: String
    ): Flow<StockTransactionEntity?>

}


