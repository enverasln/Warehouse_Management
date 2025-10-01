package tr.com.cetinkaya.data_local.db.entities

import androidx.room.Entity
import androidx.room.Index
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.enums.SyncStatus
import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel
import java.util.UUID

@Entity(
    tableName = "size_transactions", primaryKeys = ["id"], indices = [Index(value = ["sizeTransactionType", "refRecordId", "barcode"], unique = true)]
)
data class SizeTransactionEntity(
    val id: String,
    val barcode: String,
    val refRecordId: String,
    val sizeTransactionType: SizeTransactionType,
    val documentDate: Long,
    val quantity: Double,
    val syncStatus: SyncStatus
) {
    companion object {
        fun create(
            barcode: String, refRecordId: String, sizeTransactionType: SizeTransactionType, quantity: Double, syncStatus: SyncStatus
        ) = SizeTransactionEntity(
            id = UUID.randomUUID().toString(),
            barcode = barcode,
            refRecordId = refRecordId,
            sizeTransactionType = sizeTransactionType,
            documentDate = System.currentTimeMillis(),
            quantity = quantity,
            syncStatus = syncStatus
        )
    }
}

fun SizeTransactionDataModel.toEntity() = SizeTransactionEntity(
    id = this.id,
    barcode = this.barcode,
    refRecordId = this.refRecordId,
    sizeTransactionType = this.sizeTransactionType,
    documentDate = this.documentDate,
    quantity = this.quantity,
    syncStatus = this.syncStatus
)

fun List<SizeTransactionDataModel>.toEntity() = this.map { it.toEntity() }

fun SizeTransactionEntity.toDataModel() = SizeTransactionDataModel(
    id = this.id,
    barcode = this.barcode,
    sizeTransactionType = this.sizeTransactionType,
    refRecordId = this.refRecordId,
    documentDate = this.documentDate,
    quantity = this.quantity,
    syncStatus = this.syncStatus
)

fun List<SizeTransactionEntity>.toDataModel() = this.map { it.toDataModel() }

fun SizeTransactionEntity.toProductDataModel() = SizeTransactionDataModel(
    id = this.id,
    barcode = this.barcode,
    refRecordId = this.refRecordId,
    sizeTransactionType = this.sizeTransactionType,
    documentDate = this.documentDate,
    quantity = this.quantity,
    syncStatus = this.syncStatus
)