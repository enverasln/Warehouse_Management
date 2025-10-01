package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.db.TransactionRunner
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.AddTransferredDocumentDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase


class FinishStockTransactionUseCase(
    configuration: Configuration,
    private val stockTxRepo: StockTransactionRepository,
    private val sizeTxRepo: SizeTransactionRepository,
    private val transferredDocRepo: TransferredDocumentRepository,
    private val txRunner: TransactionRunner
) : UseCase<FinishStockTransactionUseCase.Request, FinishStockTransactionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        txRunner.run {
            stockTxRepo.markPending(request.stockTxDoc)
            val sizeTxs = sizeTxRepo.getAllByStockTxDoc(request.stockTxDoc)
            val filteredSizeTxs =  sizeTxs.filter { it.syncStatus == SyncStatus.New }
            sizeTxRepo.markPending(filteredSizeTxs)
            transferredDocRepo.upsertPending(listOf(request.transferredDoc))
        }
        emit(Response)
    }

    data class Request(
        val stockTxDoc: StockTransactionDocumentDomainModel,
        val transferredDoc: TransferredDocumentDomainModel
    ) : UseCase.Request

    data object Response : UseCase.Response
}