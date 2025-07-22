package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class AddStockTransactionUseCase(
    configuration: Configuration, private val orderTransactionRepository: StockTransactionRepository
) : UseCase<AddStockTransactionUseCase.Request, AddStockTransactionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = orderTransactionRepository.addStockTransaction(
        barcode = request.barcode,
        quantity = request.quantity,
        selectedDocuments = request.selectedDocuments,
        stockTransactionDocument = request.stockTransactionDocument,
        loggedUser = request.user
    ).map { Response(it) }

    data class Request(
        val barcode: String,
        val quantity: Double,
        val selectedDocuments: List<DocumentDomainModel>,
        val stockTransactionDocument: StockTransactionDocumentDomainModel,
        val user: UserDomainModel
    ) : UseCase.Request

    data class Response(val excessAmount: Double) : UseCase.Response
}