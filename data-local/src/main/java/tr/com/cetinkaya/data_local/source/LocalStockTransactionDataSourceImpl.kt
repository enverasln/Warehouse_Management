package tr.com.cetinkaya.data_local.source

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.data_local.db.AppDatabase
import tr.com.cetinkaya.data_local.db.dao.SizeTransactionDao
import tr.com.cetinkaya.data_local.db.dao.StockTransactionDao
import tr.com.cetinkaya.data_local.db.dao.TransferredDocumentDao
import tr.com.cetinkaya.data_local.db.entities.SizeTransactionEntity
import tr.com.cetinkaya.data_local.db.entities.StockTransactionEntity
import tr.com.cetinkaya.data_local.db.entities.TransferredDocumentEntity
import tr.com.cetinkaya.data_local.db.entities.toDataModel
import tr.com.cetinkaya.data_local.db.entities.toEntity
import tr.com.cetinkaya.data_local.models.stok_transaction.toDataModel
import tr.com.cetinkaya.data_repository.datasource.local.LocalStockTransactionDataSource
import tr.com.cetinkaya.data_repository.models.size_transaction.AddSizeTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.AddStockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.GetStockTransactionsByDocumentDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel
import tr.com.cetinkaya.data_repository.models.transferred_document.AddTransferredDocumentDataModel
import java.util.Date
import javax.inject.Inject

