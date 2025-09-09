package tr.com.cetinkaya.domain.usecase.size_transaction

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.repository.SizeTransactionRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class AddSizeTransactionsUseCase(
    configuration: Configuration, private val sizeTransactionRepository: SizeTransactionRepository
) : UseCase<AddSizeTransactionsUseCase.Request, AddSizeTransactionsUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        sizeTransactionRepository.addAll(request.sizeTransactions)
        emit(Response)
    }

    data class Request(val sizeTransactions: List<AddSizeTransactionDomainModel>) : UseCase.Request

    data object Response : UseCase.Response
}