package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

abstract class StockTransactionSyncHandlerBase(
    private val stockTransactionRepo: StockTransactionRepository,
    private val sizeTransactionRepo: SizeTransactionRepository,
    transferredDocumentRepo: TransferredDocumentRepository,
    private val transactionType: StockTransactionType,
    private val transactionKind: StockTransactionKind,
    private val isNormalOrReturn: Byte,
    private val transactionDocumentType: StockTransactionDocumentType
) : BaseDocumentSyncHandler(transferredDocumentRepo) {

    override suspend fun isDocumentUsed(documentSeries: String, documentNumber: Int, currentCode: String?, paperNumber: String?): Boolean =
        stockTransactionRepo.isDocumentUsed(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = transactionDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            companyCode = currentCode,
            paperNumber = paperNumber,
        )

    override suspend fun getNextAvailableDocumentNumber(series: String): Int = stockTransactionRepo.getNextAvailableDocumentNumber(
        transactionType = transactionType, transactionKind = transactionKind, isNormalOrReturn = isNormalOrReturn, documentType = transactionDocumentType, documentSeries = series
    )

    override suspend fun updateDomainDocumentNumber(series: String, oldNumber: Int, newNumber: Int) {
        stockTransactionRepo.updateDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = transactionDocumentType,
            documentSeries = series,
            oldDocumentNumber = oldNumber,
            newDocumentNumber = newNumber
        )
    }

    override suspend fun syncAndMark(document: TransferredDocumentDomainModel): Int {
        val unsyncedStockTransactions: List<StockTransactionDomainModel> = stockTransactionRepo.getUnsyncedStockTransactions(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = transactionDocumentType,
            documentSeries = document.documentSeries,
            documentNumber = document.documentNumber
        )


        var sent = 0
        for (st in unsyncedStockTransactions) {
            val ok = stockTransactionRepo.sendStockTransaction(st)
            if (ok) {
                stockTransactionRepo.markStockTransactionSynced(st)
                sent++
            }
            val sizeTransactions = sizeTransactionRepo.getAllByRefRecordIdAndSizeTransactionType(st.id, SizeTransactionType.StockTransaction)

            if (!sizeTransactions.isNullOrEmpty()) {
                val pendingTxs = sizeTransactions.filter { it.syncStatus == SyncStatus.PendingTransfer }

                val ok = sizeTransactionRepo.sendSizeTransaction(sizeTxs = pendingTxs)
                if(ok) {
                    sizeTransactionRepo.markSizeTxAsSynced(pendingTxs)
                }
            }

        }
        return sent
    }
}