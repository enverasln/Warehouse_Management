package tr.com.cetinkaya.domain.usecase.transferred_document

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetUntransferredDocumentsUseCase(
    configuration: Configuration, private val transferredDocumentRepository: TransferredDocumentRepository
) : UseCase<GetUntransferredDocumentsUseCase.Request, GetUntransferredDocumentsUseCase.Response>(configuration) {
    override fun process(request: Request): Flow<Response> =
        transferredDocumentRepository.getUntransferredDocumentsFlow().map { untransferredDocuments ->
            Response(untransferredDocuments)
        }


    data object Request : UseCase.Request
    data class Response(val untransferredDocuments: List<TransferredDocumentDomainModel>) : UseCase.Response
}