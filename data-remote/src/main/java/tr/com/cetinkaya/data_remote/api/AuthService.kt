package tr.com.cetinkaya.data_remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tr.com.cetinkaya.data_remote.models.auth.login.LoginRequestRemoteModel
import tr.com.cetinkaya.data_remote.models.auth.login.LoginResponseRemoteModel

interface AuthService {

    @POST(LOGIN)
    suspend fun login(@Body loginRequest: LoginRequestRemoteModel) : Response<LoginResponseRemoteModel>

    companion object {
        private const val LOGIN = "authserver/auth/login"
    }
}