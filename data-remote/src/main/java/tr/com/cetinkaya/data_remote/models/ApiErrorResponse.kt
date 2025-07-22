package tr.com.cetinkaya.data_remote.models

data class ApiErrorResponse(
    val type: String?,
    val title: String?,
    val status: Int?,
    val detail: String?, // business error
    val errors: Map<String, List<String>>? // validation error
)