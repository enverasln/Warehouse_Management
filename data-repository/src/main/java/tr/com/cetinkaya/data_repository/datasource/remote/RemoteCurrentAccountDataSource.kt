package tr.com.cetinkaya.data_repository.datasource.remote

import tr.com.cetinkaya.data_repository.models.current_account.GetCurrentAccountByTitleDataModel

interface RemoteCurrentAccountDataSource {
    suspend fun getCurrentAccountsByTitle(title: String) : List<GetCurrentAccountByTitleDataModel>
}