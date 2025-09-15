package tr.com.cetinkaya.data_local.db

import androidx.room.withTransaction
import tr.com.cetinkaya.common.db.TransactionRunner
import javax.inject.Inject

class RoomTransactionRunner @Inject constructor(
    private val db: AppDatabase
) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T =
        db.withTransaction { block() }
}