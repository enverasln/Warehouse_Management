package tr.com.cetinkaya.domain.model.size_transaction

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus

class SizeTransactionDomainModel(
    val id: String,
    val barcode: String,
    val refRecordId: String,
    val sizeTransactionType: SizeTransactionType,
    val documentDate: Long,
    val quantity: Double,
    val syncStatus: SyncStatus
)