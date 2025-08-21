package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

class NormalGivenOrderSyncHandler(
    private val orderRepository: OrderRepository,
    transferredDocumentRepository: TransferredDocumentRepository
) : BaseDocumentSyncHandler(transferredDocumentRepository) {

    private val transactionType = OrderTransactionTypes.Supply
    private val transactionKind = OrderTransactionKinds.NormalOrder

    override suspend fun isDocumentUsed(series: String, number: Int): Boolean = orderRepository.isDocumentUsed(
        transactionType = transactionType, transactionKind = transactionKind, documentSeries = series, documentNumber = number
    )

    override suspend fun getNextAvailableDocumentNumber(series: String): Int = orderRepository.getNextAvailableDocumentNumber(
        transactionType = transactionType, transactionKind = transactionKind, documentSeries = series
    )

    override suspend fun updateDomainDocumentNumber(series: String, oldNumber: Int, newNumber: Int) {
        orderRepository.updateOrderDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            documentSeries = series,
            oldDocumentNumber = oldNumber,
            newDocumentNumber = newNumber
        )
    }

    override suspend fun syncAndMark(document: TransferredDocumentDomainModel, documentNumber: Int): Int {
        val unsynced: List<OrderDomainModel> = orderRepository.getUnsyncedOrdersByDocument(
            transactionType = transactionType,
            transactionKind = transactionKind,
            documentSeries = document.documentSeries,
            documentNumber = documentNumber
        )

        var sent = 0
        for (order in unsynced) {
            val ok = orderRepository.sendOrder(order)
            if (ok) {
                orderRepository.markOrderTransactionSynced(order)
                sent++
            }
        }
        return sent
    }

}