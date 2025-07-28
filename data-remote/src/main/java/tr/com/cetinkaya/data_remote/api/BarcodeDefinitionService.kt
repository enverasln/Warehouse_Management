package tr.com.cetinkaya.data_remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import tr.com.cetinkaya.data_remote.models.barcode.GetBarcodeDefinitionByBarcodeResponseModel

interface BarcodeDefinitionService {


    @GET(GET_BARCODE_DEFINITION_BY_BARCODE)
    suspend fun getByBarcode(
        @Query("barcode") barcode: String,
        @Query("warehouseNumber") warehouse: Int
    ): Response<GetBarcodeDefinitionByBarcodeResponseModel>


    companion object {
        private const val GET_BARCODE_DEFINITION_BY_BARCODE = "depo-service/barcode-definitions/get-by-barcode"
    }
}