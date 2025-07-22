package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class TransferOrdersUseCase(
    configuration: UseCase.Configuration,
    private val orderRepository: OrderRepository
) : UseCase<TransferOrdersUseCase.Request, TransferOrdersUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val total = request.items.size
        emit(Response("Toplam $total adet sipariş kaydı aktarılacak."))

        var count = 0

        for(item in request.items){
            orderRepository.sendOrder(item)
            orderRepository.updateOrderSyncStatus(item.documentSeries, item.documentNumber, "Aktarıldı")
            count++
        }

        emit(Response("Toplam $count adet sipariş kaydı başarıyla aktarıldı."))
    }

    data class Request(val items : List<OrderDomainModel>) : UseCase.Request
    data class Response(val message: String) : UseCase.Response
}