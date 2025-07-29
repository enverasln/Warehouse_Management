package tr.com.cetinkaya.domain.repository

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes

interface TransferredDocumentRepository {


    fun add(
        transferredDocumentType: TransferredDocumentTypes,
        documentSeries: String,
        documentNumber: Int,
        synchronizationStatus: Boolean,
        description: String
    ): Flow<Long>
}