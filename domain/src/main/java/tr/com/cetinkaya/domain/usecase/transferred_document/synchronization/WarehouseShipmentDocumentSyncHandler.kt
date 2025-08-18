package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository


class WarehouseShipmentDocumentSyncHandler(
    private val stockTransactionRepository: StockTransactionRepository, private val transferredDocumentRepository: TransferredDocumentRepository
) : DocumentSyncHandler {


    private val transactionType = StockTransactionTypes.WarehouseTransfer
    private val transactionKind = StockTransactionKinds.InternalTransfer
    private val isNormalOrReturn: Byte = 0
    private val transactionDocumentType = StockTransactionDocumentTypes.InterWarehouseShippingNote

    override suspend fun sync(
        document: TransferredDocumentDomainModel, emit: suspend (SyncProgress) -> Unit
    ) {
        try {
            var documentNumber = document.documentNumber

            if (isDocumentUsed(document.documentSeries, document.documentNumber)) {
                val newDocumentNumber = getNextAvailableDocumentNumber(document.documentSeries)
                updateDocumentNumber(document, newDocumentNumber, emit)
                documentNumber = newDocumentNumber
            }

            syncStockTransaction(
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
        return stockTransactionRepository.isDocumentUsed(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = transactionDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        )
    }

    private suspend fun getNextAvailableDocumentNumber(
        documentSeries: String
    ): Int {
        return stockTransactionRepository.getNextAvailableDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = transactionDocumentType,
            documentSeries = documentSeries,
        )
    }

    private suspend fun updateDocumentNumber(document: TransferredDocumentDomainModel, newDocumentNumber: Int, emit: suspend (SyncProgress) -> Unit) {
        emit(SyncProgress.InProgress("${document.documentSeries}-${document.documentNumber} evrak numaralı stok hareketi sunucuda/lokalde bulunmaktadır.\nBu sebeple yeni evrak numarası veriliyor."))
        updateStockTransactionDocumentNumber(
            documentSeries = document.documentSeries, oldDocumentNumber = document.documentNumber, newDocumentNumber = newDocumentNumber
        )
        updateTransferredDocumentNumber(
            transferredDocumentType = document.transferredDocumentType,
            documentSeries = document.documentSeries,
            oldDocumentNumber = document.documentNumber,
            newDocumentNumber = newDocumentNumber
        )
        emit(SyncProgress.InProgress("${document.documentSeries}-${document.documentNumber} evrak numaralı stok hareket evrağının yeni numarası: ${document.documentSeries}-$newDocumentNumber"))
    }

    private suspend fun updateStockTransactionDocumentNumber(
        documentSeries: String, oldDocumentNumber: Int, newDocumentNumber: Int
    ) {
        stockTransactionRepository.updateDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = transactionDocumentType,
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
            oldDocumentNumber = oldDocumentNumber,
            newDocumentNumber = newDocumentNumber
        )
    }

    private suspend fun syncStockTransaction(
        transferredDocumentType: TransferredDocumentTypes, documentSeries: String, documentNumber: Int, emit: suspend (SyncProgress) -> Unit
    ) {
        emit(SyncProgress.InProgress("${documentSeries}-${documentNumber} evrak numaralı stok hareket evrağı sunucuya aktarılıyor."))
        val unsyncedStockTransaction = stockTransactionRepository.getUnsyncedStockTransactions(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = transactionDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        )
        syncAndMarkStockTransactions(unsyncedStockTransaction)
        markTransferredDocument(documentType = transferredDocumentType, documentSeries = documentSeries, documentNumber = documentNumber)
        emit(SyncProgress.InProgress("${documentSeries}-${documentNumber} evrak numaralı stok hareket evrağı sunucuya aktarıldı."))
    }

    private suspend fun syncAndMarkStockTransactions(stockTransactions: List<StockTransactionDomainModel>) {
        for (stockTransaction in stockTransactions) {
            val isSynced = stockTransactionRepository.sendStockTransaction(stockTransaction)
            if (isSynced) {
                stockTransactionRepository.markStockTransactionSynced(stockTransaction)
            }
        }
    }

    private suspend fun markTransferredDocument(documentType: TransferredDocumentTypes, documentSeries: String, documentNumber: Int) {
        transferredDocumentRepository.markedTransferredDocumentSynced(
            documentType = documentType, documentSeries = documentSeries, documentNumber = documentNumber
        )
    }
}
