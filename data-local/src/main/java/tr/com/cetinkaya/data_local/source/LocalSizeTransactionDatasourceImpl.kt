package tr.com.cetinkaya.data_local.source

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.data_local.db.AppDatabase
import tr.com.cetinkaya.data_local.db.dao.SizeTransactionDao
import tr.com.cetinkaya.data_local.db.entities.SizeTransactionEntity
import tr.com.cetinkaya.data_local.db.entities.toProductDataModel
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_repository.datasource.local.LocalSizeTransactionDataSource
import tr.com.cetinkaya.data_repository.models.size_transaction.AddSizeTransactionDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel
import javax.inject.Inject

class LocalSizeTransactionDatasourceImpl @Inject constructor (
    private val db: AppDatabase,
    private val sizeTransactionDao: SizeTransactionDao
) : LocalSizeTransactionDataSource {

    override suspend fun insertOne(sizeTransaction: SizeTransactionDataModel): Long {
        val toInsert = sizeTransaction.toEntity()

        return sizeTransactionDao.add(toInsert)
    }

    override suspend fun addAll(sizeTransactions: List<AddSizeTransactionDataModel>): List<Long> {
        val ids = mutableListOf<Long>()

        for (sizeTransaction in sizeTransactions) {
            val existSizeTransaction = sizeTransactionDao.getByBarcodeAndRefRecord(
                barcode = sizeTransaction.barcode,
                refRecordId = sizeTransaction.refRecordId,
                sizeTransactionType = sizeTransaction.sizeTransactionType
            )

            if(existSizeTransaction != null) {
                val toUpdateSizeTransaction = existSizeTransaction.copy(
                    quantity = existSizeTransaction.quantity + sizeTransaction.quantity
                )
                sizeTransactionDao.update(toUpdateSizeTransaction)
                continue
            }

            val toInsertSizeTransaction = SizeTransactionEntity.create(
                barcode = sizeTransaction.barcode,
                refRecordId = sizeTransaction.refRecordId,
                sizeTransactionType = sizeTransaction.sizeTransactionType,
                quantity = sizeTransaction.quantity
            )

            val id = sizeTransactionDao.add(toInsertSizeTransaction)
            ids.add(id)
        }
        return ids;
    }

    override suspend fun getAllByRefRecordIdAndSizeTransactionType(
        refRecordId: String,
        sizeTransactionType: SizeTransactionType
    ): List<SizeTransactionDataModel>? {
        return sizeTransactionDao.getAllByRefRecordIdAndSizeTransactionType(
            refRecordId = refRecordId,
            sizeTransactionType = sizeTransactionType
        )?.map { it.toProductDataModel() }

    }
}