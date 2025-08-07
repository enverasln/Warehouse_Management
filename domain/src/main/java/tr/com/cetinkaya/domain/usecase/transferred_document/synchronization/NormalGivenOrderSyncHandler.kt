package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.order.OrderDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.OrderRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

class NormalGivenOrderSyncHandler(
    private val orderRepository: OrderRepository, private val transferredDocumentRepository: TransferredDocumentRepository
) : DocumentSyncHandler {

    private val transactionType = OrderTransactionTypes.Supply
    private val transactionKind = OrderTransactionKinds.NormalOrder

    override suspend fun sync(
        document: TransferredDocumentDomainModel, emit: suspend (SyncProgress) -> Unit
    ) {
        try {
            var documentNumber = document.documentNumber

            if (!isDocumentUsed(document.documentSeries, document.documentNumber)) {
                val newDocumentNumber = getNextAvailableDocumentNumber(document.documentSeries)
                updateDocumentNumber(document, newDocumentNumber, emit)
                documentNumber = newDocumentNumber
            }

            syncOrder(
                transferredDocumentType = document.transferredDocumentType,
                documentSeries = document.documentSeries,
                documentNumber = documentNumber,
                emit = emit
            )
        } catch (e: Exception) {
            emit(SyncProgress.Error("Senkronizasyon sırasında bir hata oluştu: ${e.message}"))
        }

    }


    private suspend fun isDocumentUsed(
        documentSeries: String, documentNumber: Int
    ): Boolean {
        return orderRepository.isDocumentUsed(
            transactionType = transactionType, transactionKind = transactionKind, documentSeries = documentSeries, documentNumber = documentNumber
        )
    }

    private suspend fun syncOrder(
        transferredDocumentType: TransferredDocumentTypes, documentSeries: String, documentNumber: Int, emit: suspend (SyncProgress) -> Unit
    ) {
        emit(SyncProgress.InProgress("${documentSeries}-${documentNumber} evrak numaralı sipariş sunucuya aktarılıyor."))
        val unsyncedOrder = orderRepository.getUnsyncedOrdersByDocument(
            transactionType = transactionType, transactionKind = transactionKind, documentSeries = documentSeries, documentNumber = documentNumber
        )
        syncAndMarkOrders(unsyncedOrder)
        markTransferredDocument(documentType = transferredDocumentType, documentSeries = documentSeries, documentNumber = documentNumber)
        emit(SyncProgress.InProgress("${documentSeries}-${documentNumber} evrak numaralı sipariş senkronizasyonu tamamlandı."))
    }


    private suspend fun syncAndMarkOrders(orders: List<OrderDomainModel>) {
        for (unsyncedOrder in orders) {
            val isSynced = orderRepository.sendOrder(unsyncedOrder)
            if (isSynced) {
                orderRepository.markOrderTransactionSynced(unsyncedOrder)
            }
        }
    }

    private suspend fun markTransferredDocument(documentType: TransferredDocumentTypes, documentSeries: String, documentNumber: Int) {
        transferredDocumentRepository.markedTransferredDocumentSynced(
            documentType = documentType, documentSeries = documentSeries, documentNumber = documentNumber
        )
    }


    private suspend fun updateDocumentNumber(document: TransferredDocumentDomainModel, newDocumentNumber: Int, emit: suspend (SyncProgress) -> Unit) {
        emit(SyncProgress.InProgress("${document.documentSeries}-${document.documentNumber} evrak numaralı sipariş sunucuda bulunmaktadır.\nBu sebeple yeni evrak numarası veriliyor."))
        updateOrderDocumentNumber(
            documentSeries = document.documentSeries, oldDocumentNumber = document.documentNumber, newDocumentNumber = newDocumentNumber
        )
        updateTransferredDocumentNumber(
            transferredDocumentType = document.transferredDocumentType,
            documentSeries = document.documentSeries,
            oldDocumentNumber = document.documentNumber,
            newDocumentNumber = newDocumentNumber
        )
        emit(SyncProgress.InProgress("${document.documentSeries}-${document.documentNumber} evrak numaralı siparişin yeni numarası: ${document.documentSeries}-$newDocumentNumber"))
    }

    private suspend fun updateOrderDocumentNumber(
        documentSeries: String, oldDocumentNumber: Int, newDocumentNumber: Int
    ) {
        orderRepository.updateOrderDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            documentSeries = documentSeries,
            oldDocumentNumber = oldDocumentNumber,
            newDocumentNumber = newDocumentNumber
        )
    }

    private suspend fun updateTransferredDocumentNumber(
        transferredDocumentType: TransferredDocumentTypes, documentSeries: String, oldDocumentNumber: Int, newDocumentNumber: Int
    ) {
        transferredDocumentRepository.updateTransferredDocument(
            transferredDocumentType = transferredDocumentType,
            documentSeries = documentSeries,
            documentNumber = oldDocumentNumber,
            newDocumentNumber = newDocumentNumber
        )
    }

    private suspend fun getNextAvailableDocumentNumber(
        documentSeries: String
    ): Int {
        return orderRepository.getNextAvailableDocumentNumber(
            transactionType = transactionType, transactionKind = transactionKind, documentSeries = documentSeries
        )
    }

}