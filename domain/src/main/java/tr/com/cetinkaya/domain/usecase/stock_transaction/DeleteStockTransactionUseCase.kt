package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class DeleteStockTransactionUseCase(
    configuration: Configuration,
    private val stockTxRepo: StockTransactionRepository
) : UseCase<DeleteStockTransactionUseCase.Request, DeleteStockTransactionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        stockTxRepo.deleteStockTransactionById(request.stockTxId)
        emit(Response)
    }


    data class Request(val stockTxId: String) : UseCase.Request
    data object Response : UseCase.Response
}