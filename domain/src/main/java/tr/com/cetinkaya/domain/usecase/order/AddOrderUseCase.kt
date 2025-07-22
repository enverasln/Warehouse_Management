package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class AddOrderUseCase(
    configuration: UseCase.Configuration,
    private val orderRepository: OrderRepository
) : UseCase<AddOrderUseCase.Request, AddOrderUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        orderRepository.addOrder(
            newOrderDocumentSeries = request.newOrderDocumentSeries,
            newOrderDocumentNumber = request.newOrderDocumentNumber,
            barcode = request.barcode,
            quantity = request.quantity,
            warehouseNumber = request.warehouseNumber,
            documents = request.documents
        )
        emit(Response)
    }

    data class Request(
        val newOrderDocumentSeries: String,
        val newOrderDocumentNumber: Int,
        val barcode: String,
        val quantity: Double,
        val warehouseNumber: Int,
        val documents: List<Pair<String, Int>>
    ) : UseCase.Request

    data object Response: UseCase.Response
}