package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import tr.com.cetinkaya.data_repository.datasource.local.LocalAuthDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteAuthDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteWarehouseDataSource
import tr.com.cetinkaya.data_repository.utils.JwtUtil
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteAuthDataSource: RemoteAuthDataSource,
    private val remoteWarehouseDataSource: RemoteWarehouseDataSource,
    private val localAuthDataSource: LocalAuthDataSource
) : AuthRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun login(usernameOrEmail: String, password: String): Flow<UserDomainModel> {
        return remoteAuthDataSource.login(usernameOrEmail, password).map { token ->
                localAuthDataSource.saveAccessToken(token)
                JwtUtil.parseToken(token.accessToken.token)
            }.flatMapLatest { loggedUser ->
                remoteWarehouseDataSource.getWarehouseByWarehouseNumber(loggedUser.warehouseNumber).map { warehouse ->
                        val lockDate = warehouse.lockDate ?: 0L
                        val userWithLockDate = loggedUser.copy(warehouseLockDate = lockDate)
                        localAuthDataSource.saveLoggedUser(userWithLockDate)
                        UserDomainModel(
                            username = loggedUser.username,
                            email = loggedUser.email,
                            warehouseNumber = loggedUser.warehouseNumber,
                            warehouseName = loggedUser.warehouseName,
                            mikroFlyUserId = loggedUser.mikroFlyUserId,
                            documentSeries = loggedUser.documentSeries,
                            newDocumentSeries = loggedUser.newOrderDocumentSeries,
                            warehouseLockDate = warehouse.lockDate ?: 0
                        )
                    }
            }.retryWhen { e, attempt ->
                (e is java.io.IOException && attempt < 3).also { shouldRetry ->
                    if (shouldRetry) kotlinx.coroutines.delay((attempt + 1) * 1000L)
                }
            }
    }

    override fun getLoggedUser(): Flow<UserDomainModel> = localAuthDataSource.getLoggedUser().map {
        UserDomainModel(
            username = it.username,
            email = it.email,
            warehouseNumber = it.warehouseNumber,
            warehouseName = it.warehouseName,
            mikroFlyUserId = it.mikroFlyUserId,
            documentSeries = it.documentSeries,
            newDocumentSeries = it.newOrderDocumentSeries,
            warehouseLockDate = it.warehouseLockDate
        )
    }
}