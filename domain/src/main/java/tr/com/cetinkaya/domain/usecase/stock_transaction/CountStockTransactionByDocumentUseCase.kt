package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class CountStockTransactionByDocumentUseCase(
    configuration: Configuration, private val stockTransactionRepository: StockTransactionRepository
) : UseCase<CountStockTransactionByDocumentUseCase.Request, CountStockTransactionByDocumentUseCase.Response>(configuration) {
    override fun process(request: Request): Flow<Response> = flow {
        val rowCount = stockTransactionRepository.countByDocument(request.stockTransactionDocument)
        emit(rowCount)
    }.map {
        Response(it)
    }


    data class Request(val stockTransactionDocument: StockTransactionDocumentDomainModel) : UseCase.Request
    data class Response(val recordCount: Long) : UseCase.Response
}