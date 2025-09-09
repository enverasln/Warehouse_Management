package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class UpdateStockTransactionUseCase(
    configuration: Configuration, private val stockTransactionRepository: StockTransactionRepository
) : UseCase<UpdateStockTransactionUseCase.Request, UpdateStockTransactionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val rowCount = stockTransactionRepository.updateStockTransaction(
            request.stockTransaction
        )
        emit(rowCount)
    }.map {
        Response(it)
    }


    data class Request(val stockTransaction: StockTransactionDomainModel) : UseCase.Request
    data class Response(val rowCount: Int) : UseCase.Response
}