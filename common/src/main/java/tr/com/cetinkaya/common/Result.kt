package tr.com.cetinkaya.common

import java.lang.Exception

sealed class Result<out T:Any>{
    data object Loading : Result<Nothing>()
    data class Success<out T:Any>(val data:T) : Result<T>()
    data class Error(val message: String, val code: Int? = null, val throwable: Throwable? = null) : Result<Nothing>()
}