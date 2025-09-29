package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetTransferWareHouseNumberUseCase(
    configuration: Configuration, private val stockTxRepo: StockTransactionRepository
) : UseCase<GetTransferWareHouseNumberUseCase.Request, GetTransferWareHouseNumberUseCase.Response>(
    configuration
) {
    override fun process(request: Request): Flow<Response> = flow {
        val result = stockTxRepo.getTransferWarehouseNumber(documentSeries = request.documentSeries, documentNumber = request.documentNumber,
            transactionType = StockTransactionType.WarehouseTransfer,
            transactionKind = StockTransactionKind.InternalTransfer,
            isNormalOrReturn = 0,
            transactionDocumentType = StockTransactionDocumentType.InterWarehouseShippingNote)
        emit(Response(result))
    }

    data class Request(val documentSeries: String, val documentNumber: Int) : UseCase.Request
    data class Response(val warehouseNumber: Int?) : UseCase.Response
}