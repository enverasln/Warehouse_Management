package tr.com.cetinkaya.domain.usecase.stock_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class UpdateStockTransactionSyncStatusUseCase(
    configuration: UseCase.Configuration, private val stockTransactionRepository: StockTransactionRepository
) : UseCase<UpdateStockTransactionSyncStatusUseCase.Request, UpdateStockTransactionSyncStatusUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        stockTransactionRepository.updateStockTransactionSyncStatus(
            request.documentSeries, request.documentNumber, request.syncStatus
        )
        emit(Response)
    }

    data class Request(val documentSeries: String, val documentNumber: Int, val syncStatus: String) : UseCase.Request
    data object Response : UseCase.Response
}