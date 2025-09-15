package tr.com.cetinkaya.data_remote.data_source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.StockTransactionDocumentType
import tr.com.cetinkaya.common.enums.StockTransactionKind
import tr.com.cetinkaya.common.enums.StockTransactionType
import tr.com.cetinkaya.data_remote.api.StockTransactionService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.stock_transaction.addStocktransaction.toRequest
import tr.com.cetinkaya.data_remote.models.stock_transaction.check_document_series_and_number.toDataModel
import tr.com.cetinkaya.data_remote.models.stock_transaction.get_stock_transaction_document.toDataModel
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockTransactionDataSource
import tr.com.cetinkaya.data_repository.models.order.CheckStockTxDocDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.GetStockTransactionDocumentDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDataModel
import tr.com.cetinkaya.data_repository.models.stock_transaction.StockTransactionDocumentDataModel
import java.io.IOException
import java.util.Date
import javax.inject.Inject

class RemoteStockTransactionDataSourceImpl @Inject constructor(
    private val stockTransactionService: StockTransactionService, private val errorParser: ExceptionParser
) : RemoteStockTransactionDataSource {

    override suspend fun checkStockTxDoc(
        stockTxDoc: StockTransactionDocumentDataModel,
        currentCode: String
    ): CheckStockTxDocDataModel {
        val response = stockTransactionService.checkDocumentIsUsable(
            documentSeries = stockTxDoc.documentSeries,
            documentNumber = stockTxDoc.documentNumber,
            companyCode = currentCode,
            paperNumber = stockTxDoc.paperNumber,
            stockTransactionType = stockTxDoc.transactionType.value,
            stockTransactionKind = stockTxDoc.transactionKind.value,
            documentType = stockTxDoc.transactionDocumentType.value,
            isNormalOrReturn = stockTxDoc.isNormalOrReturn
        )

        if (response.isSuccessful) {
            val body = response.body() ?: throw IOException("Sunucudan boş veri geldi.")
            return body.toDataModel()
        } else {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
    }


    override suspend fun sendStockTransaction(stockTransaction: StockTransactionDataModel): Boolean = try {
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
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isStockTransactionNormalOrReturn: Byte,
        transactionDocumentType: StockTransactionDocumentType,
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
                transactionDocumentType = transactionDocumentType
            )
            emit(result)
        } else {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
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
        try {
            val response = stockTransactionService.checkDocumentIsUsable(
                documentSeries = documentSeries,
                documentNumber = documentNumber,
                companyCode = companyCode,
                paperNumber = paperNumber,
                stockTransactionType = transactionType.value,
                stockTransactionKind = transactionKind.value,
                documentType = documentType.value,
                isNormalOrReturn = isNormalOrReturn
            )
            if (!response.isSuccessful) {
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
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType,
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

    override fun getStockTransactionDocumentByDocumentNumber(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType
    ): Flow<GetStockTransactionDocumentDataModel?> = flow {
        try {
            val response = stockTransactionService.getStockTransactionDocumentByPaperNumberAndCurrentCode(
                documentSeries = documentSeries,
                documentNumber = documentNumber,
                stockTransactionType = transactionType.value,
                stockTransactionKind = transactionKind.value,
                isNormalOrReturn = isNormalOrReturn,
                documentType = documentType.value
            )

            if (!response.isSuccessful) {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }
            val body = response.body()
            val result = body?.toDataModel()
            emit(result)

        } catch (e: Exception) {
            throw e
        }
    }

    override fun getStockTransactionDocumentByPaperNumberAndCurrentCode(
        documentSeries: String,
        documentNumber: Int,
        transactionType: StockTransactionType,
        transactionKind: StockTransactionKind,
        isNormalOrReturn: Byte,
        documentType: StockTransactionDocumentType
    ): Flow<GetStockTransactionDocumentDataModel?> = flow {
        try {
            val response = stockTransactionService.getStockTransactionDocumentByPaperNumberAndCurrentCode(
                documentSeries = documentSeries,
                documentNumber = documentNumber,
                stockTransactionType = transactionType.value,
                stockTransactionKind = transactionKind.value,
                isNormalOrReturn = isNormalOrReturn,
                documentType = documentType.value
            )

            if (!response.isSuccessful) {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }
            val body = response.body()
            val result = body?.toDataModel()
            emit(result)

        } catch (e: Exception) {
            throw e
        }
    }
}