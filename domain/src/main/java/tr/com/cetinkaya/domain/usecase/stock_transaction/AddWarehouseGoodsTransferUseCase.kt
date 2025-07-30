package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class AddWarehouseGoodsTransferUseCase(
    configuration: Configuration, private val stockTransactionRepository: StockTransactionRepository
) : UseCase<AddWarehouseGoodsTransferUseCase.Request, AddWarehouseGoodsTransferUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        stockTransactionRepository.addWarehouseGoodsTransfer(
            stockCode = request.stockCode,
            stockName = request.stockName,
            barcode = request.barcode,
            quantity = request.quantity,
            price = request.price,
            stockTransactionDocument = request.stockTransactionDocument,
            inputWarehouseNumber = request.inputWarehouseNumber,
            outputWarehouseNumber = request.outputWarehouseNumber,
            responsibilityCenter = request.responsibilityCenter,
            userCode = request.userCode,
            taxPointer = request.taxPointer,
            isColorizedAndSized = request.isColorizedAndSized
        )
        emit(Response)
    }


    data class Request(
        val stockCode: String,
        val stockName: String,
        val barcode: String,
        val quantity: Double,
        val price: Double,
        val stockTransactionDocument: StockTransactionDocumentDomainModel,
        val inputWarehouseNumber: Int,
        val outputWarehouseNumber: Int,
        val responsibilityCenter: String,
        val userCode: Int,
        val taxPointer: Byte,
        val isColorizedAndSized: Boolean
    ) : UseCase.Request

    data object Response : UseCase.Response
}