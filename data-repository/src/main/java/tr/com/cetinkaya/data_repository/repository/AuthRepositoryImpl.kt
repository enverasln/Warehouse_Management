package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.data_repository.datasource.local.LocalAuthDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteAuthDataSource
import tr.com.cetinkaya.data_repository.utils.JwtUtil
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteAuthDataSource: RemoteAuthDataSource, private val localAuthDataSource: LocalAuthDataSource
) : AuthRepository {

    override fun login(usernameOrEmail: String, password: String): Flow<UserDomainModel> = flow {
        remoteAuthDataSource.login(usernameOrEmail, password).collect { token ->
            localAuthDataSource.saveAccessToken(token)
            val loggedUser = JwtUtil.parseToken(token.accessToken.token)
            localAuthDataSource.saveLoggedUser(loggedUser)

            val loggedUserDomainModel = UserDomainModel(
                username = loggedUser.username,
                email = loggedUser.email,
                warehouseNumber = loggedUser.warehouseNumber,
                warehouseName = loggedUser.warehouseName,
                mikroFlyUserId = loggedUser.mikroFlyUserId,
                documentSeries = loggedUser.documentSeries,
                newDocumentSeries = loggedUser.newOrderDocumentSeries
            )
            emit(loggedUserDomainModel)
        }
    }.catch { e -> throw e }


    override fun getLoggedUser(): Flow<UserDomainModel> = localAuthDataSource.getLoggedUser().map {
        UserDomainModel(
            username = it.username,
            email = it.email,
            warehouseNumber = it.warehouseNumber,
            warehouseName = it.warehouseName,
            mikroFlyUserId = it.mikroFlyUserId,
            documentSeries = it.documentSeries,
            newDocumentSeries = it.newOrderDocumentSeries
        )
    }
}