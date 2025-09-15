package tr.com.cetinkaya.domain.usecase.order_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.order_transaction.AddOrderTransactionDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.repository.OrderTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class AddOrderTransactionUseCase(
    configuration: Configuration, private val orderTransactionRepository: OrderTransactionRepository
) : UseCase<AddOrderTransactionUseCase.Request, AddOrderTransactionUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        orderTransactionRepository.addWithSizeTransactions(request.addOrderTx, request.sizeTxs)
        emit(Response)
    }

    data class Request(val addOrderTx: AddOrderTransactionDomainModel, val sizeTxs: List<AddSizeTransactionDomainModel>) : UseCase.Request
    data object Response : UseCase.Response
}


