package tr.com.cetinkaya.domain.usecase.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.repository.AuthRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class GetLoggedUserUseCase(configuration: UseCase.Configuration, private val authRepository: AuthRepository) :
UseCase<GetLoggedUserUseCase.Request, GetLoggedUserUseCase.Response>(configuration){

    override fun process(request: Request): Flow<Response> = authRepository.getLoggedUser().map {
        Response(it)
    }


    data object Request: UseCase.Request
    data class Response(val user: UserDomainModel): UseCase.Response
}