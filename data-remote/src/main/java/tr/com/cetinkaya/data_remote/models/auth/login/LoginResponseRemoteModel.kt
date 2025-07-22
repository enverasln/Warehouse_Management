package tr.com.cetinkaya.data_remote.models.auth.login

import java.util.Date

data class LoginResponseRemoteModel(
    val accessToken: AccessToken
) {
    data class AccessToken(
        val token: String,
        val expirationDate: Date
    )
}