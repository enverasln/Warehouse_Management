package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class CountByDocumentSeriesAndNumberUseCase(
    configuration: Configuration,
    private val orderRepository: OrderRepository
) : UseCase<CountByDocumentSeriesAndNumberUseCase.Request, CountByDocumentSeriesAndNumberUseCase.Response>(configuration) {
    override fun process(request: Request): Flow<Response> = flow {
        val result = orderRepository.countByDocumentSeriesAndNumber(
            documentSeries = request.documentSeries,
            documentNumber = request.oldDocumentNumber
        )
        emit(Response(result))
    }


    data class Request(
        val transactionType: OrderTransactionTypes,
        val transactionKind: OrderTransactionKinds,
        val documentSeries: String,
        val oldDocumentNumber: Int,
    ) : UseCase.Request

    data class Response(
        val recordCount: Int
    ) : UseCase.Response
}