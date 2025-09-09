package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.AddStockTransactionDomainModel
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.repository.StockRepository
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class AddStockTransactionUseCase(
    configuration: Configuration,
    private val stockTransactionRepository: StockTransactionRepository,
    private val sizeStockTransactionRepository: SizeTransactionRepository
) : UseCase<AddStockTransactionUseCase.Request, AddStockTransactionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {

        val stockTransactionId = stockTransactionRepository.add(request.stockTransaction)


        val sizesWithRefRecordId = request.sizeTransactions.map { it.copy(refRecordId = stockTransactionId) }

        sizeStockTransactionRepository.addAll(sizesWithRefRecordId)

        emit(Response(stockTransactionId))
    }

    data class Request(val stockTransaction: AddStockTransactionDomainModel, val sizeTransactions: List<AddSizeTransactionDomainModel>) :
        UseCase.Request

    data class Response(val recordId: String) : UseCase.Response
}