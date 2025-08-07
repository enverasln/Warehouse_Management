package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.DocumentSyncRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase
import kotlin.collections.iterator

class SyncAllDocumentsUseCase(
    configuration: Configuration,
    @JvmSuppressWildcards
    private val handlers: Map<TransferredDocumentTypes, DocumentSyncHandler>,
    private val transferredDocumentRepository: TransferredDocumentRepository
) : UseCase<SyncAllDocumentsUseCase.Request, SyncAllDocumentsUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        emit(SyncProgress.Started("Senkronizasyon başladı"))
        val unsyncedDocuments = transferredDocumentRepository.getUntransferredDocuments()
        val groupedByType = unsyncedDocuments.groupBy { it.transferredDocumentType }

        for((type, documents) in groupedByType) {
            val handler = handlers[type]
            if(handler !=  null) {
                for(doc in documents) {
                    handler.sync(doc){progress -> emit(progress)}
                }
            } else {
                emit(SyncProgress.Error("Bu tip için handler bulunmamaktadır: $type"))
            }
        }

    }.map {
        Response(it)
    }


    data class Request(val retryCount: Int = 1) : UseCase.Request
    data class Response(val syncProgression: SyncProgress) : UseCase.Response
}

