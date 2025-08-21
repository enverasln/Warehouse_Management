package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

fun interface ProgressReporter {
    suspend fun emit(progress: SyncProgress)
}