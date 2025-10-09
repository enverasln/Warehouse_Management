package tr.com.cetinkaya.data_repository.repository

import tr.com.cetinkaya.data_repository.datasource.remote.RemoteCurrentAccountDataSource
import tr.com.cetinkaya.data_repository.models.current_account.toDomainModel
import tr.com.cetinkaya.domain.model.current_account.GetCurrentAccountByTitleDomainModel
import tr.com.cetinkaya.domain.repository.CurrentAccountRepository
import javax.inject.Inject

class CurrentAccountRepositoryImpl @Inject constructor(
    private val remoteCurrentAccountDataSource: RemoteCurrentAccountDataSource
) : CurrentAccountRepository {

    override suspend fun getCurrentAccountsByTitle(title: String): List<GetCurrentAccountByTitleDomainModel> {
        val currentAccounts = remoteCurrentAccountDataSource.getCurrentAccountsByTitle(title)
        val mappedCurrentAccounts = currentAccounts.toDomainModel()
        return mappedCurrentAccounts

    }
}