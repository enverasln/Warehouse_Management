package tr.com.cetinkaya.domain.usecase.transferred_document

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class RemoveTransferredDocumentUseCase(
    configuration: UseCase.Configuration, private val transferredDocumentRepository: TransferredDocumentRepository
) : UseCase<RemoveTransferredDocumentUseCase.Request, RemoveTransferredDocumentUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        transferredDocumentRepository.removeTransferredDocument(
            documentSeries = request.documentSeries,
            documentNumber = request.documentNumber,
            transferredDocumentType = request.transferredDocumentType
        )
        emit(Response)
    }


    data class Request(val documentSeries: String, val documentNumber: Int, val transferredDocumentType: TransferredDocumentTypes) : UseCase.Request
    data object Response : UseCase.Response
}