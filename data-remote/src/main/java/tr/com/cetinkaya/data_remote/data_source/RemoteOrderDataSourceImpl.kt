package tr.com.cetinkaya.data_remote.data_source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.common.enums.OrderTransactionKinds
import tr.com.cetinkaya.common.enums.OrderTransactionTypes
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.data_remote.api.OrderService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.order.add_order.toAddOrderRequestModel
import tr.com.cetinkaya.data_remote.models.order.planned_goods_acceptance.toDataModel
import tr.com.cetinkaya.data_remote.models.order.toDataModel
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteOrderDataSource
import tr.com.cetinkaya.data_repository.models.order.GetNextDocumentSeriesAndNumberDataModel
import tr.com.cetinkaya.data_repository.models.order.OrderDataModel
import tr.com.cetinkaya.data_repository.models.order.PlannedGoodsAcceptanceDocumentRepositoryModel
import tr.com.cetinkaya.data_repository.models.order.ProductDataModel
import javax.inject.Inject

class RemoteOrderDataSourceImpl @Inject constructor(
    private val orderService: OrderService, private val errorParser: ExceptionParser
) : RemoteOrderDataSource {

    override fun getPlannedGoodsAcceptanceDocuments(
        warehouseNumber: Int, companyName: String, documentDate: String
    ): Flow<List<PlannedGoodsAcceptanceDocumentRepositoryModel>> = flow {

        val plannedGoodsAcceptanceDocuments = mutableListOf<PlannedGoodsAcceptanceDocumentRepositoryModel>()

        var pageIndex = 0
        val pageSize = 250

        do {
            val response = orderService.getPlannedGoodsAcceptanceDocuments(
                pageIndex, pageSize, warehouseNumber, companyName, documentDate
            )

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    val remoteDocuments = responseBody.items
                    val repositoryDocuments = remoteDocuments.map { document ->
                        PlannedGoodsAcceptanceDocumentRepositoryModel(
                            documentDate = document.documentDate,
                            warehouseNumber = document.warehouseNumber,
                            companyCode = document.companyCode,
                            companyName = document.companyName,
                            documentSeries = document.documentSeries,
                            documentSeriesNumber = document.documentSeriesNumber
                        )
                    }
                    plannedGoodsAcceptanceDocuments.addAll(repositoryDocuments)
                    pageIndex++
                }
            } else {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString("\n") ?: "Sunucu hatası"
                throw Exception(message)
            }

        } while (response.body()?.hasNext == true)
        emit(plannedGoodsAcceptanceDocuments)
    }

    override suspend fun getPlannedGoodsAcceptanceProducts(
        documentSeries: String, documentNumber: Int, warehouseNumber: Int
    ): List<ProductDataModel> = try {
        val response = orderService.getPlannedGoodsAcceptanceProducts(documentSeries, documentNumber, warehouseNumber)

        if (!response.isSuccessful) {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString("\n") ?: "Sunucu hatası"
            throw Exception(message)
        }

        val responseBody = response.body()

        responseBody?.map { it.toDataModel() } ?: emptyList()

    } catch (e: Exception) {
        throw Exception(e.message)
    }


    override suspend fun getNextAvailableDocumentNumber(
        orderType: OrderTransactionTypes, orderKind: OrderTransactionKinds, documentSeries: String
    ): GetNextDocumentSeriesAndNumberDataModel = try {
        val response = orderService.getNextDocumentSeriesAndNumber(
            orderType = orderType.value, orderKind = orderKind.value, documentSeries = documentSeries
        )

        if (!response.isSuccessful) {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString("\n") ?: "Sunucu hatası"
        }

        val responseBody = response.body()

        if (responseBody == null) throw Exception("Sıradaki doküman bilgisi boş olarak geldi. Lütfen tekrar deneyiniz")

        responseBody.toDataModel()
    } catch (e: Exception) {
        throw e
    }

    override suspend fun sendOrder(order: OrderDataModel): Boolean = try {
        val orderDateTimeStamp  = DateConverter.uiToTimestamp(order.orderDate)
        val orderDateApiDate = DateConverter.timeStampToApi(orderDateTimeStamp ?: 0)


        val request = order.toAddOrderRequestModel().copy(orderDate = orderDateApiDate)

        val response = orderService.sendOrder(request)
        if (!response.isSuccessful) {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
        true
    } catch (e: Exception) {
        throw e
    }


    override suspend fun isDocumentUsed(
        transactionType: OrderTransactionTypes, transactionKind: OrderTransactionKinds, documentSeries: String, documentNumber: Int
    ): Boolean = try {
        val response = orderService.isDocumentAvailable(
            transactionType = transactionType.value,
            transactionKind = transactionKind.value,
            documentSeries = documentSeries,
            documentNumber = documentNumber
        )



        if (!response.isSuccessful) {
            val error = errorParser.parse(response.errorBody())
            val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
            throw Exception(message)
        }
        response.body()?.isAvailable ?: false
    } catch (e: Exception) {
        throw e

    }
}