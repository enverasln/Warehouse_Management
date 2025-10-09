package tr.com.cetinkaya.data_remote.data_source

import tr.com.cetinkaya.data_remote.api.CurrentAccountService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.toDataModel
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteCurrentAccountDataSource
import tr.com.cetinkaya.data_repository.models.current_account.GetCurrentAccountByTitleDataModel
import javax.inject.Inject

class RemoteCurrentAccountDataSourceImpl @Inject constructor(
    private val currentAccountService: CurrentAccountService, private val errorParser: ExceptionParser
) : RemoteCurrentAccountDataSource {


    override suspend fun getCurrentAccountsByTitle(title: String): List<GetCurrentAccountByTitleDataModel> {
        try {
            val response = currentAccountService.getAllByTitle(title)
            if (!response.isSuccessful) {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)

            }
            val responseBody = response.body()
            if (responseBody == null) throw Exception("Cari hesap bilgileri sunucudan okunamadı.")

            return responseBody.toDataModel()
        } catch (e: Exception) {
            throw e
        }
    }
}