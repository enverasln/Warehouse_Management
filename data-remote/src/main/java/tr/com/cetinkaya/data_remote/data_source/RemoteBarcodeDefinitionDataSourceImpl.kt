package tr.com.cetinkaya.data_remote.data_source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import tr.com.cetinkaya.data_remote.api.BarcodeDefinitionService
import tr.com.cetinkaya.data_remote.exception.ExceptionParser
import tr.com.cetinkaya.data_remote.models.barcode.toDataModel
import tr.com.cetinkaya.data_repository.datasource.remote.RemoteBarcodeDefinitionDataSource
import tr.com.cetinkaya.data_repository.models.barcode.GetBarcodeDefinitionByBarcodeDataModel
import javax.inject.Inject

class RemoteBarcodeDefinitionDataSourceImpl @Inject constructor(
    private val barcodeDefinitionService: BarcodeDefinitionService,
    private val errorParser: ExceptionParser
) : RemoteBarcodeDefinitionDataSource {

    override fun getByBarcode(
        barcode: String, warehouse: Int
    ): Flow<GetBarcodeDefinitionByBarcodeDataModel> = flow {
        try {
            val response = barcodeDefinitionService.getByBarcode(barcode, warehouse)

            if(response.isSuccessful) {
                val responseBody = response.body()
                if(responseBody != null) {
                    emit(responseBody.toDataModel())
                }
            } else {
                val error = errorParser.parse(response.errorBody())
                val message = error?.detail ?: error?.errors?.values?.flatten()?.joinToString() ?: "Sunucu hatası"
                throw Exception(message)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}