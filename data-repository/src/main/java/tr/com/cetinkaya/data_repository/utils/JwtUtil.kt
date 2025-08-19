package tr.com.cetinkaya.data_repository.utils

import tr.com.cetinkaya.data_repository.models.user.UserRepositoryModel
import com.auth0.android.jwt.JWT

object JwtUtil {
    fun parseToken(token: String) : UserRepositoryModel {
        return try {
            val jwt = JWT(token)

            UserRepositoryModel(
                username = jwt.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name").asString() ?:"",
                email = jwt.getClaim("email").asString() ?: "",
                warehouseName = jwt.getClaim("warehouseName").asString() ?: "",
                warehouseNumber = jwt.getClaim("warehouseNumber").asInt() ?: 0,
                mikroFlyUserId = jwt.getClaim("mikroFlyUserId").asInt() ?: 0,
                documentSeries = jwt.getClaim("documentSeries").asString() ?: "",
                newOrderDocumentSeries = jwt.getClaim("orderDocumentSeries").asString() ?: "",
                warehouseLockDate = 0L
            )

        } catch (ex:Exception) {
            throw ex
        }
    }
}