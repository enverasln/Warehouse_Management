package tr.com.cetinkaya.domain.usecase.order

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class UpdateOrderSyncStatusUseCase(
    configuration: Configuration,
    private val orderRepository: OrderRepository,
    private val transferredDocumentRepository: TransferredDocumentRepository
): UseCase<UpdateOrderSyncStatusUseCase.Request, UpdateOrderSyncStatusUseCase.Response>(configuration) {
    override fun process(request: Request): Flow<Response> = flow {
        orderRepository.updateOrderSyncStatus(request.orderDocumentSeries, request.orderDocumentNumber, request.syncStatus)

        val transferredDocument = transferredDocumentRepository.add(
            transferredDocumentType = TransferredDocumentTypes.NormalGivenOrder,
            documentSeries = request.orderDocumentSeries,
            documentNumber = request.orderDocumentNumber,
            synchronizationStatus = false,
            description = "Aktarılacak"
        )

        if (transferredDocument < 0) {
            throw Exception("Depo transfer kaydı oluşturulurken hata oluştu.")
        }

        emit(Response)
    }


    data class Request(val orderDocumentSeries: String, val orderDocumentNumber: Int, val syncStatus: String) : UseCase.Request
    data object Response : UseCase.Response
}