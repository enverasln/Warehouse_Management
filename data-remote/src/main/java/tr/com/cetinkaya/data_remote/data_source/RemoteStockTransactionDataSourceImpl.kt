package tr.com.cetinkaya.data_remote.data_source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import tr.com.cetinkaya.data_remote.api.StockTransactionService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.stock_transaction.addStocktransaction.toRequest
import tr.com.cetinkaya.data_remote.models.stock_transaction.check_document_series_and_number.CheckDocumentIsUsableRequestRemoteModel
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
        stockTransactionType: Int,
        stockTransactionKind: Int,
        documentType: Int,
        isNormalOrReturn: Int
    ): Flow<CheckDocumentIsUsableRepositoryModel> = flow {
        val request = CheckDocumentIsUsableRequestRemoteModel(
            documentSeries = documentSeries,
            documentNumber = documentNumber,
            companyCode = companyCode,
            paperNumber = paperNumber,
            stockTransactionType = stockTransactionType,
            stockTransactionKind = stockTransactionKind,
            documentType = documentType,
            isNormalOrReturn = isNormalOrReturn
        )

        val response = stockTransactionService.checkDocumentIsUsable(
            documentSeries = request.documentSeries,
            documentNumber = request.documentNumber,
            companyCode = request.companyCode,
            paperNumber = request.paperNumber,
            stockTransactionType = request.stockTransactionType,
            stockTransactionKind = request.stockTransactionKind,
            documentType = request.documentType,
            isNormalOrReturn = request.isNormalOrReturn
        )

        if (response.isSuccessful) {
            val body = response.body() ?: throw IOException("Sunucudan boş veri geldi.")
            emit(body)
        } else {
            var error = errorParser.parse(response.errorBody())
            var message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
    }.map { result ->
        CheckDocumentIsUsableRepositoryModel(
            message = result.message, isDocumentNew = result.isDocumentNew
        )
    }

    override suspend fun sendStockTransaction(stockTransaction: StockTransactionDataModel) {
        try {
            val request = stockTransaction.toRequest()
            val response = stockTransactionService.sendStockTransaction(request)
            if (!response.isSuccessful) {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getNextStockTransactionDocument(
        stockTransactionType: Byte,
        stockTransactionKind: Byte,
        isStockTransactionNormalOrReturn: Byte,
        stockTransactionDocumentType: Byte,
        documentSeries: String
    ): Flow<StockTransactionDocumentDataModel> = flow {
        val response = stockTransactionService.getNextStockTransactionDocument(
            stockTransactionType = stockTransactionType,
            stockTransactionKind = stockTransactionKind,
            isStockTransactionNormalOrReturn = isStockTransactionNormalOrReturn,
            stockTransactionDocumentType = stockTransactionDocumentType,
            documentSeries = documentSeries
        )

        if (response.isSuccessful) {
            val body = response.body() ?: throw IOException("Sunucudan boş veri geldi.")

            val result = StockTransactionDocumentDataModel(
                documentDate = Date().time,
                documentSeries = body.data.documentSeries,
                documentNumber = body.data.documentSeriesNumber,
                paperNumber = "",
                transactionType = stockTransactionType,
                transactionKind = stockTransactionKind,
                isNormalOrReturn = isStockTransactionNormalOrReturn,
                documentType = stockTransactionDocumentType
            )
            emit(result)


        } else {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
    }
}