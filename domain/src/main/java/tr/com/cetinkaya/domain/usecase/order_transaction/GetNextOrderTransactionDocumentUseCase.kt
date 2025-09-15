package tr.com.cetinkaya.domain.usecase.order_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetNextOrderTransactionDocumentUseCase(
    configuration: Configuration, private val orderRepository: OrderTransactionRepository
) : UseCase<GetNextOrderTransactionDocumentUseCase.Request, GetNextOrderTransactionDocumentUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val result = orderRepository.getNextDocumentSeriesAndNumber(
            request.txType, request.txKind, request.docSeries
        )
        emit(Response(result))
    }


    data class Request(val txType: OrderTransactionTypes, val txKind: OrderTransactionKinds, val docSeries: String) : UseCase.Request
    data class Response(val nextDocument: OrderTransactionDocumentDomainModel) : UseCase.Response
}