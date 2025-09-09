package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetNextStockTransactionDocumentUseCase(
    configuration: Configuration, private val stockTransactionRepository: StockTransactionRepository
) : UseCase<GetNextStockTransactionDocumentUseCase.Request, GetNextStockTransactionDocumentUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = stockTransactionRepository.getNextStockTransactionDocument(
        transactionType = request.stockTransactionType,
        transactionKind = request.stockTransactionKind,
        isStockTransactionNormalOrReturn = request.isStockTransactionNormalOrReturn,
        documentType = request.stockTransactionDocumentType,
        documentSeries = request.documentSeries
    ).map {
        Response(it)
    }

    data class Request(
        val stockTransactionType: StockTransactionType,
        val stockTransactionKind: StockTransactionKind,
        val isStockTransactionNormalOrReturn: Byte,
        val stockTransactionDocumentType: StockTransactionDocumentType,
        val documentSeries: String
    ) : UseCase.Request

    data class Response(val stockTransactionDocument: StockTransactionDocumentDomainModel) : UseCase.Response
}