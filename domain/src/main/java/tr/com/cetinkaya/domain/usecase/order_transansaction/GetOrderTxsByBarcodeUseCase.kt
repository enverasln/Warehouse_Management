package tr.com.cetinkaya.domain.usecase.order_transansaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDomainModel
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetOrderTxsByBarcodeUseCase(
    configuration: Configuration,
    private val orderTransactionRepository: OrderTransactionRepository
) : UseCase<GetOrderTxsByBarcodeUseCase.Request, GetOrderTxsByBarcodeUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val orderTxs = orderTransactionRepository.getOrderTransactionsByBarcodeAndDocuments(
            barcode = request.barcode,
            orderTxDocuments = request.orderTxDocuments,
            warehouseNumber = request.warehouseNumber
        )
        emit(Response(orderTxs))
    }

    data class Request(val barcode: String, val orderTxDocuments: List<Pair<String, Int>>, val warehouseNumber: Int) : UseCase.Request
    data class Response(val orderTransactions: List<OrderTransactionDomainModel>) : UseCase.Response
}