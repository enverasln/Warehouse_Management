package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetStockTransactionsByDocumentUseCase(
    configuration: Configuration, private val stockTransactionRepository: StockTransactionRepository
) : UseCase<GetStockTransactionsByDocumentUseCase.Request, GetStockTransactionsByDocumentUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = stockTransactionRepository.getStockTransactionsByDocument(
        transactionType = request.transactionType,
        transactionKind = request.transactionKind,
        isNormalOrReturn = request.isNormalOrReturn,
        documentType = request.transactionDocumentType,
        documentSeries = request.documentSeries,
        documentNumber = request.documentNumber
    ).map {
        Response(it)
    }


    data class Request(
        val transactionType: StockTransactionType,
        val transactionKind: StockTransactionKind,
        val isNormalOrReturn: Byte,
        val transactionDocumentType: StockTransactionDocumentType,
        val documentSeries: String,
        val documentNumber: Int
    ) : UseCase.Request

    data class Response(
        val stockTransactions: List<StockTransactionDomainModel>
    ) : UseCase.Response
}