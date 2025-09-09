package tr.com.cetinkaya.data_repository.models.stock

import tr.com.cetinkaya.domain.model.stock.GetStockBuyingConditionDomainModel

data class GetStockBuyingConditionDataModel(
    val grossPrice: Double,
    val discount1: Double,
    val discount2: Double,
    val discount3: Double,
    val discount4: Double,
    val discount5: Double,
    val vatRate: Byte
)

fun GetStockBuyingConditionDomainModel.toDataModel() = GetStockBuyingConditionDataModel(
    grossPrice = this.grossPrice,
    discount1 = this.discount1,
    discount2 = this.discount2,
    discount3 = this.discount3,
    discount4 = this.discount4,
    discount5 = this.discount5,
    vatRate = this.vatRate
)

fun GetStockBuyingConditionDataModel.toDomainModel() = GetStockBuyingConditionDomainModel(
    grossPrice = this.grossPrice,
    discount1 = this.discount1,
    discount2 = this.discount2,
    discount3 = this.discount3,
    discount4 = this.discount4,
    discount5 = this.discount5,
    vatRate = this.vatRate
)