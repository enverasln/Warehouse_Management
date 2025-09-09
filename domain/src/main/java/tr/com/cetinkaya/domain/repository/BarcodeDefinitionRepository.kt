package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.domain.model.barcode.BarcodeDefinitionDomainModel

interface BarcodeDefinitionRepository {

    fun getByBarcode(barcode: String, warehouse: Int) : Flow<BarcodeDefinitionDomainModel>
}