package tr.com.cetinkaya.data_remote.models.auth.login

data class LoginRequestRemoteModel(
    val usernameOrEmail: String, val password: String
)