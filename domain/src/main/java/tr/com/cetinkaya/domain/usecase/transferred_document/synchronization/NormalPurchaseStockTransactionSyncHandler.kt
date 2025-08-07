package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

class NormalPurchaseStockTransactionSyncHandler(
    val stockTransactionRepository: StockTransactionRepository,
    val transferredDocumentRepository: TransferredDocumentRepository
) : DocumentSyncHandler {

    private val transactionType = StockTransactionTypes.Input
    private val transactionKind = StockTransactionKinds.Wholesale
    private val documentType = StockTransactionDocumentTypes.EntryDispatchNote


    override suspend fun sync(
        document: TransferredDocumentDomainModel,
        emit: suspend (SyncProgress) -> Unit
    ) {
        try {
            var documentNumber = document.documentNumber

            val isDocumentUsed = isDocumentUsed(documentSeries = document.documentSeries, documentNumber = documentNumber)
            if (!isDocumentUsed) {
                val newDocumentNumber = getNextAvailableDocumentNumber(documentSeries = document.documentSeries)
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

    private suspend fun isDocumentUsed(documentSeries: String, documentNumber: Int): Boolean {
        return true
    }

    private suspend fun getNextAvailableDocumentNumber(documentSeries: String): Int {
        return 0
    }

    private suspend fun updateDocumentNumber(document: TransferredDocumentDomainModel, newDocumentNumber: Int, emit: suspend (SyncProgress) -> Unit) {


    }

    private suspend fun syncOrder(
        transferredDocumentType: TransferredDocumentTypes, documentSeries: String, documentNumber: Int, emit: suspend (SyncProgress) -> Unit
    ) {

    }
}