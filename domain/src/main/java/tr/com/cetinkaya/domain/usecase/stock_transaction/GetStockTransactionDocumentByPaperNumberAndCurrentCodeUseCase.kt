package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetStockTransactionDocumentByPaperNumberAndCurrentCodeUseCase(
    configuration: Configuration, private val stockTransactionRepository: StockTransactionRepository

) : UseCase<GetStockTransactionDocumentByPaperNumberAndCurrentCodeUseCase.Request, GetStockTransactionDocumentByPaperNumberAndCurrentCodeUseCase.Response>(
    configuration
) {
    override fun process(request: Request): Flow<Response> =
        stockTransactionRepository.getStockTransactionDocumentByPaperNumberAndCurrentCode(
            request.documentSeries,
            request.documentNumber,
            request.paperNumber,
            request.currentCode,
            request.transactionType,
            request.transactionKind,
            request.isNormalOrReturn,
            request.transactionDocumentType
        ).map {
            Response(it)
        }

    data class Request(
        val documentSeries: String,
        val documentNumber: Int,
        val paperNumber: String,
        val currentCode: String,
        val transactionType: StockTransactionType,
        val transactionKind: StockTransactionKind,
        val isNormalOrReturn: Byte,
        val transactionDocumentType: StockTransactionDocumentType
    ) : UseCase.Request

    data class Response(
        val document: GetStockTransactionDocumentDomainModel?
    ) : UseCase.Response
}