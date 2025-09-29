package tr.com.cetinkaya.data_remote.data_source

import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.data_remote.api.StockService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.stock.toDataModel
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteStockDataSource
import tr.com.cetinkaya.data_repository.models.stock.GetStockBuyingConditionDataModel
import javax.inject.Inject

class RemoteStockDataSourceImpl @Inject constructor(
    private val stockService: StockService, private val errorParser: ExceptionParser
) : RemoteStockDataSource {

    override suspend fun getStockBuyingCondition(
        currentCode: String?, stockCode: String, date: Long, warehouseNumber: Int
    ): GetStockBuyingConditionDataModel {
        val apiDate = DateConverter.timeStampToApi(date)
        try {
            val response = stockService.getBuyingPrice(
                currentCode = currentCode, stockCode = stockCode, date = apiDate, warehouseNumber = warehouseNumber
            )
            if (!response.isSuccessful) {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }

            val responseBody = response.body()
            if (responseBody == null) throw Exception("Sunucudan boş veri geldi.")


            return responseBody.toDataModel()

        } catch (e: Exception) {
            throw e
        }
    }
}