package tr.com.cetinkaya.data_repository.models.auth

import java.util.Date

class TokenRepositoryModel(
    val accessToken: AccessToken
) {
    data class AccessToken(
        val token:String, val expirationDate: Date
    )
}