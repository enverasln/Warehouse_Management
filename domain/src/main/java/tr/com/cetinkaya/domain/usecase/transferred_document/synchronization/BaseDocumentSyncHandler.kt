package tr.com.cetinkaya.domain.usecase.transferred_document.synchronization

import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.domain.model.transferred_document.TransferredDocumentDomainModel
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

abstract class BaseDocumentSyncHandler(
    private val transferredDocumentRepository: TransferredDocumentRepository
) : DocumentSyncHandler {


    final override suspend fun sync(
        document: TransferredDocumentDomainModel, emit: suspend (SyncProgress) -> Unit
    ) {
        val reporter = ProgressReporter(emit)

        try {
            reporter.emit(SyncProgress.InProgress("Evrak kontrolü: ${document.documentSeries}-${document.documentNumber}"))

            // 1) Evrak no kullanılıyor mu?
            var documentNumber = document.documentNumber
            val used = isDocumentUsed(document.documentSeries, document.documentNumber)

            if (shouldReNumberWhenUsed() && used) {
                // 2) Uygun yeni no
                val newNo = getNextAvailableDocumentNumber(document.documentSeries)
                reporter.emit(
                    SyncProgress.InProgress("${document.documentSeries}-${document.documentNumber} zaten kullanılmış. Yeni evrak no veriliyor: $newNo")
                )

                // 3) Hem domain objelerini hem transferred kaydını güncelle
                updateDomainDocumentNumber(document.documentSeries, document.documentNumber, newNo)
                updateTransferredDocumentNumber(document.transferredDocumentType, document.documentSeries, document.documentNumber, newNo)

                documentNumber = newNo
                reporter.emit(
                    SyncProgress.InProgress("${document.documentSeries}-${document.documentNumber} -> ${document.documentSeries}-${newNo} olarak güncellendi.")
                )
            }

            // 4) Unsynced kayıtları oku ve gönder
            reporter.emit(
                SyncProgress.InProgress("${document.documentSeries}-${documentNumber} içeriği gönderiliyor...")
            )
            val sentCount = syncAndMark(document, documentNumber)

            // 5) Transferred Document'i işaretle
            markTransferredDocument(document.transferredDocumentType, document.documentSeries, documentNumber)

            reporter.emit(
                SyncProgress.InProgress("Gönderim tamamlandı. Toplam gönderilen: $sentCount")
            )
            reporter.emit(SyncProgress.Completed(document.documentSeries, documentNumber))
        } catch (t: Throwable) {
            reporter.emit(SyncProgress.Error("Senkronizasyon hatası: ${t.message ?: t::class.simpleName}"))
        }
    }

    /**
     * Varsayılan: evrak no kullanılıyorsa yeni no ver.
     * Bazı iş akışlarında “kullanılmışsa hata” istenebilir – o durumda override edip false dönebilirsin.
     */
    protected open fun shouldReNumberWhenUsed(): Boolean = true

    // --- Alt sınıfların sağlaması gereken tip-özel adımlar ---

    /** Evrak no kullanılmış mı? */
    protected abstract suspend fun isDocumentUsed(series: String, number: Int): Boolean

    /** Bir sonraki uygun evrak no */
    protected abstract suspend fun getNextAvailableDocumentNumber(series: String): Int

    /** Domain nesnelerindeki (sipariş/sto hareketi) evrak no güncellemesi */
    protected abstract suspend fun updateDomainDocumentNumber(series: String, oldNumber: Int, newNumber: Int)

    /** TransferredDocument kaydındaki evrak no güncellemesi */
    protected open suspend fun updateTransferredDocumentNumber(
        docType: TransferredDocumentTypes, series: String, oldNumber: Int, newNumber: Int
    ) {
        transferredDocumentRepository.updateTransferredDocument(
            transferredDocumentType = docType, documentSeries = series, oldDocumentNumber = oldNumber, newDocumentNumber = newNumber
        )
    }

    /** Unsynced kayıtları gönder + işaretle. Geriye gönderilen sayıyı döndür. */
    protected abstract suspend fun syncAndMark(
        document: TransferredDocumentDomainModel, documentNumber: Int
    ): Int

    /** TransferredDocument’i senkronize olarak işaretle. */
    protected open suspend fun markTransferredDocument(
        docType: TransferredDocumentTypes, series: String, number: Int
    ) {
        transferredDocumentRepository.markedTransferredDocumentSynced(
            documentType = docType, documentSeries = series, documentNumber = number
        )
    }

}