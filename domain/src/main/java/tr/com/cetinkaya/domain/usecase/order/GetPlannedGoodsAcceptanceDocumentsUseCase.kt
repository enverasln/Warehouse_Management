package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetPlannedGoodsAcceptanceDocumentsUseCase(configuration: Configuration, private val orderRepository: OrderRepository) :
    UseCase<GetPlannedGoodsAcceptanceDocumentsUseCase.Request, GetPlannedGoodsAcceptanceDocumentsUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = orderRepository.getPlannedGoodsAcceptanceDocuments(
        request.warehouseNumber, request.companyName, request.documentDate
    ).map {
        Response(it)
    }

    data class Request(val warehouseNumber: Int, val companyName: String, val documentDate: String) : UseCase.Request

    data class Response(val documents: List<DocumentDomainModel>) : UseCase.Response
}


