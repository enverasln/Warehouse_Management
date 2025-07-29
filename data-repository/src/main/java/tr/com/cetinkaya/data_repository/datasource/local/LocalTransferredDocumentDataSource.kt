package tr.com.cetinkaya.data_repository.datasource.local

import kotlinx.coroutines.flow.Flow
import tr.com.cetinkaya.data_repository.models.transferred_document.TransferredDocumentDataModel

interface LocalTransferredDocumentDataSource {

    suspend fun add(transferredDocument: TransferredDocumentDataModel) : Long
}