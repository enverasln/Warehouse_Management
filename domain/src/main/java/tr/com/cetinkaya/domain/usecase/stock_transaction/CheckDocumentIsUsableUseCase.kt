package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.CheckDocumentSeriesAndNumberDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class CheckDocumentIsUsableUseCase(
    configuration: Configuration, private val orderTransactionRepository: StockTransactionRepository
) : UseCase<CheckDocumentIsUsableUseCase.Request, CheckDocumentIsUsableUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = orderTransactionRepository.checkDocumentSeriesAndNumber(
        documentSeries = request.documentSeries,
        documentNumber = request.documentNumber,
        companyCode = request.companyCode,
        paperNumber = request.paperNumber,
        stockTransactionType = request.transactionType,
        stockTransactionKind = request.transactionKind,
        documentType = request.transactionDocumentType,
        isNormalOrReturn = request.isNormalOrReturn
    ).map {
        Response(it)
    }


    data class Request(
        val documentSeries: String,
        val documentNumber: Int,
        val companyCode: String,
        val paperNumber: String,
        val transactionType: StockTransactionType,
        val transactionKind: StockTransactionKind,
        val transactionDocumentType: StockTransactionDocumentType,
        val isNormalOrReturn: Byte
    ) : UseCase.Request

    data class Response(val documentStatus: CheckDocumentSeriesAndNumberDomainModel) : UseCase.Response
}