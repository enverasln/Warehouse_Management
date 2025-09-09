package tr.com.cetinkaya.domain.model.size_transaction

import tr.com.cetinkaya.common.enums.SizeTransactionType

class SizeTransactionDomainModel(
    val id: String,
    val barcode: String,
    val refRecordId: String,
    val sizeTransactionType: SizeTransactionType,
    val documentDate: Long,
    val quantity: Double
)