class LocalStockTransactionDataSourceImpl @Inject constructor(
    private val db: AppDatabase,
    private val stockTransactionDao: StockTransactionDao,
    private val sizeTransactionDao: SizeTransactionDao,
    private val transferredDocumentDao: TransferredDocumentDao
) : LocalStockTransactionDataSource {

    override suspend fun addStockTransaction(stockTransaction: StockTransactionDataModel) {
        val stockTransactionLocalModel = stockTransaction.toEntity()

        stockTransactionDao.insertOne(stockTransactionLocalModel)
    }

    override suspend fun finishStockTransaction(
        stockTransactionDocument: StockTransactionDocumentDataModel,
        transferredDocument: AddTransferredDocumentDataModel
    ) = db.withTransaction {

        val toTransferRecords = stockTransactionDao.getAllByDocumentAndSyncStatus(
            transactionType = stockTransactionDocument.transactionType,
            transactionKind = stockTransactionDocument.transactionKind,
            isNormalOrReturn = stockTransactionDocument.isNormalOrReturn,
            transactionDocumentType = stockTransactionDocument.documentType,
            documentSeries = stockTransactionDocument.documentSeries,
            documentNumber = stockTransactionDocument.documentNumber,
            syncStatus = SyncStatus.New
        )

        val toUpdateRecords = toTransferRecords.map { it.copy(syncStatus = SyncStatus.ToTransfer) }

        val rowCount = stockTransactionDao.updateAll(toUpdateRecords)

        val toInsertTransferredDocument = TransferredDocumentEntity.create(
            transferredDocumentType = transferredDocument.transferredDocumentType,
            documentSeries = transferredDocument.documentSeries,
            documentNumber = transferredDocument.documentNumber,
            currentCode = transferredDocument.currentCode,
            paperNumber = transferredDocument.paperNumber,
            synchronizationStatus = false,
            description = SyncStatus.ToTransfer.description        )

        transferredDocumentDao.add(toInsertTransferredDocument)

        return@withTransaction
    }

    override suspend fun upsertOrIncrement(stockTransactions: List<StockTransactionDataModel>): List<Long> {
        val ids = mutableListOf<Long>()
        for (stockTransaction in stockTransactions) {
            val updated = stockTransactionDao.incrementIfExists(
                documentSeries = stockTransaction.documentSeries,
                documentNumber = stockTransaction.documentNumber,
                transactionType = stockTransaction.transactionType,
                transactionKind = stockTransaction.transactionKind,
                isNormalOrReturn = stockTransaction.isNormalOrReturn,
                transactionDocumentType = stockTransaction.documentType,
                stockCode = stockTransaction.stockCode,
                deltaQuantity = stockTransaction.quantity,
                deltaTotalPrice = stockTransaction.totalPrice
            )

            if (updated == 0) {
                try {

                    val existedStockTransaction = stockTransactionDao.getStockTransactionIdByStockCodeAndDocument(
                        stockCode = stockTransaction.stockCode,
                        transactionType = stockTransaction.transactionType,
                        transactionKind = stockTransaction.transactionKind,
                        isNormalOrReturn = stockTransaction.isNormalOrReturn,
                        transactionDocumentType = stockTransaction.documentType,
                        documentSeries = stockTransaction.documentSeries,
                        documentNumber = stockTransaction.documentNumber
                    )

                    if (existedStockTransaction == null) {
                        ids += stockTransactionDao.insertOne(stockTransaction.toEntity())
                    } else {
                        val toInsert = stockTransaction.copy(id = existedStockTransaction.id, lineNumber = existedStockTransaction.lineNumber)
                        ids += stockTransactionDao.insertOne(toInsert.toEntity())
                    }


                } catch (ex: SQLiteConstraintException) {
                    stockTransactionDao.incrementIfExists(
                        documentSeries = stockTransaction.documentSeries,
                        documentNumber = stockTransaction.documentNumber,
                        transactionType = stockTransaction.transactionType,
                        transactionKind = stockTransaction.transactionKind,
                        isNormalOrReturn = stockTransaction.isNormalOrReturn,
                        transactionDocumentType = stockTransaction.documentType,
                        stockCode = stockTransaction.stockCode,
                        deltaQuantity = stockTransaction.quantity,
                        deltaTotalPrice = stockTransaction.totalPrice
                    )
                }
            }
        }
        return ids
    }

    override suspend fun insertOrIncrement(stockTransaction: AddStockTransactionDataModel): String {
        // If a row exists for the same document + stock
        // increase quantity and totalPrice by the given deltas.
        val existStockTransaction = stockTransactionDao.getByDocumentAndStockCode(
            stockTransaction.documentSeries,
            stockTransaction.documentNumber,
            stockTransaction.transactionType,
            stockTransaction.transactionKind,
            stockTransaction.isNormalOrReturn,
            stockTransaction.transactionDocumentType,
            stockTransaction.stockCode
        )
        if (existStockTransaction != null) {
            val toUpdateStockTransaction = existStockTransaction.copy(
                quantity = existStockTransaction.quantity + stockTransaction.quantity,
                totalPrice = existStockTransaction.totalPrice + stockTransaction.totalPrice
            )
            stockTransactionDao.update(toUpdateStockTransaction)
            return existStockTransaction.id
        }
        val lineNumber = stockTransactionDao.getNextLineNumber(
            documentSeries = stockTransaction.documentSeries,
            documentNumber = stockTransaction.documentNumber,
            transactionType = stockTransaction.transactionType,
            transactionKind = stockTransaction.transactionKind,
            isNormalOrReturn = stockTransaction.isNormalOrReturn,
            transactionDocumentType = stockTransaction.transactionDocumentType
        )
        // If a row does not exist for the same document + stock
        // insert a new row.
        val toInsert = StockTransactionEntity.create(
            transactionType = stockTransaction.transactionType,
            transactionKind = stockTransaction.transactionKind,
            isNormalOrReturn = stockTransaction.isNormalOrReturn,
            documentType = stockTransaction.transactionDocumentType,
            documentDate = stockTransaction.documentDate,
            documentSeries = stockTransaction.documentSeries,
            documentNumber = stockTransaction.documentNumber,
            lineNumber = lineNumber,
            stockCode = stockTransaction.stockCode,
            stockName = stockTransaction.stockName,
            companyCode = stockTransaction.currentCode,
            quantity = stockTransaction.quantity,
            inputWarehouseNumber = stockTransaction.inputWarehouseNumber,
            outputWarehouseNumber = stockTransaction.outputWarehouseNumber,
            paymentPlanNumber = stockTransaction.paymentPlanNumber,
            salesman = stockTransaction.salesman,
            responsibilityCenter = stockTransaction.responsibilityCenter,
            userCode = stockTransaction.userCode,
            totalPrice = stockTransaction.totalPrice,
            discount1 = stockTransaction.discount1,
            discount2 = stockTransaction.discount2,
            discount3 = stockTransaction.discount3,
            discount4 = stockTransaction.discount4,
            discount5 = stockTransaction.discount5,
            taxPointer = stockTransaction.taxPointer,
            orderId = stockTransaction.orderId,
            price = stockTransaction.price,
            paperNumber = stockTransaction.paperNumber,
            companyNumber = stockTransaction.companyNumber,
            storeNumber = stockTransaction.storeNumber,
            barcode = stockTransaction.barcode,
            isColoredAndSized = stockTransaction.isColoredAndSized,
            transportationStatus = stockTransaction.transportationStatus,
            syncStatus = SyncStatus.New
        )

        val insertedCount = stockTransactionDao.insertOne(toInsert)

        if (insertedCount <= 0) throw Exception("Stok hareketi kayıt edilirken bir hata oluştu.")
        return toInsert.id
    }

    override suspend fun insertWithSizeTransaction(
        stockTransaction: AddStockTransactionDataModel,
        sizeTransactions: List<AddSizeTransactionDataModel>
    ): String = db.withTransaction {
        val stockTransId = insertOrIncrement(stockTransaction)


        if (sizeTransactions.isNotEmpty()) {
            val withRef = sizeTransactions.map {
                SizeTransactionEntity.create(
                    it.barcode,
                    stockTransId,
                    it.sizeTransactionType,
                    it.quantity
                )
            }

            withRef.forEach { sizeTransactions ->
                val exist = sizeTransactionDao.getByBarcodeAndRefRecord(
                    barcode = sizeTransactions.barcode,
                    refRecordId = sizeTransactions.refRecordId,
                    sizeTransactionType = sizeTransactions.sizeTransactionType
                )

                if (exist != null) {
                    sizeTransactionDao.update(exist.copy(quantity = exist.quantity + sizeTransactions.quantity))
                } else {
                    sizeTransactionDao.insertOne(sizeTransactions)
                }
            }
        }
        stockTransId
    }


    override suspend fun getNextLineNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Long = stockTransactionDao.getNextLineNumber(

        transactionType = transactionType,
        transactionKind = transactionKind,
        isNormalOrReturn = isNormalOrReturn,
        transactionDocumentType = transactionDocumentType,
        documentSeries = documentSeries,
        documentNumber = documentNumber,
    )

    override fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentDataModel>> = stockTransactionDao.getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType = transactionType,
        transactionKind = transactionKind,
        isNormalOrReturn = isNormalOrReturn,
        documentType = documentType,
        documentSeries = documentSeries,
        documentNumber = documentNumber
    ).map {
        it.map { getStockTransactionsByDocumentLocalModel ->
            getStockTransactionsByDocumentLocalModel.toDataModel()
        }

    }

    override suspend fun getStockTransactionByBarcode(
        barcode: String, documentSeries: String, documentNumber: Int, orderId: String
    ): StockTransactionDataModel? {
        val stockTransaction = stockTransactionDao.getStockTransactionByBarcode(
            barcode = barcode, documentSeries = documentSeries, documentNumber = documentNumber, orderId = orderId
        )
        return stockTransaction?.toDataModel()
    }

    override suspend fun update(stockTransaction: StockTransactionDataModel): Int {
        val stockTransactionEntity = stockTransaction.toEntity()
        return stockTransactionDao.update(stockTransactionEntity)
    }

    override suspend fun updateStockTransactionSyncStatus(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: SyncStatus,
        oldSyncStatus: SyncStatus,
    ): Int {
        return stockTransactionDao.updateStockTransactionSyncStatus(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            syncStatus = syncStatus,
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            oldSyncStatus = oldSyncStatus
        )
    }

    override suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: SyncStatus) {
        stockTransactionDao.updateStockTransactionSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    override fun getUnsyncedStockTransactions(): Flow<List<StockTransactionDataModel>> {
        return stockTransactionDao.getBySyncStatus(SyncStatus.ToTransfer).map {
            it.map { data -> data.toDataModel() }
        }
    }

    override fun getStockTransactionsByDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<StockTransactionDataModel>> {
        return stockTransactionDao.getStockTransactionsByDocument(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        ).map {
            it.map { data -> data.toDataModel() }
        }
    }

    override fun getNextStockTransactionDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isStockTransactionNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel> {
        return stockTransactionDao.getNextStockTransactionDocument(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isStockTransactionNormalOrReturn = isStockTransactionNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries
        ).map { document ->


            var nextDocument = StockTransactionDocumentDataModel(
                documentDate = Date().time,
                documentSeries = documentSeries,
                documentNumber = 1,
                paperNumber = "",
                transactionType = transactionType,
                transactionKind = transactionKind,
                isNormalOrReturn = isStockTransactionNormalOrReturn,
                documentType = documentType
            )

            if (document != null) {
                nextDocument = nextDocument.copy(documentNumber = document.documentNumber + 1)
            }

            nextDocument

        }
    }

    override suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Int {
        val documentNumber = stockTransactionDao.getNextAvailableDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries
        )

        return if (documentNumber == null) 1 else documentNumber + 1
    }

    override suspend fun markStockTransactionSynced(stockTransaction: StockTransactionDataModel) {
        stockTransactionDao.markStockTransactionSynced(stockTransaction.id, stockTransaction.barcode)
    }

    override suspend fun getUnsyncedStockTransactions(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): List<StockTransactionDataModel> {
        return stockTransactionDao.getUnsyncedStockTransactions(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = transactionDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        ).map {
            it.toDataModel()
        }
    }

    override suspend fun updateDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        oldDocumentNumber: Int,
        newDocumentNumber: Int
    ) {
        stockTransactionDao.updateDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries,
            oldDocumentNumber = oldDocumentNumber,
            newDocumentNumber = newDocumentNumber
        )
    }

    override suspend fun getStockTransactionByStockCodeAndDocument(
        stockCode: String,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): StockTransactionDataModel? {

        val result = stockTransactionDao.getStockTransactionIdByStockCodeAndDocument(
            stockCode = stockCode,
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = transactionDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        )

        return result?.toDataModel()
    }

    override suspend fun removeStockTransaction(removedStockTransactions: List<StockTransactionDataModel>) {
        stockTransactionDao.removeStockTransactions(removedStockTransactions.map { it.toEntity() })
    }

    override suspend fun getStockTransactions(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): List<StockTransactionDataModel> {
        return stockTransactionDao.getStockTransactions(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = transactionDocumentType
        )?.map { it.toDataModel() } ?: emptyList()
    }
}