package tr.com.cetinkaya.data_repository.datasource.remote

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.auth.TokenRepositoryModel

interface RemoteAuthDataSource {

    fun login(usernameOrEmail: String, password: String) : Flow<TokenRepositoryModel>
}