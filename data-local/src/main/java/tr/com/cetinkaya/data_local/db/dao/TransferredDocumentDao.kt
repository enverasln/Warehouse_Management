package tr.com.cetinkaya.data_local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.data_local.db.entities.TransferredDocumentEntity

@Dao
interface TransferredDocumentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(transferredDocument: TransferredDocumentEntity): Long

    @Query(
        """
        DELETE FROM transferred_documents
        WHERE 
            documentSeries = :documentSeries
            AND documentNumber = :documentNumber
            AND transferredDocumentType = :transferredDocumentType
    """
    )
    suspend fun delete(documentSeries: String, documentNumber: Int, transferredDocumentType: TransferredDocumentType): Int

    @Query(
        """
            SELECT 
                *
            FROM
                transferred_documents
            WHERE synchronizationStatus = 0
            ORDER BY transferredDocumentType, documentSeries, documentNumber
        """
    )
    fun getUntransferredDocumentsFlow(): Flow<List<TransferredDocumentEntity>>

    @Query(
        """
            SELECT 
                *
            FROM
                transferred_documents
            WHERE synchronizationStatus = 0
            ORDER BY transferredDocumentType, documentSeries, documentNumber
        """
    )
    suspend fun getUntransferredDocuments(): List<TransferredDocumentEntity>

    @Query(
        """
            SELECT
                *
            FROM
                transferred_documents
            WHERE
                documentSeries = :documentSeries
                AND documentNumber = :documentNumber
                AND transferredDocumentType = :documentType
        """
    )
    suspend fun getTransferredDocumentByDocumentSeriesAndNumber(
        documentSeries: String, documentNumber: Int, documentType: TransferredDocumentType
    ): TransferredDocumentEntity?

    @Update
    suspend fun update(transferredDocument: TransferredDocumentEntity)

    @Delete
    suspend fun removeTransferredDocument(transferredDocument: TransferredDocumentEntity)

    @Query("""
        SELECT 
            *
        FROM 
            transferred_documents
        WHERE
            documentSeries=:documentSeries AND
            documentNumber=:documentNumber AND 
            transferredDocumentType=:documentType
    """)
    suspend fun getTransferredDocumentByDocumentSeriesNumberAndType(
        documentSeries: String, documentNumber: Int, documentType: TransferredDocumentType
    ): TransferredDocumentEntity?

    @Upsert
    suspend  fun upsertAll(transferredDocs: List<TransferredDocumentEntity>) : List<Long>
}