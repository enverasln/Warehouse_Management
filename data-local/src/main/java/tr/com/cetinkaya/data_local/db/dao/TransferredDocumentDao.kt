package tr.com.cetinkaya.data_local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
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
    suspend fun delete(documentSeries: String, documentNumber: Int, transferredDocumentType: TransferredDocumentTypes) : Int

    @Query(
        """
            SELECT 
                *
            FROM
                transferred_documents
            WHERE synchronizationStatus = 0
        """
    )
    fun getUntransferredDocuments(): Flow<List<TransferredDocumentEntity>>

}