package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel

interface TransferredDocumentRepository {

    suspend fun add(
        transferredDocumentType: TransferredDocumentTypes,
        documentSeries: String,
        documentNumber: Int,
        synchronizationStatus: Boolean,
        description: String
    ): Long

    suspend fun delete(
        transferredDocumentTypes: TransferredDocumentTypes, documentSeries: String, documentNumber: Int
    ): Int

    fun getUntransferredDocumentsFlow(): Flow<List<TransferredDocumentDomainModel>>
    suspend fun getUntransferredDocuments(): List<TransferredDocumentDomainModel>
    suspend fun markedTransferredDocumentSynced(documentType: TransferredDocumentTypes, documentSeries: String, documentNumber: Int)
    suspend fun updateTransferredDocument(
        transferredDocumentType: TransferredDocumentTypes,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    )


}