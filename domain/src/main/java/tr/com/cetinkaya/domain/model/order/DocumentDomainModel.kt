package tr.com.cetinkaya.domain.model.order

import java.util.Date

data class DocumentDomainModel(
    val documentDate: Date,
    val warehouseNumber: Int,
    val companyCode: String,
    val companyName: String,
    val documentSeries: String,
    val documentNumber: Int,
    val isSelected: Boolean
)