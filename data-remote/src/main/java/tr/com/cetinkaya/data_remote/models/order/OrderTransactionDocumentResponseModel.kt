package tr.com.cetinkaya.data_remote.models.order

import com.google.gson.annotations.SerializedName
import tr.com.cetinkaya.data_repository.models.order_transaction.OrderTransactionDocumentDataModel

data class OrderTransactionDocumentResponseModel(
    @SerializedName("evraknoSeri")val docSeries: String,
    @SerializedName("evraknoSira")val docNumber: Int
)

fun OrderTransactionDocumentResponseModel.toDataModel() = OrderTransactionDocumentDataModel(
    docSeries = this.docSeries,
    docNumber = this.docNumber
)
