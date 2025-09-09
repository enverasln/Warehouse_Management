package tr.com.cetinkaya.data_repository.models.size_transaction

import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.domain.model.size_transaction.AddSizeTransactionDomainModel

data class AddSizeTransactionDataModel(
    val barcode: String,
    val refRecordId: String,
    val sizeTransactionType: SizeTransactionType,
    val quantity: Double
)


fun AddSizeTransactionDomainModel.toDataModel() = AddSizeTransactionDataModel(
    barcode = this.barcode,
    refRecordId = this.refRecordId,
    sizeTransactionType = this.sizeTransactionType,
    quantity = this.quantity
)

fun List<AddSizeTransactionDomainModel>.toDataModel() = this.map { it.toDataModel() }