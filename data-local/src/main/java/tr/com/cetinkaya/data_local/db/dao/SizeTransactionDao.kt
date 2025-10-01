package tr.com.cetinkaya.data_local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.data_local.db.entities.SizeTransactionEntity

@Dao
interface SizeTransactionDao {

    @Query(
        """
        SELECT
            *
        FROM
            size_transactions
        WHERE
            barcode = :barcode AND
            refRecordId = :refRecordId AND
            sizeTransactionType = :sizeTransactionType
    """
    )
    suspend fun getByBarcodeAndRefRecord(barcode: String, refRecordId: String, sizeTransactionType: SizeTransactionType): SizeTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(sizeTransaction: SizeTransactionEntity): Long

    @Update
    suspend fun update(sizeTransaction: SizeTransactionEntity): Int

    @Update
    suspend fun updateAll(sizeTxs: List<SizeTransactionEntity>)

    @Query(
        """
            UPDATE 
                size_transactions
            SET
                quantity = quantity + :deltaQuantity
            WHERE
                refRecordId = :refRecordId
        """
    )
    suspend fun increaseIfExists(refRecordId: String, deltaQuantity: Double)

    @Query(
        """
            SELECT
                *
            FROM
                size_transactions
            WHERE
                refRecordId = :refRecordId AND
                sizeTransactionType = :sizeTransactionType
        """
    )
    suspend fun getAllByRefRecordIdAndSizeTransactionType(refRecordId: String, sizeTransactionType: SizeTransactionType): List<SizeTransactionEntity>

    @Query(
        """
            SELECT
                size.*
            FROM
                stock_transactions sto
                    INNER JOIN size_transactions size
                        ON sto.id = size.refRecordId
            WHERE
                sto.transactionType = :txType AND
                sto.transactionKind = :txKind AND
                sto.isNormalOrReturn = :isNormalOrReturn AND
                sto.transactionDocumentType = :txDocType AND
                sto.documentSeries = :docSeries AND
                sto.documentNumber = :docNumber
        """
    )
    suspend fun getAllByDocument(
        docSeries: String,
        docNumber: Int,
        txType: StockTransactionType,
        txKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        txDocType: StockTransactionDocumentType
    ) : List<SizeTransactionEntity>

    @Delete
    suspend fun deleteAll(sizeTxs: List<SizeTransactionEntity>)

    @Query(
        """
            UPDATE
                size_transactions
            SET
                syncStatus = 2
            WHERE
                syncStatus = 1 AND
                id IN (:sizeTxIds)
        """
    )
    suspend fun markPending(sizeTxIds: List<String>)

}