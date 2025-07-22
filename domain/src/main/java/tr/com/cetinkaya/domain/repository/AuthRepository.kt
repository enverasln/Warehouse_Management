package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.domain.model.user.UserDomainModel

interface AuthRepository {
    fun login(usernameOrEmail: String, password: String): Flow<UserDomainModel>

    fun getLoggedUser(): Flow<UserDomainModel>
}