package tr.com.cetinkaya.common

data class PagedResponseModel<T>(
    val items: List<T>,
    val index: Int,
    val size: Int,
    val count: Int,
    val pages: Int,
    val hasPrevious: Boolean,
    val hasNext: Boolean
)