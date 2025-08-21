package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel

interface LocalTransferredDocumentDataSource {
    suspend fun add(transferredDocument: TransferredDocumentDataModel): Long
    suspend fun delete(transferredDocumentTypes: TransferredDocumentTypes, documentSeries: String, documentNumber: Int): Int
    fun getUntransferredDocumentsFlow(): Flow<List<TransferredDocumentDataModel>>
    suspend fun getUntransferredDocuments(): List<TransferredDocumentDataModel>
    suspend fun markedTransferredDocumentSynced(documentType: TransferredDocumentTypes, documentSeries: String, documentNumber: Int)

    suspend fun updateTransferredDocument(
        transferredDocumentType: TransferredDocumentTypes,
        documentSeries: String,
        documentNumber: Int,
        newDocumentNumber: Int
    )
}