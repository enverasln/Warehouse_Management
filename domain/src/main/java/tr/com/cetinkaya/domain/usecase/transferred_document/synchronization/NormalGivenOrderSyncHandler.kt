package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

class NormalGivenOrderSyncHandler(
    private val orderTxRepo: OrderTransactionRepository,
    private val sizeTxRepo: SizeTransactionRepository,
    transferredDocRepo: TransferredDocumentRepository
) : BaseDocumentSyncHandler(transferredDocRepo) {

    private val transactionType = OrderTransactionTypes.Supply
    private val transactionKind = OrderTransactionKinds.NormalOrder

    override suspend fun isDocumentUsed(documentSeries: String, documentNumber: Int, currentCode: String?, paperNumber: String?): Boolean = !orderTxRepo.isDocumentAvailable(
        transactionType = transactionType, transactionKind = transactionKind, documentSeries = documentSeries, documentNumber = documentNumber
    )

    override suspend fun getNextAvailableDocumentNumber(series: String): Int = orderTxRepo.getNextAvailableDocumentNumber(
        transactionType = transactionType, transactionKind = transactionKind, documentSeries = series
    )

    override suspend fun updateDomainDocumentNumber(series: String, oldNumber: Int, newNumber: Int) {
        orderTxRepo.updateOrderDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            documentSeries = series,
            oldDocumentNumber = oldNumber,
            newDocumentNumber = newNumber
        )
    }

    override suspend fun syncAndMark(document: TransferredDocumentDomainModel): Int {
        val unsynced: List<OrderTransactionDomainModel> = orderTxRepo.getUnsyncedOrdersByDocument(
            transactionType = transactionType,
            transactionKind = transactionKind,
            docSeries = document.documentSeries,
            docNumber = document.documentNumber
        )

        var sent = 0
        for (order in unsynced) {
            val ok = orderTxRepo.sendOrder(order)
            if (ok) {
                orderTxRepo.markOrderTransactionSynced(order)
                sent++
            }
            val sizeTransactions = sizeTxRepo.getAllByRefRecordIdAndSizeTransactionType(order.id, SizeTransactionType.Order)
            if (!sizeTransactions.isNullOrEmpty()) {
                sizeTxRepo.sendSizeTransaction(sizeTxs = sizeTransactions)
            }
        }
        return sent
    }

}