package tr.com.cetinkaya.data_remote.models

sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>()
    data class Failure(val message: String, val code: Int? = null) : ResultWrapper<Nothing>()
}