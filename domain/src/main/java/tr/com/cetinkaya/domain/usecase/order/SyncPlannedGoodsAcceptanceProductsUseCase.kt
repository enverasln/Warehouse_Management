package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class SyncPlannedGoodsAcceptanceProductsUseCase(
    configuration: Configuration,
    private val orderRepository: OrderRepository
) : UseCase<SyncPlannedGoodsAcceptanceProductsUseCase.Request, SyncPlannedGoodsAcceptanceProductsUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        orderRepository.syncPlannedGoodsAcceptanceProducts(documents = request.documents, warehouseNumber = request.warehouseNumber)
        emit(Response)
    }

    data class Request(
        val documents: List<Pair<String, Int>>, val warehouseNumber: Int
    ) : UseCase.Request

    data object Response : UseCase.Response
}