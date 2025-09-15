package tr.com.cetinkaya.domain.usecase.order_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.db.TransactionRunner
import tr.com.cetinkaya.domain.model.order_transaction.OrderTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class FinishOrderTransactionUseCase(
    configuration: Configuration,
    private val orderTxRepo: OrderTransactionRepository,
    private val stockTxRepo: StockTransactionRepository,
    private val transferredDocRepo: TransferredDocumentRepository,
    private val txRunner: TransactionRunner
) : UseCase<FinishOrderTransactionUseCase.Request, FinishOrderTransactionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        txRunner.run {
            orderTxRepo.markPending(request.orderTxDoc)
            stockTxRepo.markPending(request.stockTxDoc)
            transferredDocRepo.upsertPending(request.transferredDoc)
        }
        emit(Response)
    }


    data class Request(
        val orderTxDoc: OrderTransactionDocumentDomainModel,
        val stockTxDoc: StockTransactionDocumentDomainModel,
        val transferredDoc: List<TransferredDocumentDomainModel>
    ) : UseCase.Request

    data object Response : UseCase.Response
}