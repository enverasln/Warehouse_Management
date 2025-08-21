package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.domain.model.order.GetNextDocumentSeriesAndNumberDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetNextOrderDocumentSeriesAndNumberUseCase(
    configuration: Configuration, private val orderRepository: OrderRepository
) : UseCase<GetNextOrderDocumentSeriesAndNumberUseCase.Request, GetNextOrderDocumentSeriesAndNumberUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val result = orderRepository.getNextDocumentSeriesAndNumber(
            request.orderType, request.orderKind, request.documentSeries
        )
        emit(Response(result))
    }


    data class Request(val orderType: OrderTransactionTypes, val orderKind: OrderTransactionKinds, val documentSeries: String) : UseCase.Request
    data class Response(val nextDocument: GetNextDocumentSeriesAndNumberDomainModel) : UseCase.Response
}