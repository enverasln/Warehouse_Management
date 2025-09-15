package tr.com.cetinkaya.common.db

interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T) : T
}