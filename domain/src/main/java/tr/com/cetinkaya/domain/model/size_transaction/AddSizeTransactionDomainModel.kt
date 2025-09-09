package tr.com.cetinkaya.domain.model.size_transaction

import tr.com.cetinkaya.common.enums.SizeTransactionType

data class AddSizeTransactionDomainModel(
    val barcode: String,
    val refRecordId: String,
    val sizeTransactionType: SizeTransactionType,
    val quantity: Double
)

