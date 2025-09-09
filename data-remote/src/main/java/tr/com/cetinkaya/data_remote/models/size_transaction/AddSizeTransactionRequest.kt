package tr.com.cetinkaya.data_remote.models.size_transaction

import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.common.enums.SizeTransactionType
import tr.com.cetinkaya.common.utils.DateConverter
import tr.com.cetinkaya.data_repository.models.size_transaction.SizeTransactionDataModel

data class AddSizeTransactionRequest(
    @SerializedName("barkod") val barcode: String,
    @SerializedName("recordId") val refRecordId: String,
    @SerializedName("bedenHareketTip") val sizeTransactionType: Byte,
    @SerializedName("evrakTarihi") val documentDate: String,
    @SerializedName("miktar") val quantity: Double
)


fun SizeTransactionDataModel.toRequestModel() = AddSizeTransactionRequest(
    barcode = this.barcode,
    refRecordId = this.refRecordId,
    sizeTransactionType = sizeTransactionType.value,
    documentDate = DateConverter.timeStampToApi(this.documentDate),
    quantity = this.quantity
)

fun List<SizeTransactionDataModel>.toRequestModel() = this.map { it.toRequestModel() }