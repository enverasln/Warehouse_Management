package tr.com.cetinkaya.domain.repository

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.SizeTransactionDomainModel

interface SizeTransactionRepository {
    suspend fun getAllByRefRecordIdAndSizeTransactionType(refRecordId: String, sizeTransactionType: SizeTransactionType) : List<SizeTransactionDomainModel>?
    suspend fun add(sizeTransaction: SizeTransactionDomainModel)
    suspend fun addAll(sizeTransactions: List<AddSizeTransactionDomainModel>) : List<Long>
    suspend fun sendSizeTransaction(sizeTransactions: List<SizeTransactionDomainModel>)
}