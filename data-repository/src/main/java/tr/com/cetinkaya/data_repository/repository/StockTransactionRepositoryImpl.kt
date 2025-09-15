package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.DataOrigin
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.data_repository.datasource.local.LocalOrderTransactionDataSource
import tr.com.cetinkaya.data_repository.datasource.local.LocalStockTransactionDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockTransactionDataSource
import tr.com.cetinkaya.data_repository.models.order.toDomainModel
import tr.com.cetinkaya.data_repository.models.order.toStockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.size_transaction.toDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.toDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.toDomain
import tr.com.cetinkaya.data_repository.models.stock_transaction.toDomainModel
import tr.com.cetinkaya.data_repository.models.transferred_document.toDataModel
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.AddStockTransactionDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.CheckStockTxDocIsUsableDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionsByDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.transferred_document.AddTransferredDocumentDomainModel
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max

class StockTransactionRepositoryImpl @Inject constructor(
    private val remoteStockTransactionDataSource: RemoteStockTransactionDataSource,
    private val remoteStockDataSource: RemoteStockDataSource,
    private val localOrderDataSource: LocalOrderTransactionDataSource,
    private val localStockTransactionDataSource: LocalStockTransactionDataSource
) : StockTransactionRepository {

    override fun addStockTransaction(
        barcode: String,
        quantity: Double,
        selectedDocuments: List<DocumentDomainModel>,
        stockTransactionDocument: StockTransactionDocumentDomainModel,
        loggedUser: UserDomainModel
    ): Flow<Double> = flow {
        try {
            val mappedSelectedDocuments: List<Pair<String, Int>> = selectedDocuments.map { it.documentSeries to it.documentNumber }
            val orders = localOrderDataSource.getProductsByBarcode(barcode, mappedSelectedDocuments, loggedUser.warehouseNumber)

            if (orders.isEmpty()) {
                throw Exception("Barkodla eşleşen sipariş bulunamadı")
            }

            var remainingQty = quantity

            for (order in orders) {

                val orderRemainingQuantity = order.remainingQuantity - order.deliveredQuantity

                if (orderRemainingQuantity == 0.0) continue

                val qtyToInsert = minOf(orderRemainingQuantity, remainingQty)
                val deliveredQuantity = order.deliveredQuantity + qtyToInsert
                val updatedOrder = order.copy(deliveredQuantity = deliveredQuantity)

                localOrderDataSource.update(updatedOrder)


                val existStockTransaction = localStockTransactionDataSource.getStockTransactionByBarcode(
                    barcode = barcode,
                    documentSeries = stockTransactionDocument.documentSeries,
                    documentNumber = stockTransactionDocument.documentNumber,
                    orderId = order.id
                )

                if (existStockTransaction != null) {
                    val updatedStockTransaction = existStockTransaction.copy(
                        quantity = existStockTransaction.quantity + qtyToInsert, updatedAt = System.currentTimeMillis()
                    )
                    localStockTransactionDataSource.update(updatedStockTransaction)
                } else {
                    val mappedStockTransactionDocument = stockTransactionDocument.toDataModel()
                    val lineNumber = localStockTransactionDataSource.getNextLineNumber(
                        stockTransactionDocument.transactionType,
                        stockTransactionDocument.transactionKind,
                        stockTransactionDocument.isNormalOrReturn,
                        stockTransactionDocument.transactionDocumentType,
                        stockTransactionDocument.documentSeries,
                        stockTransactionDocument.documentNumber
                    )
                    val newItem = order.toStockTransactionDataModel(
                        stockTransactionDocument = mappedStockTransactionDocument,
                        lineNumber = lineNumber,
                        quantity = qtyToInsert,
                        userCode = loggedUser.mikroFlyUserId,
                        barcode = barcode,
                        synchronizationStatus = SyncStatus.New,
                    )
                    localStockTransactionDataSource.addStockTransaction(newItem)

                }
                remainingQty -= qtyToInsert

            }
            emit(remainingQty)
        } catch (e: Exception) {
            throw e
        }

    }

    override suspend fun add(stockTransaction: AddStockTransactionDomainModel): String {
        val toInsert = stockTransaction.toDataModel()
        return localStockTransactionDataSource.insertOrIncrement(toInsert)
    }

    override suspend fun addWithSizeTransactions(
        stockTransaction: AddStockTransactionDomainModel, sizeTransactions: List<AddSizeTransactionDomainModel>
    ): String {
        return localStockTransactionDataSource.addWithSizeTransaction(
            stockTransaction.toDataModel(), sizeTransactions.toDataModel()
        )
    }

    override suspend fun finishStockTransaction(
        stockTransactionDocument: StockTransactionDocumentDomainModel, transferredDocument: AddTransferredDocumentDomainModel
    ) {
        localStockTransactionDataSource.finishStockTransaction(
            stockTransactionDocument = stockTransactionDocument.toDataModel(), transferredDocument = transferredDocument.toDataModel()
        )
    }


    override suspend fun addAll(stockTransactions: List<StockTransactionDomainModel>): List<Long> {
        val (_, transactionType, transactionKind, isNormalOrReturn, stockTransactionDocumentType, _, documentSeries, documentNumber) = stockTransactions.first()
        val maxLineNumber = localStockTransactionDataSource.getNextLineNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = stockTransactionDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        ) + 1
        val insertedStockTransactions = stockTransactions.map { it.toDataModel().copy(lineNumber = maxLineNumber) }
        return localStockTransactionDataSource.upsertOrIncrement(insertedStockTransactions)
    }


    override suspend fun checkDocumentSeriesAndNumber(
        stockTxDoc: StockTransactionDocumentDomainModel, currentCode: String
    ): CheckStockTxDocIsUsableDomainModel {

        val mappedStockTxDoc = stockTxDoc.toDataModel()
        val result = remoteStockTransactionDataSource.checkStockTxDoc(stockTxDoc = mappedStockTxDoc, currentCode = currentCode)
        return result.toDomainModel()
    }


    override fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentDomainModel>> = localStockTransactionDataSource.getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType = transactionType,
        transactionKind = transactionKind,
        isNormalOrReturn = isNormalOrReturn,
        documentType = documentType,
        documentSeries = documentSeries,
        documentNumber = documentNumber
    ).map { stockTransactions ->
        stockTransactions.map { it.toDomain() }

    }

    override suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: SyncStatus) {
        localStockTransactionDataSource.updateStockTransactionSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    override suspend fun updateStockTransactionSyncStatus(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: SyncStatus,
        oldSyncStatus: SyncStatus
    ): Int {
        try {
            return localStockTransactionDataSource.updateStockTransactionSyncStatus(
                transactionType = transactionType,
                transactionKind = transactionKind,
                isNormalOrReturn = isNormalOrReturn,
                documentType = documentType,
                documentSeries = documentSeries,
                documentNumber = documentNumber,
                syncStatus = syncStatus,
                oldSyncStatus = oldSyncStatus
            )
        } catch (e: Exception) {
            throw e
        }

    }

    override suspend fun sendStockTransaction(stockTransaction: StockTransactionDomainModel): Boolean {
        val mappedStockTransaction = stockTransaction.toDataModel()
        return remoteStockTransactionDataSource.sendStockTransaction(mappedStockTransaction)
    }

    override fun getUnsyncedStockTransactions(): Flow<List<StockTransactionDomainModel>> {
        return localStockTransactionDataSource.getUnsyncedStockTransactions().map { it.map { data -> data.toDomain() } }
    }

    override fun getNextStockTransactionDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isStockTransactionNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Flow<StockTransactionDocumentDomainModel> {
        val remoteFlow = remoteStockTransactionDataSource.getNextStockTransactionDocument(
            transactionType, transactionKind, isStockTransactionNormalOrReturn, documentType, documentSeries
        ).map {
            it.toDomainModel()
        }

        val localFlow = localStockTransactionDataSource.getNextStockTransactionDocument(
            transactionType, transactionKind, isStockTransactionNormalOrReturn, documentType, documentSeries
        ).map {
            it.toDomainModel()
        }

        return combine(remoteFlow, localFlow) { remoteDoc, localDoc ->
            if (remoteDoc.documentNumber >= localDoc.documentNumber) remoteDoc else localDoc
        }
    }

    override suspend fun addWarehouseGoodsTransfer(
        stockCode: String,
        stockName: String,
        barcode: String,
        quantity: Double,
        price: Double,
        stockTransactionDocument: StockTransactionDocumentDomainModel,
        inputWarehouseNumber: Int,
        outputWarehouseNumber: Int,
        responsibilityCenter: String,
        userCode: Int,
        taxPointer: Byte,
        isColorizedAndSized: Boolean
    ) {

        val existStockTransaction = localStockTransactionDataSource.getStockTransactionByBarcode(
            barcode = barcode,
            documentSeries = stockTransactionDocument.documentSeries,
            documentNumber = stockTransactionDocument.documentNumber,
            orderId = ""
        )

        if (existStockTransaction != null) {
            val updatedStockTransaction = existStockTransaction.copy(
                quantity = existStockTransaction.quantity + quantity, updatedAt = System.currentTimeMillis()
            )
            localStockTransactionDataSource.update(updatedStockTransaction)
        } else {
            val lineNumber = localStockTransactionDataSource.getNextLineNumber(
                stockTransactionDocument.transactionType,
                stockTransactionDocument.transactionKind,
                stockTransactionDocument.isNormalOrReturn,
                stockTransactionDocument.transactionDocumentType,
                stockTransactionDocument.documentSeries,
                stockTransactionDocument.documentNumber
            )

            val stokTransaction = localStockTransactionDataSource.getStockTransactionByStockCodeAndDocument(
                stockCode = stockCode,
                transactionType = stockTransactionDocument.transactionType,
                transactionKind = stockTransactionDocument.transactionKind,
                isNormalOrReturn = stockTransactionDocument.isNormalOrReturn,
                transactionDocumentType = stockTransactionDocument.transactionDocumentType,
                documentNumber = stockTransactionDocument.documentNumber,
                documentSeries = stockTransactionDocument.documentSeries
            )

            val stockTransaction = StockTransactionDataModel(
                id = stokTransaction?.id ?: UUID.randomUUID().toString(),
                transactionType = stockTransactionDocument.transactionType,
                transactionKind = stockTransactionDocument.transactionKind,
                isNormalOrReturn = stockTransactionDocument.isNormalOrReturn,
                documentType = stockTransactionDocument.transactionDocumentType,
                documentDate = stockTransactionDocument.documentDate,
                documentSeries = stockTransactionDocument.documentSeries,
                documentNumber = stockTransactionDocument.documentNumber,
                lineNumber = stokTransaction?.lineNumber ?: lineNumber,
                stockCode = stockCode,
                stockName = stockName,
                companyCode = "",
                quantity = quantity,
                inputWarehouseNumber = inputWarehouseNumber,
                outputWarehouseNumber = outputWarehouseNumber,
                paymentPlanNumber = 0,
                salesman = "",
                responsibilityCenter = responsibilityCenter,
                userCode = userCode,
                totalPrice = price,
                discount1 = 0.0,
                discount2 = 0.0,
                discount3 = 0.0,
                discount4 = 0.0,
                discount5 = 0.0,
                taxPointer = taxPointer,
                orderId = "",
                price = price,
                paperNumber = stockTransactionDocument.paperNumber,
                companyNumber = 0,
                storeNumber = 0,
                barcode = barcode,
                isColoredAndSized = isColorizedAndSized,
                transportationStatus = 0,
                createdAt = Date().time,
                updatedAt = Date().time,
                syncStatus = SyncStatus.New,
                dataOrigin = DataOrigin.Local
            )

            localStockTransactionDataSource.addStockTransaction(stockTransaction)

        }


    }

    override fun getStockTransactionsByDocument(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): Flow<List<StockTransactionDomainModel>> = localStockTransactionDataSource.getStockTransactionsByDocument(
        transactionType = transactionType,
        transactionKind = transactionKind,
        isNormalOrReturn = isNormalOrReturn,
        documentType = documentType,
        documentSeries = documentSeries,
        documentNumber = documentNumber
    ).map { stockTransactions ->
        stockTransactions.map { it.toDomain() }
    }

    override suspend fun isDocumentUsed(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int,
        companyCode: String?,
        paperNumber: String?
    ): Boolean {
        return remoteStockTransactionDataSource.isDocumentUsed(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            companyCode = companyCode,
            paperNumber = paperNumber
        )
    }

    override suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
        documentSeries: String
    ): Int {
        val remoteDocumentNumber = remoteStockTransactionDataSource.getNextAvailableDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries
        )

        val localDocumentNumber = localStockTransactionDataSource.getNextAvailableDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries

        )

        return max(remoteDocumentNumber, localDocumentNumber)
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
        localStockTransactionDataSource.updateDocumentNumber(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = documentType,
            documentSeries = documentSeries,
            oldDocumentNumber = oldDocumentNumber,
            newDocumentNumber = newDocumentNumber
        )
    }

    override suspend fun getUnsyncedStockTransactions(
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
        documentSeries: String,
        documentNumber: Int
    ): List<StockTransactionDomainModel> {
        val dataModel = localStockTransactionDataSource.getUnsyncedStockTransactions(
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = transactionDocumentType,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        )

        return dataModel.map { it.toDomain() }
    }

    override suspend fun markStockTransactionSynced(stockTransaction: StockTransactionDomainModel) {
        val dataModel = stockTransaction.toDataModel().copy(syncStatus = SyncStatus.Transferred)
        localStockTransactionDataSource.update(dataModel)
    }

    override fun getStockTransactionDocumentByDocumentNumber(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): Flow<GetStockTransactionDocumentDomainModel?> {
        return remoteStockTransactionDataSource.getStockTransactionDocumentByDocumentNumber(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = transactionDocumentType
        ).map {
            it?.toDomainModel()
        }
    }

    override fun getStockTransactionDocumentByPaperNumberAndCurrentCode(
        documentSeries: String,
        documentNumber: Int,
        paperNumber: String,
        currentCode: String,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ): Flow<GetStockTransactionDocumentDomainModel?> {
        return remoteStockTransactionDataSource.getStockTransactionDocumentByPaperNumberAndCurrentCode(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            documentType = transactionDocumentType
        ).map {
            it?.toDomainModel()
        }
    }

    override suspend fun updateStockTransaction(stockTransaction: StockTransactionDomainModel): Int {
        return 0
    }

    override suspend fun removeStockTransaction(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType
    ) {

        val stockTransactions = localStockTransactionDataSource.getStockTransactions(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            transactionType = transactionType,
            transactionKind = transactionKind,
            isNormalOrReturn = isNormalOrReturn,
            transactionDocumentType = transactionDocumentType
        )

        val deletedStockTransaction = stockTransactions.filter { it.syncStatus == SyncStatus.New }

        localStockTransactionDataSource.removeStockTransaction(deletedStockTransaction)

    }

    override suspend fun countByDocument(stockTransactionDocument: StockTransactionDocumentDomainModel): Long {
        return localStockTransactionDataSource.getNextLineNumber(
            stockTransactionDocument.transactionType,
            stockTransactionDocument.transactionKind,
            stockTransactionDocument.isNormalOrReturn,
            stockTransactionDocument.transactionDocumentType,
            stockTransactionDocument.documentSeries,
            stockTransactionDocument.documentNumber
        )
    }

    override suspend fun markPending(stockTxDoc: StockTransactionDocumentDomainModel): Int =
        localStockTransactionDataSource.markPending(stockTxDoc.toDataModel())


}