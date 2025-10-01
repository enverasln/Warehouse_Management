package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.TransferredDocumentType
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class SyncAllDocumentsUseCase(
    configuration: Configuration,
    @JvmSuppressWildcards
    private val handlers: Map<TransferredDocumentType, DocumentSyncHandler>,
    private val transferredDocumentRepository: TransferredDocumentRepository
) : UseCase<SyncAllDocumentsUseCase.Request, SyncAllDocumentsUseCase.Response>(configuration) {
    private val syncMutex = kotlinx.coroutines.sync.Mutex()

    override fun process(request: Request): Flow<Response> = flow {
        syncMutex.lock()
        try {
            emit(SyncProgress.Started("Senkronizasyon başladı"))
            val unsyncedDocuments = transferredDocumentRepository.getUntransferredDocuments()
            val groupedByType = unsyncedDocuments.groupBy { it.transferredDocumentType }


            coroutineScope {
                for ((type, documents) in groupedByType) {
                    val handler = handlers[type]
                    if (handler != null) {
                        for (doc in documents) {
                            handler.sync(doc) { progress -> emit(progress) }
                        }
                    } else {
                        emit(SyncProgress.Error("Bu tip için handler bulunmamaktadır: $type"))
                    }
                }
            }
            emit(SyncProgress.Completed)
        } finally {
            syncMutex.unlock()
        }
    }.map {
        Response(it)
    }

    data class Request(val retryCount: Int = 1) : UseCase.Request
    data class Response(val syncProgression: SyncProgress) : UseCase.Response
}

