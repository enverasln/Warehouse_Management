package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionsByDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetStockTransactionsByDocumentUseCase(
    configuration: UseCase.Configuration,
    private val stockTransactionRepository: StockTransactionRepository
): UseCase<GetStockTransactionsByDocumentUseCase.Request, GetStockTransactionsByDocumentUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = stockTransactionRepository.getStockTransactionsByDocument(
        transactionKind = request.transactionType,
        transactionType = request.transactionKind,
        isNormalOrReturn = request.isNormalOrReturn,
        documentType = request.documentType,
        documentSeries = request.documentSeries,
        documentNumber = request.documentNumber
    ).map {
        Response(it)

    }


    data class Request(
        val transactionType: Byte, val transactionKind: Byte, val isNormalOrReturn: Byte, val documentType: Byte, val documentSeries: String, val documentNumber: Int
    ) : UseCase.Request

    data class Response(
        val stockTransactions: List<GetStockTransactionsByDocumentDomainModel>
    ): UseCase.Response
}