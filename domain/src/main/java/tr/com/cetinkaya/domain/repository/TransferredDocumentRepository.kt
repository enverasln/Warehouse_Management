package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel

interface TransferredDocumentRepository {

    suspend fun add(
        transferredDocumentType: TransferredDocumentType,
        documentSeries: String,
        documentNumber: Int,
        synchronizationStatus: Boolean,
        description: String,
        currentCode: String?,
        paperNumber: String?
    ): Long

    suspend fun delete(
        transferredDocumentTypes: TransferredDocumentType, documentSeries: String, documentNumber: Int
    ): Int

    fun getUntransferredDocumentsFlow(): Flow<List<TransferredDocumentDomainModel>>
    suspend fun getUntransferredDocuments(): List<TransferredDocumentDomainModel>
    suspend fun markedTransferredDocumentSynced(documentType: TransferredDocumentType, documentSeries: String, documentNumber: Int)
    suspend fun updateTransferredDocument(
        transferredDocumentType: TransferredDocumentType, documentSeries: String, oldDocumentNumber: Int, newDocumentNumber: Int
    )

    suspend fun removeTransferredDocument(
        documentSeries: String, documentNumber: Int, transferredDocumentType: TransferredDocumentType
    )

    suspend fun upsertPending(transferredDocs: List<TransferredDocumentDomainModel>): List<Long>

}