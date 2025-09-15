package tr.com.cetinkaya.domain.usecase.order_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDomainModel
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetOrderTxsByDocumentsUseCase(
    configuration: Configuration,
    private val orderTransactionRepository: OrderTransactionRepository
) : UseCase<GetOrderTxsByDocumentsUseCase.Request, GetOrderTxsByDocumentsUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = orderTransactionRepository
        .getOrderTxsByDocuments(request.orderTxDocuments, request.warehouseNumber)
        .map { Response(it) }

    data class Request(val orderTxDocuments: List<Pair<String, Int>>, val warehouseNumber: Int) : UseCase.Request
    data class Response(val orderTxs: List<OrderTransactionDomainModel>) : UseCase.Response
}