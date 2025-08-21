package tr.com.cetinkaya.data_remote.data_source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.common.enums.StockTransactionDocumentTypes
import tr.com.cetinkaya.common.enums.StockTransactionKinds
import tr.com.cetinkaya.common.enums.StockTransactionTypes
import tr.com.cetinkaya.data_remote.api.StockTransactionService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.stock_transaction.addStocktransaction.toRequest
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockTransactionDataSource
import tr.com.cetinkaya.data_repository.models.order.CheckDocumentIsUsableRepositoryModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stocktransaction.StockTransactionDocumentDataModel
import java.io.IOException
import java.util.Date
import javax.inject.Inject

class RemoteStockTransactionDataSourceImpl @Inject constructor(
    private val stockTransactionService: StockTransactionService, private val errorParser: ExceptionParser
) : RemoteStockTransactionDataSource {

    override fun checkDocumentIsUsable(
        documentSeries: String,
        documentNumber: Int,
        companyCode: String,
        paperNumber: String,
        stockTransactionType: StockTransactionTypes,
        stockTransactionKind: StockTransactionKinds,
        documentType: StockTransactionDocumentTypes,
        isNormalOrReturn: Byte
    ): Flow<CheckDocumentIsUsableRepositoryModel> = flow {

        val response = stockTransactionService.checkDocumentIsUsable(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            companyCode = companyCode,
            paperNumber = paperNumber,
            stockTransactionType = stockTransactionType.value,
            stockTransactionKind = stockTransactionKind.value,
            documentType = documentType.value,
            isNormalOrReturn = isNormalOrReturn
        )

        if (response.isSuccessful) {
            val body = response.body() ?: throw IOException("Sunucudan boş veri geldi.")
            emit(body)
        } else {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
    }.map { result ->
        CheckDocumentIsUsableRepositoryModel(
            message = result.message, isDocumentNew = result.isDocumentNew
        )
    }

    override suspend fun sendStockTransaction(stockTransaction: StockTransactionDataModel) : Boolean =
        try {
            val request = stockTransaction.toRequest()
            val response = stockTransactionService.sendStockTransaction(request)
            if (!response.isSuccessful) {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }
            true
        } catch (e: Exception) {
            throw e
        }


    override fun getNextStockTransactionDocument(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isStockTransactionNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentTypes,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel> = flow {
        val response = stockTransactionService.getNextStockTransactionDocument(
            stockTransactionType = transactionType.value,
            stockTransactionKind = transactionKind.value,
            isNormalOrReturn = isStockTransactionNormalOrReturn,
            stockTransactionDocumentType = transactionDocumentType.value,
            documentSeries = documentSeries
        )

        if (response.isSuccessful) {
            val body = response.body() ?: throw IOException("Sunucudan boş veri geldi.")

            val result = StockTransactionDocumentDataModel(
                documentDate = Date().time,
                documentSeries = body.data.documentSeries,
                documentNumber = body.data.documentSeriesNumber,
                paperNumber = "",
                transactionType = transactionType,
                transactionKind = transactionKind,
                isNormalOrReturn = isStockTransactionNormalOrReturn,
                documentType = transactionDocumentType
            )
            emit(result)


        } else {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
    }

    override suspend fun isDocumentUsed(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String,
        documentNumber: Int
    ): Boolean {
        try {
            val response = stockTransactionService.checkDocumentIsUsable(
                documentSeries = documentSeries,
                documentNumber = documentNumber,
                companyCode = null,
                paperNumber = null,
                stockTransactionType = transactionType.value,
                stockTransactionKind = transactionKind.value,
                documentType = documentType.value,
                isNormalOrReturn = isNormalOrReturn
            )
            if(!response.isSuccessful) {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }
            val body = response.body() ?: throw IOException("Sunucudan boş veri geldi.")
            return body.isUsed!!
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getNextAvailableDocumentNumber(
        transactionType: StockTransactionTypes,
        transactionKind: StockTransactionKinds,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentTypes,
        documentSeries: String
    ): Int {
        val response = stockTransactionService.getNextStockTransactionDocument(
            stockTransactionType = transactionType.value,
            stockTransactionKind = transactionKind.value,
            isNormalOrReturn = isNormalOrReturn,
            stockTransactionDocumentType = documentType.value,
            documentSeries = documentSeries
        )

        if (response.isSuccessful) {
            val body = response.body() ?: throw IOException("Sunucudan boş veri geldi.")
            return body.data.documentSeriesNumber
        } else {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
    }
}