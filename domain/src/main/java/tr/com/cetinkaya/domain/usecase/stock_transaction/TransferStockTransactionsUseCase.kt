package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class TransferStockTransactionsUseCase(
    configuration: UseCase.Configuration, private val stockTransactionRepository: StockTransactionRepository
) : UseCase<TransferStockTransactionsUseCase.Request, TransferStockTransactionsUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val total = request.items.size
        emit(Response("Toplam $total adet stok kaydı aktarılacak."))

        var successCount = 0

        for (item in request.items) {
            stockTransactionRepository.sendStockTransaction(item)
            stockTransactionRepository.updateStockTransactionSyncStatus(item.documentSeries, item.documentNumber, "Aktarıldı")
            successCount++
        }

        emit(Response("Toplam $successCount adet kaydı başarıyla aktarıldı."))
    }


    data class Request(val items: List<StockTransactionDomainModel>) : UseCase.Request
    data class Response(val message: String) : UseCase.Response
}