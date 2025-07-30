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
        transferredDocumentTypes: TransferredDocumentTypes,
        documentSeries: String,
        documentNumber: Int
    ) : Int

    fun getUntransferredDocuments(): Flow<List<TransferredDocumentDomainModel>>
}