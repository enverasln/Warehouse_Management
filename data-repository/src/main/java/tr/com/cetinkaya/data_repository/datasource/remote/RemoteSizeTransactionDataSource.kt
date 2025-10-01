package tr.com.cetinkaya.data_repository.datasource.remote

import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel

interface RemoteSizeTransactionDataSource {

    suspend fun sendSizeTransaction(sizeTransaction: List<SizeTransactionDataModel>) : Boolean
}