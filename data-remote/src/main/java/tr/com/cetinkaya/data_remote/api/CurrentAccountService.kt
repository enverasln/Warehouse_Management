package tr.com.cetinkaya.data_remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tr.com.cetinkaya.data_remote.models.GetCurrentAccountByTitleResponseModel

interface CurrentAccountService {

    @GET(GET_ALL_BY_TITLE)
    suspend fun getAllByTitle(@Query("title") title: String): Response<List<GetCurrentAccountByTitleResponseModel>>

    companion object {
        private const val CURRENT_ACCOUNTS = "depo-service/current-accounts"
        private const val GET_ALL_BY_TITLE = "$CURRENT_ACCOUNTS/by-title"
    }
}