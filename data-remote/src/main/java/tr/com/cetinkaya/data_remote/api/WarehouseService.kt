package tr.com.cetinkaya.data_remote.api


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import tr.com.cetinkaya.common.PagedResponseModel
import tr.com.cetinkaya.data_remote.models.warehouse.GetWarehousesResponseModel

interface WarehouseService {

    @GET(GET_ALL_WAREHOUSES)
    suspend fun getWarehouses(
        @Query("PageRequest.PageIndex") pageIndex: Int, @Query("PageRequest.PageSize") pageSize: Int
    ): Response<PagedResponseModel<GetWarehousesResponseModel>>

    @GET("${GET_WAREHOUSES}/{warehouseNumber}")
    suspend fun getWarehouseByWarehouseNumber(@Path("warehouseNumber") warehouseNumber: Int) : Response<GetWarehousesResponseModel>


    companion object {
        private const val GET_WAREHOUSES = "depo-service/warehouses"
        private const val GET_ALL_WAREHOUSES = "depo-service/warehouses/get-all"
    }
}