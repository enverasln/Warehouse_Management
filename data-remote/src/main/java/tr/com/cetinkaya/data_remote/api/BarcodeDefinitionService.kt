package tr.com.cetinkaya.data_remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tr.com.cetinkaya.data_remote.models.barcode.GetBarcodeDefinitionByBarcodeResponseModel
import tr.com.cetinkaya.data_remote.models.barcode.GetAssortmentBarcodeByStockCodeResponseModel

interface BarcodeDefinitionService {


    @GET(GET_BARCODE_DEFINITION_BY_BARCODE)
    suspend fun getByBarcode(
        @Query("barcode") barcode: String,
        @Query("warehouseNumber") warehouse: Int
    ): Response<GetBarcodeDefinitionByBarcodeResponseModel>

    @GET(GET_ASSORTMENT_BARCODE_BY_STOCK_CODE)
    suspend fun getStockBarcodesByStockCode(@Query("stockCode") stockCode: String): Response<GetAssortmentBarcodeByStockCodeResponseModel>


    companion object {

        private const val BARCODE_DEFINITION = "depo-service/barcode-definitions"
        private const val GET_BARCODE_DEFINITION_BY_BARCODE = "depo-service/barcode-definitions/get-by-barcode"
        private const val GET_ASSORTMENT_BARCODE_BY_STOCK_CODE = "$BARCODE_DEFINITION/get-assortment-barcode"
    }
}