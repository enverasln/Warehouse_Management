package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class FinishStockTransactionUseCase(
    configuration: Configuration,
    private val stockTransactionRepository: StockTransactionRepository,
    private val transferredDocumentRepository: TransferredDocumentRepository
) : UseCase<FinishStockTransactionUseCase.Request, FinishStockTransactionUseCase.Response>(configuration) {

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
                syncStatus = "Aktarılacak"
            )
            if (updatedRows == 0) {
                throw Exception("Depo transfer kaydı güncellemesi sırasında kayıt .")
            }

            // 2. Aktarılacak dokümanlar tablosuna kayıt ekle
            val transferredDocument = transferredDocumentRepository.add(
                transferredDocumentType = request.transferredDocumentType,
                documentSeries = request.documentSeries,
                documentNumber = request.documentNumber,
                synchronizationStatus = false,
                description = "Aktarılacak"
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
                syncStatus = "Yeni Kayıt"
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
        val transactionType: StockTransactionTypes,
        val transactionKind: StockTransactionKinds,
        val isNormalOrReturn: Byte,
        val documentType: StockTransactionDocumentTypes,
        val transferredDocumentType: TransferredDocumentTypes,
        val documentSeries: String,
        val documentNumber: Int
    ) : UseCase.Request

    data object Response : UseCase.Response
}