package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.order.ProductDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class ObservePlannedGoodsAcceptanceProductsUseCase(
    configuration: Configuration, private val orderRepository: OrderRepository
) : UseCase<ObservePlannedGoodsAcceptanceProductsUseCase.Request, ObservePlannedGoodsAcceptanceProductsUseCase.Response>(
    configuration
) {

    override fun process(request: Request): Flow<Response> {
        return orderRepository.observeLocalPlannedGoodsAcceptanceProducts(request.documents, request.warehouseNumber).map { products ->
                Response(products)
            }
    }

    data class Request(
        val documents: List<Pair<String, Int>>, val warehouseNumber: Int
    ) : UseCase.Request

    data class Response(val products: List<ProductDomainModel>) : UseCase.Response
}