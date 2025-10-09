package tr.com.cetinkaya.domain.usecase.current_account

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.domain.model.current_account.GetCurrentAccountByTitleDomainModel
import tr.com.cetinkaya.domain.repository.CurrentAccountRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetCurrentAccountByTitleUseCase(
    configuration: UseCase.Configuration, private val currentAccountRepo: CurrentAccountRepository
) : UseCase<GetCurrentAccountByTitleUseCase.Request, GetCurrentAccountByTitleUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> = flow {
        val currentAccounts = currentAccountRepo.getCurrentAccountsByTitle(request.currentAccountTitle)
        emit(Response(currentAccounts))
    }

    data class Request(val currentAccountTitle: String) : UseCase.Request
    data class Response(val currentAccounts: List<GetCurrentAccountByTitleDomainModel>) : UseCase.Response
}