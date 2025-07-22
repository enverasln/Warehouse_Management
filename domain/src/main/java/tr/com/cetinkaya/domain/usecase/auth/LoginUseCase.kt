package tr.com.cetinkaya.domain.usecase.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.repository.AuthRepository
import tr.com.cetinkaya.domain.usecase.UseCase

class LoginUseCase(configuration: UseCase.Configuration, private val authRepository: AuthRepository) :
    UseCase<LoginUseCase.Request, LoginUseCase.Response>(configuration) {

    override fun process(request: Request): Flow<Response> =
        authRepository.login(request.usernameOrEmail, request.password).map { Response(it) }


    data class Request(val usernameOrEmail: String, val password: String) : UseCase.Request
    data class Response(val loggedUser: UserDomainModel) : UseCase.Response
}