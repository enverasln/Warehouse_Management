package tr.com.cetinkaya.domain.usecase.order_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class FetchAndSaveOrderTransactionsUseCase(
    configuration: Configuration,
    private val orderRepository: OrderTransactionRepository
) : UseCase<FetchAndSaveOrderTransactionsUseCase.Request, FetchAndSaveOrderTransactionsUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> =
        orderRepository.fetchAndSaveOrderTransactions(documents = request.documents, warehouseNumber = request.warehouseNumber)
            .map { Response }


    data class Request(
        val documents: List<Pair<String, Int>>, val warehouseNumber: Int
    ) : UseCase.Request

    data object Response : UseCase.Response
}