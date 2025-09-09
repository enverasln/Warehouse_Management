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
) : UseCase<UpdateOrderSyncStatusUseCase.Request, UpdateOrderSyncStatusUseCase.Response>(configuration) {
    override fun process(request: Request): Flow<Response> = flow {

        val recordCount = orderRepository.countByDocumentSeriesAndNumber(
            documentSeries = request.orderDocumentSeries,
            documentNumber = request.orderDocumentNumber
        )

        if (recordCount <= 0) return@flow

        orderRepository.updateOrderSyncStatus(request.orderDocumentSeries, request.orderDocumentNumber, request.syncStatus)


        val transferredDocument = transferredDocumentRepository.add(
            transferredDocumentType = TransferredDocumentTypes.NormalGivenOrder,
            documentSeries = request.orderDocumentSeries,
            documentNumber = request.orderDocumentNumber,
            synchronizationStatus = false,
            description = "Aktarılacak",
            currentCode = request.currentCode,
            paperNumber = request.paperNumber
        )
        if (transferredDocument < 0) {
            throw Exception("Depo transfer kaydı oluşturulurken hata oluştu.")
        }
        emit(Response)
    }


    data class Request(
        val orderDocumentSeries: String,
        val orderDocumentNumber: Int,
        val syncStatus: String,
        val currentCode: String? = null,
        val paperNumber: String? = null
    ) : UseCase.Request

    data object Response : UseCase.Response
}