package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetUnsyncedOrderUseCase(
    configuration: Configuration, private val orderRepository: OrderRepository
) : UseCase<GetUnsyncedOrderUseCase.Request, GetUnsyncedOrderUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = orderRepository.getUnsyncedOrders()
        .map { Response(it)  }


    data object Request : UseCase.Request
    data class Response(val items: List<OrderDomainModel>) : UseCase.Response
}