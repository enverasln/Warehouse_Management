package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class FinishStockTransactionOldUseCase(
    configuration: Configuration,
    private val stockTransactionRepository: StockTransactionRepository,
    private val transferredDocumentRepository: TransferredDocumentRepository
) : UseCase<FinishStockTransactionOldUseCase.Request, FinishStockTransactionOldUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        try {

            // 1. Stok hareketleri tablosundaki kayıtların senkronizasyon durumlarını "Aktarılacak" olarak güncelle
            val updatedRows = stockTransactionRepository.updateStockTransactionSyncStatus(
                transactionType = request.transactionType,
                transactionKind = request.transactionKind,
                isNormalOrReturn = request.isNormalOrReturn,
                documentType = request.documentType,
                documentSeries = request.documentSeries,
                documentNumber = request.documentNumber,
                syncStatus = SyncStatus.ToTransfer,
                oldSyncStatus = SyncStatus.New
            )
            if (updatedRows == 0) {
                throw Exception("Depo transfer kaydı güncellemesi sırasında hata oluştu.")
            }

            // 2. Aktarılacak dokümanlar tablosuna kayıt ekle
            val transferredDocument = transferredDocumentRepository.add(
                transferredDocumentType = request.transferredDocumentType,
                documentSeries = request.documentSeries,
                documentNumber = request.documentNumber,
                synchronizationStatus = false,
                description = "Aktarılacak",
                currentCode = request.currentCode,
                paperNumber = request.paperNumber
            )

            if (transferredDocument < 0) {
                throw Exception("Depo transfer kaydı oluşturulurken hata oluştu.")
            }

            emit(Response)
        } catch (e: Exception) {
            // Hata alınması durumunda rollback yap
            stockTransactionRepository.updateStockTransactionSyncStatus(
                transactionType = request.transactionType,
                transactionKind = request.transactionKind,
                isNormalOrReturn = request.isNormalOrReturn,
                documentType = request.documentType,
                documentSeries = request.documentSeries,
                documentNumber = request.documentNumber,
                syncStatus = SyncStatus.New,
                oldSyncStatus = SyncStatus.ToTransfer
            )
            transferredDocumentRepository.delete(
                transferredDocumentTypes = request.transferredDocumentType,
                documentSeries = request.documentSeries,
                documentNumber = request.documentNumber
            )
            throw e
        }


    }


    data class Request(
        val transactionType: StockTransactionType,
        val transactionKind: StockTransactionKind,
        val isNormalOrReturn: Byte,
        val documentType: StockTransactionDocumentType,
        val transferredDocumentType: TransferredDocumentTypes,
        val documentSeries: String,
        val documentNumber: Int,
        val currentCode: String? = null,
        val paperNumber: String? = null
    ) : UseCase.Request

    data object Response : UseCase.Response
}