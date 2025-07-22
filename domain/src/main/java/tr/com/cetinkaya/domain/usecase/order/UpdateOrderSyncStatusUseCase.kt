package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class UpdateOrderSyncStatusUseCase(
    configuration: UseCase.Configuration,
    private val orderRepository: OrderRepository
): UseCase<UpdateOrderSyncStatusUseCase.Request, UpdateOrderSyncStatusUseCase.Response>(configuration) {
    override fun process(request: Request): Flow<Response> = flow {
        orderRepository.updateOrderSyncStatus(request.orderDocumentSeries, request.orderDocumentNumber, request.syncStatus)
        emit(Response)
    }


    data class Request(val orderDocumentSeries: String, val orderDocumentNumber: Int, val syncStatus: String) : UseCase.Request
    data object Response : UseCase.Response
}