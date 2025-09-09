package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetStockTransactionDocumentByDocumentNumberUseCase(
    configuration: Configuration, private val stockTransactionRepository: StockTransactionRepository
) : UseCase<GetStockTransactionDocumentByDocumentNumberUseCase.Request, GetStockTransactionDocumentByDocumentNumberUseCase.Response>(
    configuration
) {

    override fun process(request: Request): Flow<Response> = stockTransactionRepository.getStockTransactionDocumentByDocumentNumber(
        documentSeries =  request.documentSeries,
        documentNumber =  request.documentNumber,
        transactionType =request.transactionType,
        transactionKind = request.transactionKind,
        isNormalOrReturn = request.isNormalOrReturn,
        transactionDocumentType = request.transactionDocumentType,
    ).map {
        Response(it)
    }


    data class Request(
        val documentSeries: String,
        val documentNumber: Int,
        val transactionType: StockTransactionType,
        val transactionKind: StockTransactionKind,
        val isNormalOrReturn: Byte,
        val transactionDocumentType: StockTransactionDocumentType
    ) : UseCase.Request

    data class Response(
        val document: GetStockTransactionDocumentDomainModel?
    ) : UseCase.Response
}