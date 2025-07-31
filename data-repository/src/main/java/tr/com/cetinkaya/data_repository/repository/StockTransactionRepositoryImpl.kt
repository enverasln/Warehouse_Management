package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.data_repository.datasource.local.LocalOrderDataSource
import tr.com.cetinkaya.data_repository.datasource.local.LocalStockTransactionDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockTransactionDataSource
import tr.com.cetinkaya.data_repository.models.order.toStockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.toDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.toDomain
import tr.com.cetinkaya.data_repository.models.stocktransaction.toDomainModel
import tr.com.cetinkaya.domain.model.order.DocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.CheckDocumentSeriesAndNumberDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.GetStockTransactionsByDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDocumentDomainModel
import tr.com.cetinkaya.domain.model.stok_transaction.StockTransactionDomainModel
import tr.com.cetinkaya.domain.model.user.UserDomainModel
import tr.com.cetinkaya.domain.repository.StockTransactionRepository
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class StockTransactionRepositoryImpl @Inject constructor(
    private val remoteStockTransactionDataSource: RemoteStockTransactionDataSource,
    private val localOrderDataSource: LocalOrderDataSource,
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
                    documentNumber = stockTransactionDocument.documentNumber
                )

                if (existStockTransaction != null) {
                    val updatedStockTransaction = existStockTransaction.copy(
                        quantity = existStockTransaction.quantity + qtyToInsert, updatedAt = System.currentTimeMillis()
                    )
                    localStockTransactionDataSource.updateStockTransaction(updatedStockTransaction)
                } else {
                    val mappedStockTransactionDocument = stockTransactionDocument.toDataModel()
                    val lineNumber = localStockTransactionDataSource.getCountByDocuments(mappedStockTransactionDocument)
                    val newItem = order.toStockTransactionDataModel(
                        stockTransactionDocument = mappedStockTransactionDocument,
                        lineNumber = lineNumber,
                        quantity = qtyToInsert,
                        userCode = loggedUser.mikroFlyUserId,
                        barcode = barcode,
                        synchronizationStatus = "Yeni kayıt",
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


    override fun checkDocumentSeriesAndNumber(
        documentSeries: String,
        documentNumber: Int,
        companyCode: String,
        paperNumber: String,
        stockTransactionType: StockTransactionTypes,
        stockTransactionKind: StockTransactionKinds,
        documentType: Int,
        isNormalOrReturn: Int
    ): Flow<CheckDocumentSeriesAndNumberDomainModel> = remoteStockTransactionDataSource.checkDocumentIsUsable(
        documentSeries = documentSeries,
        documentNumber = documentNumber,
        companyCode = companyCode,
        paperNumber = paperNumber,
        stockTransactionType = stockTransactionType,
        stockTransactionKind = stockTransactionKind,
        documentType = documentType,
        isNormalOrReturn = isNormalOrReturn
    ).map {
        CheckDocumentSeriesAndNumberDomainModel(
            message = it.message, isDocumentNew = it.isDocumentNew
        )
    }

    override fun getStockTransactionsByDocumentWithRemainingQuantity(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: Byte,
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

    override suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String) {
        localStockTransactionDataSource.updateStockTransactionSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    override suspend fun updateStockTransactionSyncStatus(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: Byte,
        documentSeries: String,
        documentNumber: Int,
        syncStatus: String
    ): Int {
        try {
            return localStockTransactionDataSource.updateStockTransactionSyncStatus(
                transactionType = transactionType,
                transactionKind = transactionKind,
                isNormalOrReturn = isNormalOrReturn,
                documentType = documentType,
                documentSeries = documentSeries,
                documentNumber = documentNumber,
                syncStatus = syncStatus
            )
        } catch (e: Exception) {
            throw e
        }

    }

    override suspend fun sendStockTransaction(stockTransaction: StockTransactionDomainModel) {
        val stockTransactionDataModel = stockTransaction.toDataModel()
        remoteStockTransactionDataSource.sendStockTransaction(stockTransactionDataModel)
    }

    override fun getUnsyncedStockTransactions(): Flow<List<StockTransactionDomainModel>> {
        return localStockTransactionDataSource.getUnsyncedStockTransactions().map { it.map { data -> data.toDomain() } }
    }

    override fun getNextStockTransactionDocument(
        stockTransactionType: StockTransactionTypes,
        stockTransactionKind: StockTransactionKinds,
        isStockTransactionNormalOrReturn: Byte,
        stockTransactionDocumentType: Byte,
        documentSeries: String
    ): Flow<StockTransactionDocumentDomainModel> {
        val remoteFlow = remoteStockTransactionDataSource.getNextStockTransactionDocument(
            stockTransactionType, stockTransactionKind, isStockTransactionNormalOrReturn, stockTransactionDocumentType, documentSeries
        ).map {
            it.toDomainModel()
        }

        val localFlow = localStockTransactionDataSource.getNextStockTransactionDocument(
            stockTransactionType, stockTransactionKind, isStockTransactionNormalOrReturn, stockTransactionDocumentType, documentSeries
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
            barcode = barcode, documentSeries = stockTransactionDocument.documentSeries, documentNumber = stockTransactionDocument.documentNumber
        )

        if (existStockTransaction != null) {
            val updatedStockTransaction = existStockTransaction.copy(
                quantity = existStockTransaction.quantity + quantity, updatedAt = System.currentTimeMillis()
            )
            localStockTransactionDataSource.updateStockTransaction(updatedStockTransaction)
        } else {
            val lineNumber = localStockTransactionDataSource.getCountByDocuments(stockTransactionDocument.toDataModel())


            val stockTransaction = StockTransactionDataModel(
                id = UUID.randomUUID().toString(),
                transactionType = stockTransactionDocument.transactionType,
                transactionKind = stockTransactionDocument.transactionKind,
                isNormalOrReturn = stockTransactionDocument.isNormalOrReturn,
                documentType = stockTransactionDocument.documentType,
                documentDate = stockTransactionDocument.documentDate,
                documentSeries = stockTransactionDocument.documentSeries,
                documentNumber = stockTransactionDocument.documentNumber,
                lineNumber = lineNumber,
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
                synchronizationStatus = "Yeni Kayıt"
            )

            localStockTransactionDataSource.addStockTransaction(stockTransaction)

        }


    }

    override fun getStockTransactionsByDocument(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: Byte,
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

}