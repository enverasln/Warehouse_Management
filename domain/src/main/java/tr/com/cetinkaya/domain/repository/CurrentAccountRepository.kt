package tr.com.cetinkaya.domain.repository

import tr.com.cetinkaya.domain.model.current_account.GetCurrentAccountByTitleDomainModel

interface CurrentAccountRepository {
    suspend fun getCurrentAccountsByTitle(title: String): List<GetCurrentAccountByTitleDomainModel>
}