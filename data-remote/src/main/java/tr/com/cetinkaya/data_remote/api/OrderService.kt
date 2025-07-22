package tr.com.cetinkaya.data_remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import tr.com.cetinkaya.common.PagedResponseModel
import tr.com.cetinkaya.data_remote.models.order.GetNextDocumentSeriesAndNumberRemoteModel
import tr.com.cetinkaya.data_remote.models.order.add_order.AddOrderRequestModel
import tr.com.cetinkaya.data_remote.models.order.planned_goods_acceptance.DocumentRemoteModel
import tr.com.cetinkaya.data_remote.models.order.planned_goods_acceptance.PlannedGoodsAcceptanceProductResponseRemoteModel

interface OrderService {

    @GET(GET_PLANNED_GOODS_ACCEPTANCE_DOCUMENTS)
    suspend fun getPlannedGoodsAcceptanceDocuments(
        @Query("PageRequest.PageIndex") pageIndex: Int,
        @Query("PageRequest.PageSize") pageSize: Int,
        @Query("DepoNo") warehouseNumber: Int,
        @Query("CariUnvan") companyName: String,
        @Query("SiparisTarihi") documentDate: String
    ): Response<PagedResponseModel<DocumentRemoteModel>>

    @GET(GET_PLANNED_GOODS_ACCEPTANCE_PRODUCTS)
    suspend fun getPlannedGoodsAcceptanceProducts(
        @Query("EvrakSeri") documentSeries: String, @Query("EvrakSira") documentNumber: Int, @Query("DepoNo") warehouseNumber: Int
    ): Response<List<PlannedGoodsAcceptanceProductResponseRemoteModel>>

    @GET(GET_NEXT_ORDER_DOCUMENT_SERIES_AND_NUMBER)
    suspend fun getNextDocumentSeriesAndNumber(
        @Query("SipTip") orderType: Byte, @Query("SipCins") orderKind: Byte, @Query("EvraknoSeri") documentSeries: String
    ): Response<GetNextDocumentSeriesAndNumberRemoteModel>

    @POST(ADD_ORDER)
    suspend fun sendOrder(@Body body: AddOrderRequestModel): Response<Unit>


    companion object {
        private const val GET_PLANNED_GOODS_ACCEPTANCE_DOCUMENTS = "depo-service/siparisler/planli-mal-kabul-evraklari"
        private const val GET_PLANNED_GOODS_ACCEPTANCE_PRODUCTS = "depo-service/siparisler/planli-mal-kabul-evraklari/urunler"
        private const val GET_NEXT_ORDER_DOCUMENT_SERIES_AND_NUMBER = "depo-service/siparisler/planli-mal-kabul-evraklari/siradaki-evrak-seri-sira"
        private const val ADD_ORDER = "depo-service/siparisler/normal-siparis-ekle"
    }
}