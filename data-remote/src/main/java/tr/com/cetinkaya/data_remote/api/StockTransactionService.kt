package tr.com.cetinkaya.data_remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import tr.com.cetinkaya.common.DataResponseModel
import tr.com.cetinkaya.data_remote.models.stock_transaction.addStocktransaction.AddStockTransactionRequestModel
import tr.com.cetinkaya.data_remote.models.stock_transaction.check_document_series_and_number.CheckDocumentSeriesAndNumberResponseRemoteModel
import tr.com.cetinkaya.data_remote.models.stock_transaction.get_next_stock_transaction_document.GetNextStockTransactionDocumentResponse

interface StockTransactionService {

    @GET(CHECK_DOCUMENT_USABLE)
    suspend fun checkDocumentIsUsable(
        @Query("evrakNoSeri") documentSeries: String,
        @Query("evrakNoSira") documentNumber: Int,
        @Query("cariKod") companyCode: String,
        @Query("belgeNo") paperNumber: String,
        @Query("StokHareketTipi") stockTransactionType: Int,
        @Query("StokHareketCinsi") stockTransactionKind: Int,
        @Query("StokHareketEvrakTipi") documentType: Int,
        @Query("StokHareketIslemTipi") isNormalOrReturn: Int
    ): Response<CheckDocumentSeriesAndNumberResponseRemoteModel>

    @POST(ADD_STOCK_TRANSACTION)
    suspend fun sendStockTransaction(
        @Body body: AddStockTransactionRequestModel
    ) : Response<Unit>

    @GET(GET_NEXT_STOCK_TRANSACTION_DOCUMENT)
    suspend fun getNextStockTransactionDocument(
        @Query ("stokHareketTipi") stockTransactionType: Byte,
        @Query ("stokHareketCinsi") stockTransactionKind: Byte,
        @Query ("stokHareketIslemTipi") isStockTransactionNormalOrReturn: Byte,
        @Query ("stokHareketEvrakTipi") stockTransactionDocumentType: Byte,
        @Query ("evrakNoSeri") documentSeries: String
    ) : Response<DataResponseModel<GetNextStockTransactionDocumentResponse>>

    companion object {
        private const val CHECK_DOCUMENT_USABLE = "depo-service/stok-hareketleri/check-document-usable"
        private const val ADD_STOCK_TRANSACTION = "depo-service/stok-hareketleri/terminal-shar-ekle"
        private const val GET_NEXT_STOCK_TRANSACTION_DOCUMENT = "depo-service/stok-hareketleri/get-next-document"

    }
}