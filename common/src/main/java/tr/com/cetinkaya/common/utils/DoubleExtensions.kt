package tr.com.cetinkaya.common.utils

object DoubleExtensions {
    fun Double?.isNullOrZero(): Boolean = this == null || this == 0.0
}