package tr.com.cetinkaya.data_remote.data_source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okio.IOException
import tr.com.cetinkaya.data_remote.api.AuthService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.auth.login.LoginRequestRemoteModel
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteAuthDataSource
import tr.com.cetinkaya.data_repository.models.auth.TokenRepositoryModel
import javax.inject.Inject

class RemoteAuthDataSourceImpl @Inject constructor(
    private val authService: AuthService, private val errorParser: ExceptionParser
) : RemoteAuthDataSource {

    override fun login(usernameOrEmail: String, password: String): Flow<TokenRepositoryModel> = flow {
        val request = LoginRequestRemoteModel(usernameOrEmail, password)
        val response = authService.login(request)

        if (response.isSuccessful) {
            val body = response.body() ?: throw IOException("Sunucudan boş veri geldi.")
            emit(body)
        } else {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString("\n") ?: "Sunucu hatası"
            throw Exception(message)
        }
    }.map { token ->
        TokenRepositoryModel(
            TokenRepositoryModel.AccessToken(
                token.accessToken.token, token.accessToken.expirationDate
            )
        )
    }

}