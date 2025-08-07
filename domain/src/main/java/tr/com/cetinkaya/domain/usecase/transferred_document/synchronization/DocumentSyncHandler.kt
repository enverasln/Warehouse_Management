package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel

interface DocumentSyncHandler {
    suspend fun sync(document: TransferredDocumentDomainModel, emit: suspend (SyncProgress) -> Unit)
}