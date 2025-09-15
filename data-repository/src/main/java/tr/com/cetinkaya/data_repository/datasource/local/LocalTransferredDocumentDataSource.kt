package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel

interface LocalTransferredDocumentDataSource {
    suspend fun add(transferredDocument: TransferredDocumentDataModel): Long
    suspend fun delete(transferredDocumentTypes: TransferredDocumentType, documentSeries: String, documentNumber: Int): Int
    fun getUntransferredDocumentsFlow(): Flow<List<TransferredDocumentDataModel>>
    suspend fun getUntransferredDocuments(): List<TransferredDocumentDataModel>
    suspend fun markedTransferredDocumentSynced(documentType: TransferredDocumentType, documentSeries: String, documentNumber: Int)
    suspend fun updateTransferredDocument(
        transferredDocumentType: TransferredDocumentType, documentSeries: String, documentNumber: Int, newDocumentNumber: Int
    )

    suspend fun removeTransferredDocument(
        documentSeries: String, documentNumber: Int, transferredDocumentType: TransferredDocumentType
    )

    suspend fun upsertPending(transferredDocs: List<TransferredDocumentDataModel>): List<Long>
}