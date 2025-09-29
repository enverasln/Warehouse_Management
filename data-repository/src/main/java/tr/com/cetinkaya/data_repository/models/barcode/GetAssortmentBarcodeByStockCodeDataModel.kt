package tr.com.cetinkaya.data_repository.models.barcode

import tr.com.cetinkaya.domain.model.barcode.GetAssortmentBarcodeByStockCodeDomainModel


data class GetAssortmentBarcodeByStockCodeDataModel(
    val barcode: String
)


fun GetAssortmentBarcodeByStockCodeDataModel.toDomainModel(): GetAssortmentBarcodeByStockCodeDomainModel = GetAssortmentBarcodeByStockCodeDomainModel(
    barcode = this.barcode
)


