package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.order.GetProductByBarcodeDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetProductByBarcodeUseCase(
    configuration: Configuration, private val orderRepository: OrderRepository
) : UseCase<GetProductByBarcodeUseCase.Request, GetProductByBarcodeUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> =
        orderRepository.getProductByBarcode(request.barcode, request.sekectedDocuments, request.warehouseNumber).map { Response(it) }


    data class Request(val barcode: String, val sekectedDocuments: List<Pair<String, Int>>, val warehouseNumber: Int) : UseCase.Request
    data class Response(val product: GetProductByBarcodeDomainModel) : UseCase.Response
}


