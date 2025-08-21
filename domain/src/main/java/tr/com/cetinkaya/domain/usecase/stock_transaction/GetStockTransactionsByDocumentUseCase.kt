package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
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
        documentType = request.documentType,
        documentSeries = request.documentSeries,
        documentNumber = request.documentNumber
    ).map {
        Response(it)
    }


    data class Request(
        val transactionType: StockTransactionTypes,
        val transactionKind: StockTransactionKinds,
        val isNormalOrReturn: Byte,
        val documentType: StockTransactionDocumentTypes,
        val documentSeries: String,
        val documentNumber: Int
    ) : UseCase.Request

    data class Response(
        val stockTransactions: List<StockTransactionDomainModel>
    ) : UseCase.Response
}