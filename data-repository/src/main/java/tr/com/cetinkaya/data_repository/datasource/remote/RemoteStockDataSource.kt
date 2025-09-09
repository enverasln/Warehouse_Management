package tr.com.cetinkaya.data_repository.datasource.remote

import tr.com.cetinkaya.data_repository.models.stock.GetStockBuyingConditionDataModel
import tr.com.cetinkaya.domain.model.stock.GetStockBuyingConditionDomainModel
import java.util.Date

interface RemoteStockDataSource {

    suspend fun getStockBuyingCondition(currentCode: String?, stockCode: String, date: Long, warehouseNumber: Int): GetStockBuyingConditionDataModel
}