package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.stok_transaction.CheckStockTxDocIsUsableDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class CheckDocumentIsUsableUseCase(
    configuration: Configuration, private val orderTransactionRepository: StockTransactionRepository
) : UseCase<CheckDocumentIsUsableUseCase.Request, CheckDocumentIsUsableUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val result = orderTransactionRepository.checkDocumentSeriesAndNumber(
            stockTxDoc = request.stockTxDoc,
            currentCode =  request.currentCode
        )
        emit(Response(result))
    }

    data class Request(
        val stockTxDoc: StockTransactionDocumentDomainModel,
        val currentCode: String,
    ) : UseCase.Request

    data class Response(val documentStatus: CheckStockTxDocIsUsableDomainModel) : UseCase.Response
}