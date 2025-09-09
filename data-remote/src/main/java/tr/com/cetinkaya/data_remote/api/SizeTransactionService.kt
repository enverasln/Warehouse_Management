package tr.com.cetinkaya.data_remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import tr.com.cetinkaya.data_remote.models.size_transaction.AddSizeTransactionRequest

interface SizeTransactionService {

    @POST(SIZE_TRANSACTIONS)
    suspend fun sendSizeTransaction(
        @Body body: List<AddSizeTransactionRequest>
    ): Response<Unit>

    companion object {
        private const val SIZE_TRANSACTIONS = "depo-service/size-transactions"
    }
}