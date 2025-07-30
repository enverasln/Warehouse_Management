package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.common.enums.TransferredDocumentTypes
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel
import tr.com.cetinkaya.domain.repository.TransferredDocumentRepository

interface LocalTransferredDocumentDataSource {

    suspend fun add(transferredDocument: TransferredDocumentDataModel) : Long

    suspend fun delete(
        transferredDocumentTypes: TransferredDocumentTypes,
        documentSeries: String,
        documentNumber: Int
    ) : Int

    fun getUntransferredDocuments() : Flow<List<TransferredDocumentDataModel>>
}