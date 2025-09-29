package tr.com.cetinkaya.data_repository.datasource.remote

import tr.com.cetinkaya.data_repository.models.stock.GetStockBuyingConditionDataModel

interface RemoteStockDataSource {

    suspend fun getStockBuyingCondition(currentCode: String?, stockCode: String, date: Long, warehouseNumber: Int): GetStockBuyingConditionDataModel
}