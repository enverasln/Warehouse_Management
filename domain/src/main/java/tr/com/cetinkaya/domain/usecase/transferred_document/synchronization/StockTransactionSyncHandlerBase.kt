package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

abstract class StockTransactionSyncHandlerBase (
    private val stockRepo: StockTransactionRepository,
    transferredDocumentRepo: TransferredDocumentRepository,
    private val type: StockTransactionTypes,
    private val kind: StockTransactionKinds,
    private val isNormalOrReturn: Byte,
    private val docType: StockTransactionDocumentTypes
) : BaseDocumentSyncHandler(transferredDocumentRepo){

    override suspend fun isDocumentUsed(series: String, number: Int): Boolean =
        stockRepo.isDocumentUsed(
            transactionType = type,
            transactionKind = kind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = docType,
            documentSeries = series,
            documentNumber = number
        )

    override suspend fun getNextAvailableDocumentNumber(series: String): Int =
        stockRepo.getNextAvailableDocumentNumber(
            transactionType = type,
            transactionKind = kind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = docType,
            documentSeries = series
        )

    override suspend fun updateDomainDocumentNumber(series: String, oldNumber: Int, newNumber: Int) {
        stockRepo.updateDocumentNumber(
            transactionType = type,
            transactionKind = kind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = docType,
            documentSeries = series,
            oldDocumentNumber = oldNumber,
            newDocumentNumber = newNumber
        )
    }

    override suspend fun syncAndMark(document: TransferredDocumentDomainModel, documentNumber: Int): Int {
        val unsynced: List<StockTransactionDomainModel> = stockRepo.getUnsyncedStockTransactions(
            transactionType = type,
            transactionKind = kind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = docType,
            documentSeries = document.documentSeries,
            documentNumber = documentNumber
        )
        var sent = 0
        for (st in unsynced) {
            val ok = stockRepo.sendStockTransaction(st)
            if (ok) {
                stockRepo.markStockTransactionSynced(st)
                sent++
            }
        }
        return sent
    }
}