package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.model.stok_transaction.GetWarehouseTransfersByDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetWarehouseTransfersByDocumentUseCase(
    configuration: Configuration, private val stockTxRepository: StockTransactionRepository
) : UseCase<GetWarehouseTransfersByDocumentUseCase.Request, GetWarehouseTransfersByDocumentUseCase.Response>(configuration) {
    override fun process(request: Request): Flow<Response> = stockTxRepository.getWarehouseTransfersByDocument(
        documentSeries = request.documentSeries,
        documentNumber = request.documentNumber,
        transactionType = StockTransactionType.WarehouseTransfer,
        transactionKind = StockTransactionKind.InternalTransfer,
        isNormalOrReturn = 0.toByte(),
        transactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote
    ).map { transfers ->
        Response(transfers)
    }


    data class Request(
        val documentSeries: String, val documentNumber: Int
    ) : UseCase.Request

    data class Response(val transfers: List<GetWarehouseTransfersByDocumentDomainModel>) : UseCase.Response
}