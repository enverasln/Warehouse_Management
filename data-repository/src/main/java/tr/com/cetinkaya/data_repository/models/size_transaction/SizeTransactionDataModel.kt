package tr.com.cetinkaya.data_repository.models.size_transaction

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.domain.model.size_transaction.SizeTransactionDomainModel

data class SizeTransactionDataModel(
    val id: String,
    val barcode: String,
    val refRecordId: String,
    val sizeTransactionType: SizeTransactionType,
    val documentDate: Long,
    val quantity: Double
)

fun SizeTransactionDomainModel.toDataModel() = SizeTransactionDataModel(
    id = this.id,
    barcode = this.barcode,
    refRecordId = this.refRecordId,
    sizeTransactionType = this.sizeTransactionType,
    documentDate = this.documentDate,
    quantity = this.quantity
)

fun SizeTransactionDataModel.toDomainModel() = SizeTransactionDomainModel(
    id = this.id,
    barcode = this.barcode,
    refRecordId = this.refRecordId,
    sizeTransactionType = this.sizeTransactionType,
    documentDate = this.documentDate,
    quantity = this.quantity
)
