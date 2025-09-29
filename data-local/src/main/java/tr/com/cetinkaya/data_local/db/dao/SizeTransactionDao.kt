package tr.com.cetinkaya.data_local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import tr.com.cetinkaya.common.enums.SizeTransactionType
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
    suspend fun getAllByRefRecordIdAndSizeTransactionType(refRecordId: String, sizeTransactionType: SizeTransactionType): List<SizeTransactionEntity>?

    @Delete
    suspend fun deleteAll(sizeTxs: List<SizeTransactionEntity>)

}