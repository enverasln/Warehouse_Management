package tr.com.cetinkaya.data_repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.data_repository.datasource.local.LocalOrderDataSource
import tr.com.cetinkaya.data_repository.datasource.local.LocalStockTransactionDataSource
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockTransactionDataSource
import tr.com.cetinkaya.data_repository.models.order.toStockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel
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
        stockTransactionType: Int,
        stockTransactionKind: Int,
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

    override fun getStockTransactionsByDocument(
        transactionType: Byte, transactionKind: Byte, isNormalOrReturn: Byte, documentType: Byte, documentSeries: String, documentNumber: Int
    ): Flow<List<GetStockTransactionsByDocumentDomainModel>> = localStockTransactionDataSource.getStockTransactionsByDocument(
        transactionType = transactionType,
        transactionKind = transactionKind,
        isNormalOrReturn = isNormalOrReturn,
        documentType = documentType,
        documentSeries = documentSeries,
        documentNumber = documentNumber
    ).map {
        it.map { it2 -> it2.toDomain() }

    }

    override suspend fun updateStockTransactionSyncStatus(documentSeries: String, documentNumber: Int, syncStatus: String) {
        localStockTransactionDataSource.updateStockTransactionSyncStatus(documentSeries, documentNumber, syncStatus)
    }

    override suspend fun sendStockTransaction(stockTransaction: StockTransactionDomainModel) {
        val stockTransactionDataModel = stockTransaction.toDataModel()
        remoteStockTransactionDataSource.sendStockTransaction(stockTransactionDataModel)
    }

    override fun getUnsyncedStockTransactions(): Flow<List<StockTransactionDomainModel>> {
        return localStockTransactionDataSource.getUnsyncedStockTransactions().map { it.map { data -> data.toDomain() } }
    }

    override fun getNextStockTransactionDocument(
        stockTransactionType: Byte,
        stockTransactionKind: Byte,
        isStockTransactionNormalOrReturn: Byte,
        stockTransactionDocumentType: Byte,
        documentSeries: String
    ): Flow<StockTransactionDocumentDomainModel> = remoteStockTransactionDataSource.getNextStockTransactionDocument(
        stockTransactionType, stockTransactionKind, isStockTransactionNormalOrReturn, stockTransactionDocumentType, documentSeries
    ).map {
        it.toDomainModel()
    }

}