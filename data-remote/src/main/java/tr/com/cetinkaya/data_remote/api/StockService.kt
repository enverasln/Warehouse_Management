package tr.com.cetinkaya.data_remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tr.com.cetinkaya.data_remote.models.stock.GetStockBuyingConditionResponseModel

interface StockService {

    @GET(GET_BUYING_PRICE)
    suspend fun getBuyingPrice(
        @Query("CurrentCode") currentCode: String?,
        @Query("StockCode") stockCode: String,
        @Query("Date") date: String,
        @Query("WarehouseNumber") warehouseNumber: Int
    ): Response<GetStockBuyingConditionResponseModel>


    companion object {
        private const val SIZE_TRANSACTIONS = "depo-service/stocks"
        private const val GET_BUYING_PRICE = "${SIZE_TRANSACTIONS}/get-purchase-price"
    }
}