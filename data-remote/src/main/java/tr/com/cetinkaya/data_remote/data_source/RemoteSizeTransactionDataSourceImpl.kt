package tr.com.cetinkaya.data_remote.data_source

import tr.com.cetinkaya.data_remote.api.SizeTransactionService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.size_transaction.toRequestModel
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteSizeTransactionDataSource
import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel
import javax.inject.Inject

class RemoteSizeTransactionDataSourceImpl @Inject constructor(
    private val sizeTransactionService: SizeTransactionService,
    private val errorParser: ExceptionParser
) : RemoteSizeTransactionDataSource {

    override suspend fun sendSizeTransaction(sizeTransaction: List<SizeTransactionDataModel>) =
        try {
            val requests = sizeTransaction.toRequestModel()

            val response = sizeTransactionService.sendSizeTransaction(requests)

            if (!response.isSuccessful) {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }
            true
        } catch (e: Exception) {
            throw e
        }

}