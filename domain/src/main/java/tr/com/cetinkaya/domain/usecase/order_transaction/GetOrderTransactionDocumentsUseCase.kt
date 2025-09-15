package tr.com.cetinkaya.domain.usecase.order_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetOrderTransactionDocumentsUseCase(configuration: Configuration, private val orderRepository: OrderTransactionRepository) :
    UseCase<GetOrderTransactionDocumentsUseCase.Request, GetOrderTransactionDocumentsUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = orderRepository.getPlannedGoodsAcceptanceDocuments(
        request.warehouseNumber, request.companyName, request.documentDate
    ).map {
        Response(it)
    }

    data class Request(val warehouseNumber: Int, val companyName: String, val documentDate: String) : UseCase.Request

    data class Response(val documents: List<DocumentDomainModel>) : UseCase.Response
}