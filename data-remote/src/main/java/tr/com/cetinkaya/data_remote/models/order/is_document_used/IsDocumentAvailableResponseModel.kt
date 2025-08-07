package tr.com.cetinkaya.data_remote.models.order.is_document_used

import com.google.gson.annotations.SerializedName

data class IsDocumentAvailableResponseModel(
    @SerializedName("message")val message: String,
    @SerializedName("isAvailable")val isAvailable: Boolean
)