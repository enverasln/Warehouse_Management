package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetUnsyncedStockTransactionsUseCase(
    configuration: UseCase.Configuration,
    private val stockTransactionRepository: StockTransactionRepository
) : UseCase<GetUnsyncedStockTransactionsUseCase.Request, GetUnsyncedStockTransactionsUseCase.Response>(
    configuration
) {
    override fun process(request: Request): Flow<Response> = stockTransactionRepository.getUnsyncedStockTransactions()
        .map { Response(it) }


    data object Request : UseCase.Request
    data class Response(val items: List<StockTransactionDomainModel>) : UseCase.Response
}