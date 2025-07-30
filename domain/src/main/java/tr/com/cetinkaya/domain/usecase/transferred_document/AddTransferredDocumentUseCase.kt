package tr.com.cetinkaya.domain.usecase.transferred_document

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class AddTransferredDocumentUseCase(
    configuration: Configuration, private val transferredDocumentRepository: TransferredDocumentRepository
) : UseCase<AddTransferredDocumentUseCase.Request, AddTransferredDocumentUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val result = transferredDocumentRepository.add(
            request.transferredDocumentTypes, request.documentSeries, request.documentNumber, request.synchronizationStatus, request.description
        )
        emit(result)
    }.map {
        Response(it)
    }


    data class Request(
        val transferredDocumentTypes: TransferredDocumentTypes,
        val documentSeries: String,
        val documentNumber: Int,
        val synchronizationStatus: Boolean,
        val description: String
    ) : UseCase.Request

    data class Response(val id: Long) : UseCase.Response
}