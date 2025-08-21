package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

sealed class SyncProgress {
    data class Started(val message: String) : SyncProgress()
    data class InProgress(val message: String) : SyncProgress()
    data class Completed(val documentSeries: String, val documentNumber: Int) : SyncProgress()
    data class Error(val error: String) : SyncProgress()
}