package tr.com.cetinkaya.domain.model.stock

data class GetStockBuyingConditionDomainModel(
    val grossPrice: Double,
    val discount1: Double,
    val discount2: Double,
    val discount3: Double,
    val discount4: Double,
    val discount5: Double,
    val vatRate: Byte
)