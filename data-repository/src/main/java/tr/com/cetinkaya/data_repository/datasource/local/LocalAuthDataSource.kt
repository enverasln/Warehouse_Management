package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.auth.TokenRepositoryModel
import tr.com.cetinkaya.data_repository.models.user.UserRepositoryModel

interface LocalAuthDataSource {
    suspend fun saveAccessToken(token: TokenRepositoryModel)
    fun getAccessToken(): Flow<String>
    suspend fun saveLoggedUser(userRepositoryModel: UserRepositoryModel)
    fun  getLoggedUser(): Flow<UserRepositoryModel>
}