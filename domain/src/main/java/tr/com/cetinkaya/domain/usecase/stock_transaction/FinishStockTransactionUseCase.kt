package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.AddTransferredDocumentDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase


class FinishStockTransactionUseCase(
    configuration: Configuration,
    private val stockTransactionRepository: StockTransactionRepository
) : UseCase<FinishStockTransactionUseCase.Request, FinishStockTransactionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        stockTransactionRepository.finishStockTransaction(
            stockTransactionDocument = request.stockTransactionDocument,
            transferredDocument = request.transferredDocument
        )
        emit(Response)
    }

    data class Request(
        val stockTransactionDocument: StockTransactionDocumentDomainModel,
        val transferredDocument: AddTransferredDocumentDomainModel
    ) : UseCase.Request

    data object Response : UseCase.Response
}